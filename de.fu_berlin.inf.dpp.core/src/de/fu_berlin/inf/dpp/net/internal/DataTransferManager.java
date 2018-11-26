package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.context.IContextKeyBindings.IBBStreamService;
import de.fu_berlin.inf.dpp.context.IContextKeyBindings.Socks5StreamService;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.IPacketInterceptor;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransferListener;
import de.fu_berlin.inf.dpp.net.stream.IStreamService;
import de.fu_berlin.inf.dpp.net.stream.StreamMode;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Nullable;

/**
 * This class is responsible for handling all transfers of binary data. It maintains a map of
 * established connections and tries to reuse them.
 *
 * @author srossbach
 * @author coezbek
 * @author jurke
 */
@Component(module = "net")
public class DataTransferManager implements IConnectionListener, IConnectionManager {

  private static final Logger LOG = Logger.getLogger(DataTransferManager.class);

  private static final int CHUNKSIZE = 16 * 1024;

  private static final String DEFAULT_CONNECTION_ID = "default";

  private static final String IN = "in";

  private static final String OUT = "out";

  private final CopyOnWriteArrayList<IPacketInterceptor> packetInterceptors =
      new CopyOnWriteArrayList<IPacketInterceptor>();

  private final List<ITransferListener> transferListeners =
      new CopyOnWriteArrayList<ITransferListener>();

  private volatile JID currentLocalJID;

  private Connection xmppConnection;

  private int serviceMask = -1;

  private final IReceiver receiver;

  private final IStreamService mainService;

  private final IStreamService fallbackService;

  private final Lock connectLock = new ReentrantLock();

  private final ConnectionPool connectionPool = new ConnectionPool();

  private final Set<String> currentOutgoingConnectionEstablishments = new HashSet<String>();

  private final List<IStreamService> streamServices = new CopyOnWriteArrayList<IStreamService>();

  private final IByteStreamConnectionListener byteStreamConnectionListener =
      new IByteStreamConnectionListener() {

        @Override
        public void receive(final BinaryXMPPExtension extension) {

          boolean dispatchPacket = true;

          for (IPacketInterceptor packetInterceptor : packetInterceptors)
            dispatchPacket &= packetInterceptor.receivedPacket(extension);

          if (!dispatchPacket) return;

          if (LOG.isTraceEnabled())
            LOG.trace(
                "received binary XMPP extension: "
                    + extension.getTransferDescription()
                    + ", size: "
                    + extension.getCompressedSize()
                    + ", RX time: "
                    + extension.getTransferDuration()
                    + " ms ["
                    + extension.getTransferMode()
                    + "]");

          if (extension.getTransferDescription().compressContent()) {
            byte[] payload = extension.getPayload();
            long compressedPayloadLength = payload.length;

            try {
              payload = inflate(payload);
            } catch (IOException e) {
              LOG.error("could not decompress extension payload", e);
              return;
            }

            extension.setPayload(compressedPayloadLength, payload);
          }

          notifyDataReceived(
              extension.getTransferMode(),
              extension.getCompressedSize(),
              extension.getUncompressedSize(),
              extension.getTransferDuration());

          receiver.processBinaryXMPPExtension(extension);
        }

        @Override
        public void connectionChanged(
            final String connectionID,
            final IByteStreamConnection connection,
            final boolean incomingRequest) {

          // FIXME init first, than add to pool and finally start the receiver
          // thread !

          final String id =
              toConnectionIDToken(
                  connectionID, incomingRequest ? IN : OUT, connection.getRemoteAddress());

          LOG.debug(
              "bytestream connection changed "
                  + connection
                  + ", inc="
                  + incomingRequest
                  + ", pool id="
                  + id
                  + "]");

          /*
           * this may return the current connection if the pool is closed so
           * close it anyway
           */
          final IByteStreamConnection current = connectionPool.add(id, connection);

          if (current != null) {
            current.close();
            if (current == connection) {
              LOG.warn(
                  "closed connection [pool id="
                      + id
                      + "]: "
                      + current
                      + " , no connections are currently allowed");

              return;
            } else {
              LOG.warn(
                  "existing connection [pool id="
                      + id
                      + "] "
                      + current
                      + " was replaced with connection "
                      + connection);
            }
          }

          connection.initialize();
        }

        @Override
        public void connectionClosed(String connectionID, IByteStreamConnection connection) {
          closeConnection(connectionID, connection.getRemoteAddress());
        }
      };

