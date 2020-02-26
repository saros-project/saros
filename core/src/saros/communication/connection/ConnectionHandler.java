package saros.communication.connection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import saros.SarosConstants;
import saros.account.XMPPAccount;
import saros.net.ConnectionState;
import saros.net.IConnectionManager;
import saros.net.ResourceFeature;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.preferences.Preferences;

/**
 * Facade for handling connection establishment and connection events. This facade should be
 * preferred over {@link XMPPConnectionService} and <b>should</b> always be used in UI components
 * and widgets.
 */
// TODO better name
public class ConnectionHandler {

  private static final Logger LOG = Logger.getLogger(ConnectionHandler.class);

  private final XMPPConnectionService connectionService;

  private final Preferences preferences;

  private final IConnectionManager connectionManager;

  private volatile IConnectingFailureCallback callback;

  private List<IConnectionStateListener> stateListeners =
      new CopyOnWriteArrayList<IConnectionStateListener>();

  private boolean isConnecting;
  private boolean isDisconnecting;

  private ConnectionState currentConnectionState = ConnectionState.NOT_CONNECTED;
  private Exception currentConnectionError = null;

  private final Object notifyLock = new Object();

  private final IConnectionListener xmppConnectionListener =
      new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection, ConnectionState state) {

          if (state == ConnectionState.CONNECTING) {
            ServiceDiscoveryManager.getInstanceFor(connection)
                .addFeature(ResourceFeature.SAROS.getIdentifier());
          }

          final Exception error =
              state == ConnectionState.ERROR ? connectionService.getConnectionError() : null;

          setConnectionState(state, error, true);
        }
      };

  public ConnectionHandler(
      final XMPPConnectionService connectionService,
      final IConnectionManager transferManager,
      final Preferences preferences) {

    this.connectionService = connectionService;
    this.connectionManager = transferManager;
    this.preferences = preferences;

    this.connectionService.addListener(xmppConnectionListener);
  }

  /** Returns the current connection state. */
  public ConnectionState getConnectionState() {
    synchronized (notifyLock) {
      return currentConnectionState;
    }
  }

  /**
   * Returns the latest connection error.
   *
   * @return the connection error or <code>null</code>
   */
  public Exception getConnectionError() {
    synchronized (notifyLock) {
      return currentConnectionError;
    }
  }

  /**
   * Returns a unique id for the current connection.
   *
   * @return a unique id for the current connection or <code>null</code> if no connection is
   *     established or the connection has no unique id
   */
  public String getConnectionID() {
    final JID jid = connectionService.getJID();

    if (jid == null) return null;

    return jid.toString();
  }

  /**
   * Returns the local JID for the current connection.
   *
   * @return the local JID for the current connection or <code>null</code> if no connection is
   *     established or the connection has no jid
   */
  public JID getLocalJID() {
    return connectionService.getJID();
  }

  /**
   * Checks if a connection is currently established.
   *
   * @return <code>true</code> if a connection is established, <code>false</code> otherwise
   */
  public boolean isConnected() {
    return connectionService.isConnected();
  }

  public void addConnectionStateListener(IConnectionStateListener listener) {
    stateListeners.add(listener);
  }

  public void removeConnectionStateListener(IConnectionStateListener listener) {
    stateListeners.remove(listener);
  }

  /**
   * Connects to the a XMPP server with the given account. If a connection establishment (connect or
   * disconnect) is already in progress this connection attempt will be ignored.
   *
   * <p>If there is already an established connection this connection will be disconnected.
   *
   * @param account the account to connect with
   * @param failSilently if set to <code>true</code> a connection failure will not be reported to
   *     the {@linkplain IConnectingFailureCallback callback}
   * @blocking this method may block for several seconds
   */
  public void connect(final XMPPAccount account, final boolean failSilently) {

    synchronized (this) {
      if (isConnecting || isDisconnecting) return;

      isConnecting = true;
    }

    try {
      connectXMPPInternal(account, failSilently);
    } finally {
      synchronized (this) {
        isConnecting = false;
      }
    }
  }

  // TODO javadoc
  public void disconnect() {
    synchronized (this) {
      if (isConnecting || isDisconnecting) return;

      isDisconnecting = true;
    }

    try {
      disconnectXMPPInternal();
    } finally {
      synchronized (this) {
        isDisconnecting = false;
      }
    }
  }

  // TODO javadoc
  public void setCallback(IConnectingFailureCallback callback) {
    this.callback = callback;
  }

  private void disconnectXMPPInternal() {
    connectionService.disconnect();
  }

  private void connectXMPPInternal(final XMPPAccount account, final boolean failSilently) {
    IConnectingFailureCallback callbackTmp = callback;

    String username = account.getUsername();
    String password = account.getPassword();
    String domain = account.getDomain();
    String server = account.getServer();
    int port = account.getPort();
    boolean useTLS = account.useTLS();
    boolean useSASL = account.useSASL();

    connectionService.disconnect();

    List<String> socks5Candidates = preferences.getSocks5Candidates();

    if (socks5Candidates.isEmpty()) socks5Candidates = null;

    connectionService.configure(
        SarosConstants.RESOURCE,
        preferences.isSmackDebugModeEnabled(),
        preferences.isLocalSOCKS5ProxyEnabled(),
        preferences.getFileTransferPort(),
        socks5Candidates,
        preferences.getAutoPortmappingGatewayID(),
        preferences.useExternalGatewayAddress(),
        preferences.getStunIP(),
        preferences.getStunPort(),
        preferences.isAutoPortmappingEnabled());

    try {

      if (preferences.forceIBBTransport())
        connectionManager.setServices(IConnectionManager.IBB_SERVICE);
      else connectionManager.setServices(/* use all */ -1);

      connectionService.connect(
          createConnectionConfiguration(domain, server, port, useTLS, useSASL), username, password);
    } catch (Exception e) {
      if (!(e instanceof XMPPException))
        LOG.error("internal error while connecting to the XMPP server: " + e.getMessage(), e);

      synchronized (this) {
        isConnecting = false;
      }

      if (callbackTmp != null && !failSilently) {
        callbackTmp.connectingFailed(e);
        return;
      }
    }
  }

  private ConnectionConfiguration createConnectionConfiguration(
      String domain, String server, int port, boolean useTLS, boolean useSASL) {

    ConnectionConfiguration connectionConfiguration = null;
    if (server.isEmpty()) connectionConfiguration = new ConnectionConfiguration(domain);
    else connectionConfiguration = new ConnectionConfiguration(server, port, domain);

    connectionConfiguration.setSASLAuthenticationEnabled(useSASL);

    if (!useTLS)
      connectionConfiguration.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

    connectionConfiguration.setReconnectionAllowed(false);

    return connectionConfiguration;
  }

  private void setConnectionState(
      final ConnectionState state, final Exception error, final boolean fireChanges) {
    synchronized (notifyLock) {
      this.currentConnectionState = state;
      this.currentConnectionError = error;
    }

    for (IConnectionStateListener listener : stateListeners) {
      try {
        listener.connectionStateChanged(state, error);
      } catch (Exception e) {
        LOG.error("internal error in listener: " + listener, e);
      }
    }
  }
}
