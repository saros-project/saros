package saros.net.internal;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import saros.context.IContextKeyBindings.IBBStreamService;
import saros.context.IContextKeyBindings.Socks5StreamService;
import saros.net.ConnectionState;
import saros.net.IConnectionManager;
import saros.net.IStreamConnection;
import saros.net.IStreamConnectionListener;
import saros.net.stream.ByteStream;
import saros.net.stream.IStreamService;
import saros.net.stream.IStreamServiceListener;
import saros.net.stream.StreamMode;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.repackaged.picocontainer.annotations.Nullable;

public class DataTransferManager implements IConnectionListener, IConnectionManager {

  private static final Logger LOG = Logger.getLogger(DataTransferManager.class);

  private static final String STREAM_SUFFIX = "-stream";

  private static final String IN = "in";

  private static final String OUT = "out";

  private volatile JID currentLocalJID;

  private Connection xmppConnection;

  private int serviceMask = -1;

  private final IStreamService mainService;

  private final IStreamService fallbackService;

  private final Lock connectLock = new ReentrantLock();

  private final ConnectionPool packetConnectionPool = new ConnectionPool("packet");
  private final ConnectionPool streamConnectionPool = new ConnectionPool("stream");

  private final Set<String> currentOutgoingConnectionEstablishments = new HashSet<String>();

  private final List<IStreamService> streamServices = new CopyOnWriteArrayList<IStreamService>();

  private final CopyOnWriteArrayList<IPacketConnectionListener> packetConnectionListeners =
      new CopyOnWriteArrayList<>();

  private final CopyOnWriteArrayList<IStreamConnectionListener> streamConnectionListeners =
      new CopyOnWriteArrayList<>();

  private final IStreamServiceListener streamServiceListener =
      new IStreamServiceListener() {

        @Override
        public void connectionEstablished(final ByteStream byteStream) {

          try {
            if (byteStream.getId().endsWith(STREAM_SUFFIX)) {
              createAndAnnounceStreamConnection(byteStream, true);
            } else {
              createAndAnnouncePacketConnection(byteStream, true);
            }
          } catch (IOException e) {
            LOG.error("failed to accept byte stream: " + byteStream, e);
          }
        }
      };

  private final IConnectionClosedCallback packetConnectionClosedCallback =
      (connection) -> closeConnection(connection.getId(), connection.getRemoteAddress());

  private final IConnectionClosedCallback localStreamConnectionClosedCallback =
      (connection) ->
          closeStreamConnection(connection.getId(), connection.getRemoteAddress(), false);

  private final IConnectionClosedCallback remoteStreamConnectionClosedCallback =
      (connection) ->
          closeStreamConnection(connection.getId(), connection.getRemoteAddress(), true);

  public DataTransferManager(
      XMPPConnectionService connectionService,
      @Nullable @Socks5StreamService IStreamService mainService,
      @Nullable @IBBStreamService IStreamService fallbackService) {

    this.fallbackService = fallbackService;
    this.mainService = mainService;
    this.setStreamServices();

    connectionService.addListener(this);
  }

  @Override
  public IStreamConnection connectStream(final String id, Object address) throws IOException {
    Objects.requireNonNull(id, "id is null");
    Objects.requireNonNull(address, "address is null");

    return (IStreamConnection) connectInternal(id, (JID) address, true);
  }

  @Override
  public void connect(final String id, Object address) throws IOException {
    Objects.requireNonNull(id, "id is null");
    Objects.requireNonNull(address, "address is null");

    if (id.endsWith(STREAM_SUFFIX))
      throw new IllegalArgumentException(
          "connection id " + id + " must not end with " + STREAM_SUFFIX);

    connectInternal(id, (JID) address, false);
  }

  /**
   * Returns the current packet connection for the remote side. If the local side is connected to
   * the remote side as well as the remote side is connected to the local side the local to remote
   * connection will be returned.
   *
   * @param id identifier for the connection to retrieve
   * @param address address of the remote side
   * @return the connection to the remote side or <code>null</code> if no connection exists
   */
  public IPacketConnection getPacketConnection(final String id, final Object address) {

    IPacketConnection connection;

    connection =
        (IPacketConnection) packetConnectionPool.get(toConnectionIdToken(id, OUT, address));

    if (connection != null) return connection;

    return (IPacketConnection) packetConnectionPool.get(toConnectionIdToken(id, IN, address));
  }