  public DataTransferManager(
      XMPPConnectionService connectionService,
      IReceiver receiver,
      @Nullable @Socks5StreamService IStreamService mainService,
      @Nullable @IBBStreamService IStreamService fallbackService) {

    this.receiver = receiver;
    this.fallbackService = fallbackService;
    this.mainService = mainService;
    this.setStreamServices();

    connectionService.addListener(this);
  }

  @Override
  public void addTransferListener(ITransferListener listener) {
    transferListeners.add(listener);
  }

  @Override
  public void removeTransferListener(ITransferListener listener) {
    transferListeners.remove(listener);
  }

  public void sendData(
      final String connectionID, final TransferDescription description, final byte[] data)
      throws IOException {

    final JID connectionJID = currentLocalJID;

    if (connectionJID == null) throw new IOException("not connected to a XMPP server");

    final IByteStreamConnection connection =
        getCurrentConnection(connectionID, description.getRecipient());

    if (connection == null)
      throw new IOException(
          "not connected to "
              + description.getRecipient()
              + " [connection identifier="
              + connectionID
              + "]");

    description.setSender(connectionJID);

    if (LOG.isTraceEnabled())
      LOG.trace(
          "send "
              + description
              + ", data len="
              + data.length
              + " byte(s), connection="
              + connection);

    sendInternal(connectionID, connection, description, data);
  }

  /**
   * @deprecated establishes connections on demand
   * @param transferDescription
   * @param payload
   * @throws IOException
   */
  @Deprecated
  public void sendData(TransferDescription transferDescription, byte[] payload) throws IOException {

    JID connectionJID = currentLocalJID;

    if (connectionJID == null) throw new IOException("not connected to a XMPP server");

    if (LOG.isTraceEnabled())
      LOG.trace(
          "sending data ... from " + connectionJID + " to " + transferDescription.getRecipient());

    JID recipient = transferDescription.getRecipient();
    transferDescription.setSender(connectionJID);

    sendInternal(
        DEFAULT_CONNECTION_ID,
        connectInternal(DEFAULT_CONNECTION_ID, recipient),
        transferDescription,
        payload);
  }

  private void sendInternal(
      final String connectionID,
      final IByteStreamConnection connection,
      final TransferDescription description,
      byte[] payload)
      throws IOException {

    boolean sendPacket = true;

    for (IPacketInterceptor packetInterceptor : packetInterceptors)
      sendPacket &= packetInterceptor.sendPacket(connectionID, description, payload);

    if (!sendPacket) return;

    long sizeUncompressed = payload.length;

    if (description.compressContent()) payload = deflate(payload);

    final long transferStartTime = System.currentTimeMillis();

    try {
      connection.send(description, payload);
    } catch (IOException e) {
      LOG.error(
          "failed to send " + description + ", connection=" + connection + ":" + e.getMessage(), e);
      throw e;
    }

    notifyDataSent(
        connection.getMode(),
        payload.length,
        sizeUncompressed,
        System.currentTimeMillis() - transferStartTime);
  }

  /** @deprecated */
  @Override
  @Deprecated
  public void connect(JID peer) throws IOException {
    connect(DEFAULT_CONNECTION_ID, peer);
  }

  @Override
  public void connect(String connectionID, JID peer) throws IOException {
    if (connectionID == null) throw new NullPointerException("connectionID is null");

    if (peer == null) throw new NullPointerException("peer is null");

    connectInternal(connectionID, peer);
  }

