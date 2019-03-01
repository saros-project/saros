package saros.net.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import saros.net.stream.ByteStream;
import saros.net.stream.StreamMode;
import saros.net.xmpp.JID;

/**
 * BinaryChannelConnection is a class that encapsulates a bidirectional communication channel
 * between two participants.
 *
 * <p>The threading requirements of this class are the following:
 *
 * <p>send() is a reentrant method for sending data. Any number of threads can call it in parallel.
 * <b>Note:</b> The maximum number of concurrent threads is 32 !
 *
 * @author sszuecs
 * @author coezbek
 * @author srossbach
 */
public class BinaryChannelConnection implements IByteStreamConnection {

  private static final Logger LOG = Logger.getLogger(BinaryChannelConnection.class);

  private static final long TERMINATE_TIMEOUT = 10000L;

  private static class Opcode {
    /* these opcodes will be cropped to byte values, do not exceed 0xFF ! */

    private static final int TRANSFERDESCRIPTION = 0xFA;
    private static final int DATA = 0xFB;

    private static final int NAMESPACE_UPDATE = 0x64;
    private static final int ELEMENT_NAME_UPDATE = 0x65;
  }

  /** Max size of data chunks */
  private static final int CHUNKSIZE = 32 * 1024 - 1;

  private IByteStreamConnectionListener listener;
  private ReceiverThread receiveThread;

  private final JID remoteAddress;
  private final JID localAddress;

  private final String connectionID;

  private IDPool idPool = new IDPool();

  private boolean connected;
  private boolean initialized;

  private Map<Integer, ByteArrayOutputStream> pendingFragmentedPackets =
      new HashMap<Integer, ByteArrayOutputStream>();
  private Map<Integer, BinaryXMPPExtension> pendingXMPPExtensions =
      new HashMap<Integer, BinaryXMPPExtension>();

  private DataInputStream inputStream;
  private DataOutputStream outputStream;

  private ByteStream stream;
  private StreamMode mode;

  private Map<Integer, String> inNamespaceCache = new HashMap<Integer, String>();
  private Map<String, Integer> outNamespaceCache = new HashMap<String, Integer>();

  private Map<Integer, String> inElementNameCache = new HashMap<Integer, String>();
  private Map<String, Integer> outElementNameCache = new HashMap<String, Integer>();

  private int nextNamespaceId = 0;
  private int nextElementNameId = 0;

  private class ReceiverThread extends Thread {

    @Override
    public void run() {
      String connection = BinaryChannelConnection.this.toString();

      LOG.debug(connection + " ReceiverThread started.");
      try {
        while (!isInterrupted()) listener.receive(readNextXMPPExtension());

      } catch (SocketException e) {
        LOG.debug(connection + " connection closed locally: " + e.getMessage());
      } catch (EOFException e) {
        LOG.debug(connection + " connection closed remotely:" + e.getMessage());
      } catch (IOException e) {
        LOG.error(connection + " network error: " + e.getMessage(), e);
      } catch (Exception e) {
        LOG.error(connection + " internal error: " + e.getMessage(), e);
      } finally {
        close();
      }
    }
  }

  public BinaryChannelConnection(
      JID localAddress,
      JID remoteAddress,
      String connectionID,
      ByteStream stream,
      StreamMode mode,
      IByteStreamConnectionListener listener)
      throws IOException {
    this.listener = listener;
    this.localAddress = localAddress;
    this.remoteAddress = remoteAddress;
    this.connectionID = connectionID;
    this.stream = stream;
    this.stream.setReadTimeout(0); // keep connection alive
    this.mode = mode;

    outputStream = new DataOutputStream(new BufferedOutputStream(stream.getOutputStream()));
    inputStream = new DataInputStream(new BufferedInputStream(stream.getInputStream()));
  }

  @Override
  public synchronized void initialize() {
    if (initialized) return;

    /*
     * it is ok to start the receiver a bit later because the data will be
     * already buffered by SMACK or the OS
     */
    receiveThread = new ReceiverThread();
    receiveThread.setName("BinaryChannel-" + remoteAddress.getName());
    receiveThread.start();
    connected = true;
    initialized = true;
  }

  @Override
  public String getConnectionID() {
    return connectionID;
  }

  @Override
  public synchronized boolean isConnected() {
    return connected;
  }

