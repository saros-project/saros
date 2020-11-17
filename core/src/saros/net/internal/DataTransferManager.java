package saros.net.internal;

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
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import saros.annotations.Component;
import saros.context.IContextKeyBindings.IBBStreamService;
import saros.context.IContextKeyBindings.Socks5StreamService;
import saros.net.ConnectionState;
import saros.net.IConnectionManager;
import saros.net.stream.IStreamService;
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

  private static final Logger log = Logger.getLogger(DataTransferManager.class);

  private static final String DEFAULT_CONNECTION_ID = "default";

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

  private final CopyOnWriteArrayList<IByteStreamConnectionListener> connectionListeners =
      new CopyOnWriteArrayList<>();

  private final IByteStreamConnectionListener byteStreamConnectionListener =
      new IByteStreamConnectionListener() {

        @Override
        public void connectionChanged(
            final String connectionId,
            final IByteStreamConnection connection,
            final boolean incomingRequest) {

          // FIXME init first, than add to pool and finally start the receiver
          // thread !

          final String id =
              toConnectionIDToken(
                  connectionId, incomingRequest ? IN : OUT, connection.getRemoteAddress());

          /// TODO we currently have to announce not initialized connections otherwise the IReceiver
          // will miss updates

          notfiyconnectionChanged(id, connection, incomingRequest);

          log.debug(
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
              log.warn(
                  "closed connection [pool id="
                      + id
                      + "]: "
                      + current
                      + " , no connections are currently allowed");

              return;
            } else {
              log.warn(
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
        public void connectionClosed(
            final String connectionId, final IByteStreamConnection connection) {
          closeConnection(connectionId, connection.getRemoteAddress());
          notfiyConnectionClosed(connectionId, connection);
        }
      };

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
  public IByteStreamConnection connect(JID peer) throws IOException {
    return connect(DEFAULT_CONNECTION_ID, peer);
  }

  @Override
  public IByteStreamConnection connect(String connectionID, JID peer) throws IOException {
    if (connectionID == null) throw new NullPointerException("connectionID is null");

    if (peer == null) throw new NullPointerException("peer is null");

    return connectInternal(connectionID, peer);
  }

  public IByteStreamConnection getConnection(final String connectionId, final JID peer) {
    return getCurrentConnection(connectionId, peer);
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
      log.debug("closed connection [pool id=" + outID + "]: " + out);
    }

    if (in != null) {
      closed |= true;
      in.close();
      log.debug("closed connection [pool id=" + inID + "]: " + in);
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

  public void addConnectionListener(final IByteStreamConnectionListener listener) {
    connectionListeners.addIfAbsent(listener);
  }

  public void removeConnectionListener(final IByteStreamConnectionListener listener) {
    connectionListeners.remove(listener);
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
        log.info(
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
          log.warn("failed to connect to " + peer + " using stream service: " + streamService, e);
        } catch (InterruptedException e) {
          log.warn(
              "interrupted while connecting to "
                  + peer
                  + " using stream service: "
                  + streamService);
          IOException io =
              new InterruptedIOException("connection establishment to " + peer + " aborted");
          io.initCause(e);
          throw io;
        } catch (Exception e) {
          log.error(
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

    log.debug(
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

  private void notfiyConnectionClosed(
      final String connectionId, final IByteStreamConnection connection) {

    for (final IByteStreamConnectionListener listener : connectionListeners) {
      try {
        listener.connectionClosed(connectionId, connection);
      } catch (RuntimeException e) {
        log.error("invoking connectionClosed() on listener: " + listener + " failed", e);
      }
    }
  }

  private void notfiyconnectionChanged(
      final String connectionId,
      final IByteStreamConnection connection,
      final boolean incomingRequest) {

    for (final IByteStreamConnectionListener listener : connectionListeners) {
      try {
        listener.connectionChanged(connectionId, connection, incomingRequest);
      } catch (RuntimeException e) {
        log.error("invoking connectionChanged() on listener: " + listener + " failed", e);
      }
    }
  }
}