  /**
   * @deprecated Disconnects {@link IByteStreamConnection} with the specified peer
   * @param peer {@link JID} of the peer to disconnect the {@link IByteStreamConnection}
   */
  @Override
  @Deprecated
  public boolean closeConnection(JID peer) {
    return closeConnection(DEFAULT_CONNECTION_ID, peer);
  }

  @Override
  public boolean closeConnection(String connectionIdentifier, JID peer) {

    final String outID = toConnectionIDToken(connectionIdentifier, OUT, peer);

    final String inID = toConnectionIDToken(connectionIdentifier, IN, peer);

    final IByteStreamConnection out = connectionPool.remove(outID);
    final IByteStreamConnection in = connectionPool.remove(inID);

    boolean closed = false;

    if (out != null) {
      closed |= true;
      out.close();
      LOG.debug("closed connection [pool id=" + outID + "]: " + out);
    }

    if (in != null) {
      closed |= true;
      in.close();
      LOG.debug("closed connection [pool id=" + inID + "]: " + in);
    }

    return closed;
  }

  /**
   * {@inheritDoc} The services will be used on the next successful connection to a XMPP server and
   * will not affect the transports that are currently used.
   */
  @Override
  public synchronized void setServices(int transportMask) {
    this.serviceMask = transportMask;
    setStreamServices();
  }

  /** @deprecated */
  @Override
  @Deprecated
  public StreamMode getTransferMode(JID jid) {
    return getTransferMode(null, jid);
  }

  @Override
  public StreamMode getTransferMode(String connectionID, JID jid) {
    IByteStreamConnection connection = getCurrentConnection(connectionID, jid);
    return connection == null ? StreamMode.NONE : connection.getMode();
  }

  private IByteStreamConnection connectInternal(String connectionID, JID peer) throws IOException {

    IByteStreamConnection connection = null;

    final String connectionIDToken = toConnectionIDToken(connectionID, OUT, peer);

    synchronized (currentOutgoingConnectionEstablishments) {
      if (!currentOutgoingConnectionEstablishments.contains(connectionIDToken)) {
        connection = getCurrentConnection(connectionID, peer);

        if (connection == null) currentOutgoingConnectionEstablishments.add(connectionIDToken);
      }

      if (connection != null) return connection;
    }

    connectLock.lock();

    try {

      connection = getCurrentConnection(connectionID, peer);

      if (connection != null) return connection;

      JID connectionJID = currentLocalJID;

      if (connectionJID == null) throw new IOException("not connected to a XMPP server");

      final ArrayList<IStreamService> currentStreamServices =
          new ArrayList<IStreamService>(streamServices);

      for (IStreamService streamService : currentStreamServices) {
        LOG.info(
            "establishing connection to "
                + peer
                + " from "
                + connectionJID
                + " using stream service "
                + streamService);
        try {
          connection = streamService.connect(connectionID, peer);
          break;
        } catch (IOException e) {
          LOG.warn("failed to connect to " + peer + " using stream service: " + streamService, e);
        } catch (InterruptedException e) {
          LOG.warn(
              "interrupted while connecting to "
                  + peer
                  + " using stream service: "
                  + streamService);
          IOException io =
              new InterruptedIOException("connection establishment to " + peer + " aborted");
          io.initCause(e);
          throw io;
        } catch (Exception e) {
          LOG.error(
              "failed to connect to "
                  + peer
                  + " due to an internal error in stream service: "
                  + streamService,
              e);
        }
      }

      if (connection != null) {
        byteStreamConnectionListener.connectionChanged(connectionID, connection, false);

        return connection;
      }

      throw new IOException(
          "could not connect to "
              + peer
              + ", exhausted all available stream services: "
              + currentStreamServices);
    } finally {
      synchronized (currentOutgoingConnectionEstablishments) {
        currentOutgoingConnectionEstablishments.remove(connectionIDToken);
      }
      connectLock.unlock();
    }
  }