  @Override
  public boolean closeConnection(final String id, final Object address) {

    final String outID = toConnectionIdToken(id, OUT, address);

    final String inID = toConnectionIdToken(id, IN, address);

    final IConnection out = packetConnectionPool.remove(outID);
    final IConnection in = packetConnectionPool.remove(inID);

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

  @Override
  public StreamMode getTransferMode(String connectionID, Object address) {
    IConnection connection = getPacketConnection(connectionID, address);
    return connection == null ? StreamMode.NONE : connection.getMode();
  }

  public void addPacketConnectionListener(final IPacketConnectionListener listener) {
    packetConnectionListeners.addIfAbsent(listener);
  }

  public void removePacketConnectionListener(final IPacketConnectionListener listener) {
    packetConnectionListeners.remove(listener);
  }

  @Override
  public void addStreamConnectionListener(final IStreamConnectionListener listener) {
    streamConnectionListeners.addIfAbsent(listener);
  }

  @Override
  public void removeStreamConnectionListener(final IStreamConnectionListener listener) {
    streamConnectionListeners.remove(listener);
  }

  private IConnection connectInternal(
      final String id, final JID address, final boolean isPlainStream) throws IOException {

    IConnection connection = null;

    final String internalStreamId = isPlainStream ? id + STREAM_SUFFIX : id;

    final String connectionIdToken = toConnectionIdToken(internalStreamId, OUT, address);

    synchronized (currentOutgoingConnectionEstablishments) {
      if (!currentOutgoingConnectionEstablishments.contains(connectionIdToken)) {
        connection = getPacketConnection(id, address);

        if (connection == null) currentOutgoingConnectionEstablishments.add(connectionIdToken);
      }

      if (connection != null) return connection;
    }

    connectLock.lock();

    try {

      if (isPlainStream) {
        connection = getLocalStreamConnection(id, address);

        if (connection != null)
          throw new IOException(
              "a stream connection with id " + id + " is already established: " + connection);
      }

      connection = getPacketConnection(id, address);

      if (connection != null) return connection;

      JID connectionJID = currentLocalJID;

      if (connectionJID == null) throw new IOException("not connected to a XMPP server");

      final ArrayList<IStreamService> currentStreamServices =
          new ArrayList<IStreamService>(streamServices);

      ByteStream byteStream = null;

      for (IStreamService streamService : currentStreamServices) {
        LOG.info(
            "establishing connection to "
                + address
                + " from "
                + connectionJID
                + " using stream service "
                + streamService);
        try {
          byteStream = streamService.connect(internalStreamId, address);
          break;
        } catch (IOException e) {
          LOG.warn(
              "failed to connect to " + address + " using stream service: " + streamService, e);
        } catch (InterruptedException e) {
          LOG.warn(
              "interrupted while connecting to "
                  + address
                  + " using stream service: "
                  + streamService);
          IOException io =
              new InterruptedIOException("connection establishment to " + address + " aborted");
          io.initCause(e);
          throw io;
        } catch (Exception e) {
          LOG.error(
              "failed to connect to "
                  + address
                  + " due to an internal error in stream service: "
                  + streamService,
              e);
        }
      }

      if (byteStream != null) {
        if (isPlainStream) return createAndAnnounceStreamConnection(byteStream, false);
        else return createAndAnnouncePacketConnection(byteStream, false);
      }

      throw new IOException(
          "could not connect to "
              + address
              + ", exhausted all available stream services: "
              + currentStreamServices);
    } finally {
      synchronized (currentOutgoingConnectionEstablishments) {
        currentOutgoingConnectionEstablishments.remove(connectionIdToken);
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

    packetConnectionPool.open();

    for (IStreamService streamService : streamServices)
      streamService.initialize(xmppConnection, streamServiceListener);
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

    packetConnectionPool.close();
    streamConnectionPool.close();
    xmppConnection = null;
  }

  @Override
  public void connectionStateChanged(Connection connection, ConnectionState newState) {
    if (newState == ConnectionState.CONNECTED) prepareConnection(connection);
    else if (this.xmppConnection != null) disposeConnection();
  }

  private IStreamConnection getLocalStreamConnection(final String id, final Object address) {
    return (IStreamConnection) streamConnectionPool.get(toConnectionIdToken(id, OUT, address));
  }

  private void closeStreamConnection(
      final String id, final Object address, final boolean isRemote) {

    final String poolId = toConnectionIdToken(id, isRemote ? IN : OUT, address);

    final IConnection connection = streamConnectionPool.remove(poolId);

    if (connection != null) {
      connection.close();
      LOG.debug("closed connection [pool id=" + poolId + "]: " + connection);
    }
  }

  private IConnection createAndAnnouncePacketConnection(
      final ByteStream byteStream, final boolean isIncoming) throws IOException {
    final BinaryChannelConnection connection =
        new BinaryChannelConnection(byteStream, packetConnectionClosedCallback);

    // FIXME init first, than add to pool
    addConnectionToPool(packetConnectionPool, connection, isIncoming);

    for (final IPacketConnectionListener listener : packetConnectionListeners) {
      try {
        listener.connectionEstablished(connection);
      } catch (RuntimeException e) {
        LOG.error("invoking connectionEstablished() on listener: " + listener + " failed", e);
      }
    }

    try {
      connection.initialize();
    } catch (IOException e) {
      LOG.error("failed to initialize connection [inc=" + isIncoming + "] : " + connection);
      connection.close();
      packetConnectionPool.remove(connection.getId());

      throw e;
    }

    return connection;
  }

  private IConnection createAndAnnounceStreamConnection(
      final ByteStream byteStream, final boolean isRemote) throws IOException {

    final String id = getIdFromIdWithStreamSuffix(byteStream.getId());

    final StreamConnection connection =
        new StreamConnection(
            id,
            byteStream,
            isRemote ? remoteStreamConnectionClosedCallback : localStreamConnectionClosedCallback);

    addConnectionToPool(streamConnectionPool, connection, isRemote);

    if (!isRemote) return connection;

    boolean accepted = false;

    for (final IStreamConnectionListener listener : streamConnectionListeners) {
      try {
        accepted |= listener.connectionEstablished(connection);
        if (accepted) break;
      } catch (RuntimeException e) {
        LOG.error("invoking connectionEstablished() on listener: " + listener + " failed", e);
      }
    }

    if (!accepted) closeConnection(connection.getId(), connection.getRemoteAddress());

    throw new IOException("no listener accepted the connection: " + connection);
  }

  /**
   * Adds the given connection to the given pool.
   *
   * @throws IOException if the pool is closed
   */
  private static IConnection addConnectionToPool(
      final ConnectionPool pool, final IConnection connection, final boolean isRemote)
      throws IOException {

    final String id =
        toConnectionIdToken(connection.getId(), isRemote ? IN : OUT, connection.getRemoteAddress());

    LOG.debug(
        "adding connection "
            + connection
            + " [isRemote="
            + isRemote
            + "] to connection pool "
            + pool
            + " with pool id="
            + id);

    /*
     * this may return the current connection if the pool is closed so
     * close it anyway
     */
    final IConnection current = pool.add(id, connection);

    if (current != null) {
      current.close();
      if (current == connection) {
        throw new IOException("connection pool " + pool + " is closed");
      } else {
        LOG.warn("replaced connection " + connection + " in connection pool " + pool);
      }
    }

    return connection;
  }

  private static String toConnectionIdToken(
      String connectionIdentifier, String mode, Object address) {

    return connectionIdentifier + ":" + mode + ":" + address;
  }

  private static String getIdFromIdWithStreamSuffix(final String id) {
    return id.endsWith(STREAM_SUFFIX) ? id.substring(0, id.length() - STREAM_SUFFIX.length()) : id;
  }
}
