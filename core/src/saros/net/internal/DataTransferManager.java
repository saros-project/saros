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
import saros.annotations.Component;
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

/**
 * This class is responsible for handling all transfers of binary data. It maintains a map of
 * established connections and tries to reuse them.
 */
@Component(module = "net")
public class DataTransferManager implements IConnectionListener, IConnectionManager {

  private static final Logger LOG = Logger.getLogger(DataTransferManager.class);

  // package private for testing purposes
  static final String DEFAULT_CONNECTION_ID = "default";

  private static final String STREAM_SUFFIX = "-stream";

  private static final String IN = "in";

  private static final String OUT = "out";

  private volatile JID currentLocalJID;

  private Connection xmppConnection;

  private int serviceMask = -1;

  private final IStreamService mainService;

  private final IStreamService fallbackService;

  private final Lock connectLock = new ReentrantLock();

  private final ConnectionPool connectionPool = new ConnectionPool();

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

  private final IConnectionClosedCallback connectionClosedCallback =
      (connection) -> closeConnection(connection.getId(), connection.getRemoteAddress());

  public DataTransferManager(
      XMPPConnectionService connectionService,
      @Nullable @Socks5StreamService IStreamService mainService,
      @Nullable @IBBStreamService IStreamService fallbackService) {

    this.fallbackService = fallbackService;
    this.mainService = mainService;
    this.setStreamServices();

    connectionService.addListener(this);
  }

  /** @deprecated */
  @Override
  @Deprecated
  public IConnection connect(Object address) throws IOException {
    return connect(DEFAULT_CONNECTION_ID, address);
  }

  @Override
  public IStreamConnection connectStream(final String id, Object address) throws IOException {
    Objects.requireNonNull(id, "id is null");
    Objects.requireNonNull(address, "address is null");

    final JID jid = (JID) address;

    if (jid.isBareJID()) throw new IllegalStateException("cannot connect to a bare JID: " + jid);

    return (IStreamConnection) connectInternal(id + STREAM_SUFFIX, jid);
  }

  @Override
  public IConnection connect(String connectionID, Object address) throws IOException {
    if (connectionID == null) throw new NullPointerException("connectionID is null");

    if (address == null) throw new NullPointerException("peer is null");

    return connectInternal(connectionID, (JID) address);
  }

  public IConnection getConnection(final String connectionId, final Object address) {
    return getCurrentConnection(connectionId, address);
  }

  @Override
  @Deprecated
  public boolean closeConnection(Object address) {
    return closeConnection(DEFAULT_CONNECTION_ID, address);
  }

  @Override
  public boolean closeConnection(String connectionIdentifier, Object address) {

    final String outID = toConnectionIDToken(connectionIdentifier, OUT, address);

    final String inID = toConnectionIDToken(connectionIdentifier, IN, address);

    final IConnection out = connectionPool.remove(outID);
    final IConnection in = connectionPool.remove(inID);

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
  public StreamMode getTransferMode(String connectionID, Object address) {
    IConnection connection = getCurrentConnection(connectionID, address);
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

  private IConnection connectInternal(String connectionID, JID peer) throws IOException {

    IConnection connection = null;

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

      ByteStream byteStream = null;

      for (IStreamService streamService : currentStreamServices) {
        LOG.info(
            "establishing connection to "
                + peer
                + " from "
                + connectionJID
                + " using stream service "
                + streamService);
        try {
          byteStream = streamService.connect(connectionID, peer);
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

      if (byteStream != null) {
        if (byteStream.getId().endsWith(STREAM_SUFFIX))
          return createAndAnnounceStreamConnection(byteStream, false);
        else return createAndAnnouncePacketConnection(byteStream, false);
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

    connectionPool.close();
    xmppConnection = null;
  }

  @Override
  public void connectionStateChanged(Connection connection, ConnectionState newState) {
    if (newState == ConnectionState.CONNECTED) prepareConnection(connection);
    else if (this.xmppConnection != null) disposeConnection();
  }

  /**
   * Returns the current connection for the remote side. If the local side is connected to the
   * remote side as well as the remote side is connected to the local side the local to remote
   * connection will be returned.
   *
   * @param connectionID identifier for the connection to retrieve or <code>null</code> to retrieve
   *     the default one
   * @param address address of the remote side
   * @return the connection to the remote side or <code>null</code> if no connection exists
   */
  private IConnection getCurrentConnection(String connectionID, Object address) {

    IConnection connection;

    connection = connectionPool.get(toConnectionIDToken(connectionID, OUT, address));

    if (connection != null) return connection;

    return connectionPool.get(toConnectionIDToken(connectionID, IN, address));
  }

  private static String toConnectionIDToken(
      String connectionIdentifier, String mode, Object address) {

    if (connectionIdentifier == null) connectionIdentifier = DEFAULT_CONNECTION_ID;

    return connectionIdentifier + ":" + mode + ":" + address;
  }

  private IConnection createAndAnnouncePacketConnection(
      final ByteStream byteStream, final boolean isIncoming) throws IOException {
    final BinaryChannelConnection connection =
        new BinaryChannelConnection(byteStream, connectionClosedCallback);

    // FIXME init first, than add to pool and finally start the receiver
    // thread !

    addConnectionToPool(connection, isIncoming);

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
      connectionPool.remove(connection.getId());

      throw e;
    }

    return connection;
  }

  private IConnection createAndAnnounceStreamConnection(
      final ByteStream byteStream, final boolean isIncoming) throws IOException {

    final DefaultStreamConnection connection =
        new DefaultStreamConnection(byteStream, connectionClosedCallback);

    addConnectionToPool(connection, isIncoming);

    if (!isIncoming) return connection;

    boolean accepted = false;

    final String connectionId =
        connection.getId().substring(0, connection.getId().length() - STREAM_SUFFIX.length());

    for (final IStreamConnectionListener listener : streamConnectionListeners) {
      try {
        accepted |= listener.streamConnectionEstablished(connectionId, connection);
      } catch (RuntimeException e) {
        LOG.error("invoking streamConnectionEstablished() on listener: " + listener + " failed", e);
      }
    }

    if (!accepted) closeConnection(connection.getId(), connection.getRemoteAddress());

    throw new IOException("no listener accepted the connection: " + connection);
  }

  private IConnection addConnectionToPool(final IConnection connection, final boolean isIncoming)
      throws IOException {

    final String id =
        toConnectionIDToken(
            connection.getId(), isIncoming ? IN : OUT, connection.getRemoteAddress());

    LOG.debug(
        "bytestream connection changed "
            + connection
            + ", inc="
            + isIncoming
            + ", pool id="
            + id
            + "]");

    /*
     * this may return the current connection if the pool is closed so
     * close it anyway
     */
    final IConnection current = connectionPool.add(id, connection);

    if (current != null) {
      current.close();
      if (current == connection) {
        throw new IOException(
            "closed connection [pool id="
                + id
                + "]: "
                + current
                + " , no connections are currently allowed");
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

    return connection;
  }
}