  private void setStreamServices() {
    boolean useIBB;
    boolean useSocks5;

    synchronized (this) {
      useIBB = (serviceMask & IBB_SERVICE) != 0;
      useSocks5 = (serviceMask & SOCKS5_SERVICE) != 0;
    }

    streamServices.clear();

    if (useSocks5 && mainService != null) streamServices.add(mainService);

    if (useIBB && fallbackService != null) streamServices.add(fallbackService);

    LOG.debug(
        "used stream service order for the current XMPP connection: "
            + Arrays.toString(streamServices.toArray()));
  }

  /** Sets up the stream services for the given XMPPConnection */
  private void prepareConnection(final Connection connection) {
    assert xmppConnection == null;

    xmppConnection = connection;
    currentLocalJID = new JID(connection.getUser());

    connectionPool.open();

    for (IStreamService streamService : streamServices)
      streamService.initialize(xmppConnection, byteStreamConnectionListener);
  }

  private void disposeConnection() {

    currentLocalJID = null;

    boolean acquired = false;

    try {
      acquired = connectLock.tryLock(5000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      acquired = false;
    }

    try {
      for (IStreamService streamService : streamServices) streamService.uninitialize();
    } finally {
      if (acquired) connectLock.unlock();
    }

    connectionPool.close();
    xmppConnection = null;
  }

  @Override
  public void connectionStateChanged(Connection connection, ConnectionState newState) {
    if (newState == ConnectionState.CONNECTED) prepareConnection(connection);
    else if (this.xmppConnection != null) disposeConnection();
  }

  // TODO: move to ITransmitter
  public void addPacketInterceptor(IPacketInterceptor interceptor) {
    packetInterceptors.addIfAbsent(interceptor);
  }

  // TODO: move to IReceiver
  public void removePacketInterceptor(IPacketInterceptor interceptor) {
    packetInterceptors.remove(interceptor);
  }

  /**
   * Left over and <b>MUST</b> only used by the STF
   *
   * @param extension
   * @deprecated
   */
  @Deprecated
  public void addIncomingTransferObject(BinaryXMPPExtension extension) {
    byteStreamConnectionListener.receive(extension);
  }

  /**
   * Returns the current connection for the remote side. If the local side is connected to the
   * remote side as well as the remote side is connected to the local side the local to remote
   * connection will be returned.
   *
   * @param connectionID identifier for the connection to retrieve or <code>null</code> to retrieve
   *     the default one
   * @param jid JID of the remote side
   * @return the connection to the remote side or <code>null</code> if no connection exists
   */
  private IByteStreamConnection getCurrentConnection(String connectionID, JID jid) {

    IByteStreamConnection connection;

    connection = connectionPool.get(toConnectionIDToken(connectionID, OUT, jid));

    if (connection != null) return connection;

    return connectionPool.get(toConnectionIDToken(connectionID, IN, jid));
  }

  private static String toConnectionIDToken(String connectionIdentifier, String mode, JID jid) {

    if (connectionIdentifier == null) connectionIdentifier = DEFAULT_CONNECTION_ID;

    return connectionIdentifier + ":" + mode + ":" + jid.toString();
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
        LOG.error("invoking sent() on listener: " + listener + " failed", e);
      }
    }
  }

  private void notifyDataReceived(
      final StreamMode mode,
      final long sizeCompressed,
      final long sizeUncompressed,
      final long duration) {

    for (final ITransferListener listener : transferListeners) {
      try {
        listener.received(mode, sizeCompressed, sizeUncompressed, duration);
      } catch (RuntimeException e) {
        LOG.error("invoking received() on listener: " + listener + " failed", e);
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

  private static byte[] inflate(byte[] input) throws IOException {

    ByteArrayOutputStream bos;
    Inflater decompressor = new Inflater();

    decompressor.setInput(input, 0, input.length);
    bos = new ByteArrayOutputStream(input.length);

    byte[] buf = new byte[CHUNKSIZE];

    try {
      while (!decompressor.finished()) {
        int count = decompressor.inflate(buf);
        bos.write(buf, 0, count);
      }
      return bos.toByteArray();
    } catch (DataFormatException e) {
      throw new IOException("failed to inflate data", e);
    }
  }
}
