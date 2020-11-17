package saros.net.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.Deflater;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import saros.annotations.Component;
import saros.net.ConnectionState;
import saros.net.IPacketInterceptor;
import saros.net.ITransferListener;
import saros.net.ITransmitter;
import saros.net.stream.StreamMode;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;

/**
 * ITransmitter implementation using XMPP, IBB streams and Socks5 streams for sending packet
 * extensions and packets.
 */
@Component(module = "net")
public class XMPPTransmitter implements ITransmitter, IConnectionListener {

  private static final Logger log = Logger.getLogger(XMPPTransmitter.class);

  /** size in bytes that a packet extension must exceed to be compressed */
  private static final int PACKET_EXTENSION_COMPRESS_THRESHOLD =
      Integer.getInteger("saros.net.transmitter.PACKET_EXTENSION_COMPRESS_THRESHOLD", 32);

  private static final int CHUNKSIZE = 16 * 1024;

  private final DataTransferManager dataManager;

  private Connection connection;

  private final CopyOnWriteArrayList<ITransferListener> transferListeners =
      new CopyOnWriteArrayList<>();

  private final CopyOnWriteArrayList<IPacketInterceptor> packetInterceptors =
      new CopyOnWriteArrayList<>();

  private volatile JID localJid;

  public XMPPTransmitter(DataTransferManager dataManager, XMPPConnectionService connectionService) {
    connectionService.addListener(this);
    this.dataManager = dataManager;
  }

  @Override
  public void send(JID recipient, PacketExtension extension) throws IOException {
    send(null, recipient, extension);
  }

  @Override
  public void send(String connectionID, JID recipient, PacketExtension extension)
      throws IOException {

    boolean sendPacket = true;

    for (IPacketInterceptor packetInterceptor : packetInterceptors)
      sendPacket &= packetInterceptor.sendPacket(connectionID, recipient, extension);

    if (!sendPacket) return;

    final JID currentLocalJid = localJid;

    if (currentLocalJid == null) throw new IOException("not connected to a XMPP server");

    IByteStreamConnection connection = dataManager.getConnection(connectionID, recipient);

    if (connectionID != null && connection == null)
      throw new IOException(
          "not connected to " + recipient + " [connection identifier=" + connectionID + "]");

    if (connection == null) connection = dataManager.connect(recipient);

    /*
     * The TransferDescription can be created out of the session, the name
     * and namespace of the packet extension and standard values and thus
     * transparent to users of this method.
     */
    final TransferDescription transferDescription =
        TransferDescription.newDescription()
            .setSender(currentLocalJid)
            .setRecipient(recipient)
            .setElementName(extension.getElementName())
            .setNamespace(extension.getNamespace());

    byte[] data = extension.toXML().getBytes("UTF-8");

    if (data.length > PACKET_EXTENSION_COMPRESS_THRESHOLD) {
      transferDescription.setCompressContent(true);
    }

    sendPacketExtension(connection, transferDescription, data);
  }

  @Override
  public void sendPacketExtension(JID recipient, PacketExtension extension) {
    Message message = new Message();
    message.addExtension(extension);
    message.setTo(recipient.toString());

    assert recipient.toString().equals(message.getTo());

    try {
      sendPacket(message);
    } catch (IOException e) {
      log.error("could not send message to " + recipient, e);
    }
  }

  @Override
  public synchronized void sendPacket(Packet packet) throws IOException {

    if (isConnectionInvalid()) throw new IOException("not connected to a XMPP server");

    try {
      connection.sendPacket(packet);
    } catch (Exception e) {
      throw new IOException("could not send packet " + packet + " : " + e.getMessage(), e);
    }
  }

  @Override
  public void addTransferListener(final ITransferListener listener) {
    transferListeners.addIfAbsent(listener);
  }

  @Override
  public void removeTransferListener(final ITransferListener listener) {
    transferListeners.remove(listener);
  }

  @Override
  public void addPacketInterceptor(final IPacketInterceptor interceptor) {
    packetInterceptors.addIfAbsent(interceptor);
  }

  @Override
  public void removePacketInterceptor(final IPacketInterceptor interceptor) {
    packetInterceptors.remove(interceptor);
  }

  @Override
  public synchronized void connectionStateChanged(Connection connection, ConnectionState state) {

    switch (state) {
      case CONNECTING:
        this.connection = connection;
        break;
      case CONNECTED:
        localJid = new JID(connection.getUser());
        break;
      case ERROR:
      case NOT_CONNECTED:
        this.connection = null;
        localJid = null;
        break;
      default:
        break; // NOP
    }
  }

  /**
   * Determines if the connection can be used. Helper method for error handling.
   *
   * @return false if the connection can be used, true otherwise.
   */
  private synchronized boolean isConnectionInvalid() {
    return connection == null || !connection.isConnected();
  }

  private void sendPacketExtension(
      final IByteStreamConnection connection, final TransferDescription description, byte[] payload)
      throws IOException {

    if (log.isTraceEnabled())
      log.trace(
          "send "
              + description
              + ", data len="
              + payload.length
              + " byte(s), connection="
              + connection);

    long sizeUncompressed = payload.length;

    if (description.compressContent()) payload = deflate(payload);

    final long transferStartTime = System.currentTimeMillis();

    try {
      connection.send(description, payload);
    } catch (IOException e) {
      log.error(
          "failed to send " + description + ", connection=" + connection + ":" + e.getMessage(), e);
      throw e;
    }

    notifyDataSent(
        connection.getMode(),
        payload.length,
        sizeUncompressed,
        System.currentTimeMillis() - transferStartTime);
  }

  private void notifyDataSent(
      final StreamMode mode,
      final long sizeCompressed,
      final long sizeUncompressed,
      final long duration) {

    for (final ITransferListener listener : transferListeners) {
      try {
        listener.sent(mode, sizeCompressed, sizeUncompressed, duration);
      } catch (RuntimeException e) {
        log.error("invoking sent() on listener: " + listener + " failed", e);
      }
    }
  }

  private static byte[] deflate(byte[] input) {

    Deflater compressor = new Deflater(Deflater.DEFLATED);
    compressor.setInput(input);
    compressor.finish();

    ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

    byte[] buf = new byte[CHUNKSIZE];

    while (!compressor.finished()) {
      int count = compressor.deflate(buf);
      bos.write(buf, 0, count);
    }

    return bos.toByteArray();
  }
}