  @Override
  public void close() {
    synchronized (this) {
      if (!isConnected()) return;

      try {
        stream.close();
      } catch (Exception e) {
        LOG.error("failed to gracefully close connection " + this, e);
      } finally {
        connected = false;
      }
    }

    assert receiveThread != null;

    if (Thread.currentThread() != receiveThread) {
      try {
        receiveThread.join(TERMINATE_TIMEOUT);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      if (receiveThread.isAlive()) {
        LOG.warn("timeout while waiting for closure of binary channel " + this);
        receiveThread.interrupt();
      }
    }

    listener.connectionClosed(connectionID, this);
  }

  @Override
  public StreamMode getMode() {
    return mode;
  }

  @Override
  public JID getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public void send(TransferDescription data, byte[] content) throws IOException {

    if (!isConnected()) throw new EOFException("connection is closed");

    final int fragmentId = idPool.nextID();

    if (fragmentId < 0) throw new IOException("concurrent access threshold exceeded");

    try {

      Integer namespaceId;
      Integer elementNameId;

      synchronized (this) {
        boolean sendUpdate = false;

        final String namespace = data.getNamespace();
        namespaceId = outNamespaceCache.get(namespace);

        if (namespaceId == null) {
          if (nextNamespaceId > 255) throw new IOException("namespace cache limit exceeded");

          namespaceId = Integer.valueOf(nextNamespaceId++);
          outNamespaceCache.put(namespace, namespaceId);

          if (LOG.isTraceEnabled())
            LOG.trace(
                "updated outgoing namespace cache, id: "
                    + namespaceId
                    + " , namespace: "
                    + namespace);

          outputStream.write(Opcode.NAMESPACE_UPDATE);
          outputStream.write(namespaceId);
          outputStream.writeUTF(namespace);
          sendUpdate = true;
        }

        final String elementName = data.getElementName();
        elementNameId = outElementNameCache.get(elementName);

        if (elementNameId == null) {
          if (nextElementNameId > 65535) throw new IOException("element name cache limit exceeded");

          elementNameId = Integer.valueOf(nextElementNameId++);
          outElementNameCache.put(elementName, elementNameId);

          if (LOG.isTraceEnabled())
            LOG.trace(
                "updated outgoing element name cache, id: "
                    + elementNameId
                    + " , element name: "
                    + elementName);

          outputStream.write(Opcode.ELEMENT_NAME_UPDATE);
          outputStream.writeShort(elementNameId);
          outputStream.writeUTF(elementName);
          sendUpdate = true;
        }

        if (sendUpdate) outputStream.flush();
      }

      assert content.length > 0;

      int chunks = ((content.length - 1) / CHUNKSIZE) + 1;

      sendTransferDescription(
          fragmentId, chunks, namespaceId, elementNameId, data.compressContent());

      splitAndSend(content, chunks, fragmentId);
    } catch (IOException e) {
      close();
      throw e;
    } finally {
      idPool.freeID(fragmentId);
    }
  }

  /**
   * Reads the next XMPP extension.
   *
   * @return returns the next incoming transfer object
   * @throws IOException If the associated socket broke, while reading or if the socket has already
   *     been disposed.
   */
  private BinaryXMPPExtension readNextXMPPExtension() throws IOException {

    while (!Thread.currentThread().isInterrupted()) {

      final int opcode = inputStream.readUnsignedByte();

      if (opcode == -1) throw new EOFException("no stream data available");

      final int fragmentId;

      final int id;
      final String name;

      switch (opcode) {
        case Opcode.TRANSFERDESCRIPTION:
          fragmentId = inputStream.readShort();
          final int chunks = inputStream.readInt();

          if (LOG.isTraceEnabled()) {
            LOG.trace(
                "processing opcode 0x"
                    + Integer.toHexString(opcode).toUpperCase()
                    + " [TFD]: id="
                    + fragmentId
                    + ", chunks="
                    + chunks);
          }

          final int namespaceId = inputStream.readUnsignedByte();
          final int elementNameId = inputStream.readUnsignedShort();
          final int compressed = inputStream.readUnsignedByte();

          final String namespace = inNamespaceCache.get(Integer.valueOf(namespaceId));

          final String elementName = inElementNameCache.get(Integer.valueOf(elementNameId));

          final TransferDescription transferDescription = TransferDescription.newDescription();

          transferDescription.setSender(remoteAddress);
          transferDescription.setRecipient(localAddress);
          transferDescription.setNamespace(namespace);
          transferDescription.setElementName(elementName);
          transferDescription.setCompressContent(compressed == 1);

          BinaryXMPPExtension oldTransferObject =
              pendingXMPPExtensions.put(
                  fragmentId, new BinaryXMPPExtension(mode, transferDescription, chunks));

          if (oldTransferObject != null)
            throw new IOException("replaced a XMPP extension that is still transmitted");
          break;

        case Opcode.DATA:
          fragmentId = inputStream.readShort();
          final int payloadLength = inputStream.readInt();

          if (LOG.isTraceEnabled()) {
            LOG.trace(
                "processing opcode 0x"
                    + Integer.toHexString(opcode).toUpperCase()
                    + " [DATA]: id="
                    + fragmentId
                    + ", DATA len="
                    + payloadLength
                    + " bytes");
          }

          if (payloadLength <= 0 || payloadLength > CHUNKSIZE)
            throw new ProtocolException(
                "payload length field contains corrupted value: 0 < "
                    + payloadLength
                    + " <= "
                    + CHUNKSIZE);

          byte[] payload = new byte[payloadLength];
          inputStream.readFully(payload);

          ByteArrayOutputStream out = pendingFragmentedPackets.get(fragmentId);

          if (out == null) {
            out = new ByteArrayOutputStream(payloadLength * 2);
            pendingFragmentedPackets.put(fragmentId, out);
          }

          out.write(payload);
          out.flush();

          if (!pendingXMPPExtensions.get(fragmentId).isLastChunk()) break;

          pendingFragmentedPackets.remove(fragmentId);

          BinaryXMPPExtension fullyReceivedTransferObject =
              pendingXMPPExtensions.remove(fragmentId);

          payload = out.toByteArray();
          out = null; // help GC

          fullyReceivedTransferObject.setPayload(payload.length, payload);

          return fullyReceivedTransferObject;

        case Opcode.ELEMENT_NAME_UPDATE:
          if (LOG.isTraceEnabled()) {
            LOG.trace(
                "processing opcode 0x" + Integer.toHexString(opcode).toUpperCase() + " [ENU]");
          }

          id = inputStream.readUnsignedShort();
          name = inputStream.readUTF();
          inElementNameCache.put(Integer.valueOf(id), name);

          if (LOG.isTraceEnabled())
            LOG.trace(
                "updated incoming element name cache, id: " + id + " , element name: " + name);

          break;

        case Opcode.NAMESPACE_UPDATE:
          if (LOG.isTraceEnabled()) {
            LOG.trace(
                "processing opcode 0x" + Integer.toHexString(opcode).toUpperCase() + " [NSU]");
          }

          id = inputStream.readUnsignedByte();
          name = inputStream.readUTF();
          inNamespaceCache.put(Integer.valueOf(id), name);

          if (LOG.isTraceEnabled())
            LOG.trace("updated incoming namespace cache, id: " + id + " , namespace: " + name);

          break;

        default:
          close();
          throw new ProtocolException(
              "unknown opcode: 0x" + Integer.toHexString(opcode).toUpperCase());
      }
    }

    // clear the interrupt flag
    Thread.interrupted();
    throw new InterruptedIOException("interrupted while reading stream data");
  }

  private synchronized void sendData(int fragmentId, byte[] data, int offset, int length)
      throws IOException {

    if (LOG.isTraceEnabled()) {
      LOG.trace("sending data: id=" + fragmentId + ", len=" + length + " bytes");
    }

    outputStream.write(Opcode.DATA);
    outputStream.writeShort(fragmentId);
    outputStream.writeInt(length);
    outputStream.write(data, offset, length);
    outputStream.flush();
  }

  private synchronized void sendTransferDescription(
      int fragmentId, int chunks, int namespaceId, int elementNameId, boolean compress)
      throws IOException {

    if (LOG.isTraceEnabled()) {
      LOG.trace("sending transfer description: id=" + fragmentId);
    }

    outputStream.write(Opcode.TRANSFERDESCRIPTION);
    outputStream.writeShort(fragmentId);
    outputStream.writeInt(chunks);
    outputStream.write(namespaceId);
    outputStream.writeShort(elementNameId);
    outputStream.write(compress ? 1 : 0);
    outputStream.flush();
  }

  /** Splits the given data into chunks of CHUNKSIZE to send the BinaryPackets. */
  private void splitAndSend(byte[] data, int chunks, int fragmentId) throws IOException {

    int offset = 0;
    int length = 0;

    while (chunks-- > 0) {

      length = Math.min(data.length - offset, CHUNKSIZE);

      sendData(fragmentId, data, offset, length);

      offset += length;
    }
  }

  @Override
  public String toString() {
    return "[mode=" + getMode() + ", id=" + connectionID + "]" + " " + remoteAddress;
  }

  static class IDPool {

    private final int MAX_ID = 32;
    private int pool = 0; // 32 ids

    // see
    // http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogDeBruijn
    private static final int LOG_2_TABLE[] = {
      0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26,
      12, 18, 6, 11, 5, 10, 9
    };

    public synchronized int nextID() {

      final int bitIdx = Integer.lowestOneBit(~pool);

      if (bitIdx == 0) return -1;

      pool |= bitIdx;

      return LOG_2_TABLE[(bitIdx * 0x077CB531) >>> 27];
    }

    public synchronized void freeID(int id) {
      if (id < 0 || id >= MAX_ID) return;

      int bitIdx = 1 << id;
      pool &= (~bitIdx);
    }
  }
}
