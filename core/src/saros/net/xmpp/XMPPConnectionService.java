package saros.net.xmpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.entitycaps.EntityCapsManager;
import org.jivesoftware.smackx.entitycaps.packet.CapsExtension;
import saros.annotations.Component;
import saros.net.ConnectionState;
import saros.net.stun.IStunService;
import saros.net.upnp.IUPnPService;
import saros.repackaged.picocontainer.annotations.Nullable;

/**
 * This class is responsible for establishing XMPP connections and notifying registered listeners
 * about the state of the current connection.
 *
 * <p>In addition it will also setup an Socks5 server if configured and use UPnP if possible to
 * ensure that the Socks5 server is reachable.
 */
@Component(module = "net")
public class XMPPConnectionService {

  public static final String XMPP_CLIENT_IDENTIFIER = "https://saros-project.org";
  private static final String CAPS_HASH_ALGORITHM = "sha-1";

  private static final Logger log = Logger.getLogger(XMPPConnectionService.class);

  private Connection connection;

  private String resource;
  private int proxyPort;
  private boolean isProxyEnabled;
  private String gatewayDeviceID;

  /** The current IP address of the STUN server or <code>null</code>. */
  private String stunServer;

  /** The current port of the STUN server or <code>null</code>. */
  private int stunPort;

  /**
   * Flag indicating if the public IP address of the gateway device should be published as Socks5
   * candidate.
   */
  private boolean useExternalGatewayDeviceAddress;

  private List<String> proxyAddresses;

  private JID localJID;

  private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

  private Exception connectionError;

  private final List<IConnectionListener> listeners =
      new CopyOnWriteArrayList<IConnectionListener>();

  private final Socks5ProxySupport socks5ProxySupport;

  private int packetReplyTimeout;

  private final ConnectionListener smackConnectionListener =
      new ConnectionListener() {

        @Override
        public void connectionClosed() {
          /*
           * see Smack Source: connectionClosed is called before
           * connectionClosedOnError and so would violate the state transition
           * described in the ConnectionState class
           */
        }

        @Override
        public void connectionClosedOnError(Exception e) {
          synchronized (XMPPConnectionService.this) {
            log.error("XMPP connection error: ", e);
            setConnectionState(ConnectionState.ERROR, e);
            disconnectInternal();
            setConnectionState(ConnectionState.NOT_CONNECTED, null);
            localJID = null;
          }
        }

        @Override
        public void reconnectingIn(int seconds) {
          // NOP
        }

        @Override
        public void reconnectionFailed(Exception e) {
          // NOP
        }

        @Override
        public void reconnectionSuccessful() {
          // NOP
        }
      };

  public XMPPConnectionService(
      @Nullable IUPnPService upnpService, @Nullable IStunService stunService) {
    socks5ProxySupport = new Socks5ProxySupport(upnpService, stunService);
    packetReplyTimeout = Integer.getInteger("saros.net.smack.PACKET_REPLAY_TIMEOUT", 30000);
  }

  /**
   * Configures the service. Must be at least called once before {@link #connect} is called.
   *
   * @param resource the resource qualifier for a running connection
   * @param enableDebug true to show Smack Debug Window upon XMPP connection
   * @param proxyEnabled true to enable Socks5Proxy
   * @param proxyPort Socks5Proxy port
   * @param proxyAddresses collection of addresses (either host name or ip address) for the local
   *     Socks5 proxy, if <code>null</code> the addresses will be determined automatically at proxy
   *     start
   * @param gatewayDeviceID the USN of a UPNP compatible gateways device to perform port mapping on
   *     or <code>null</code>
   * @param useExternalGatewayDeviceAddress if <code>true</code> the external (ip) address of the
   *     gateway will be included into the Socks5 proxy addresses
   * @param stunServer STUN server address or <code>null</code> if STUN discovery should not be
   *     performed
   * @param stunPort STUN server port if 0 the default stun port will be used
   */
  public synchronized void configure(
      final String resource,
      final boolean enableDebug,
      final boolean proxyEnabled,
      final int proxyPort,
      final Collection<String> proxyAddresses,
      final String gatewayDeviceID,
      final boolean useExternalGatewayDeviceAddress,
      final String stunServer,
      final int stunPort,
      boolean enablePortMapping) {

    if (isConnected())
      throw new IllegalStateException(
          "cannot configure the network while a connection is established");

    Connection.DEBUG_ENABLED = enableDebug;
    this.resource = resource;
    this.proxyPort = proxyPort;

    if (proxyAddresses != null) this.proxyAddresses = new ArrayList<String>(proxyAddresses);
    else this.proxyAddresses = null;

    this.isProxyEnabled = proxyEnabled;
    this.gatewayDeviceID = gatewayDeviceID;
    this.useExternalGatewayDeviceAddress = useExternalGatewayDeviceAddress;
    this.stunServer = stunServer;
    this.stunPort = stunPort;

    if (this.stunServer != null && this.stunServer.isEmpty()) this.stunServer = null;

    if (this.gatewayDeviceID != null && this.gatewayDeviceID.isEmpty()) this.gatewayDeviceID = null;
  }

  /**
   * The {@linkplain JID resource qualified JID} of the local user if currently {@linkplain
   * ConnectionState#CONNECTED connected} to a XMPP server.</br> The JID is also available in the
   * states {@linkplain ConnectionState#ERROR error}, {@linkplain ConnectionState#DISCONNECTING
   * disconnecting} and {@linkplain ConnectionState#NOT_CONNECTED disconnected} during a {@linkplain
   * IConnectionListener#connectionStateChanged listener} notification if and only if a successful
   * login was performed before.
   *
   * @return the resource qualified JID of the current connection or <code>null</code> if not
   *     connected to a server
   */
  public JID getJID() {
    return localJID;
  }

  /**
   * Returns the {@linkplain Roster roster} of the established connection or <code>null</code> if
   * not connected.
   */
  public Roster getRoster() {
    return isConnected() ? connection.getRoster() : null;
  }

  /**
   * Connects the service to a XMPP server using the given configuration and credentials.
   *
   * @param connectionConfiguration {@link ConnectionConfiguration} Configuration for connecting to
   *     the server.
   * @param username the username of the XMPP account
   * @param password the password of the XMPP Account
   * @blocking
   */
  public synchronized void connect(
      ConnectionConfiguration connectionConfiguration, String username, String password)
      throws XMPPException {

    if (isConnected()) disconnect();

    initialzeNetworkComponents();

    Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

    ServiceDiscoveryManager.setIdentityName(XMPP_CLIENT_IDENTIFIER);
    // send presence manual after Login, because automatic doesn't use caps extension
    connectionConfiguration.setSendPresence(false);

    connection = new XMPPConnection(connectionConfiguration);

    try {
      setConnectionState(ConnectionState.CONNECTING, null);

      connection.connect();

      /*
       * BUG in Smack: should be possible to register the listener before
       * call connect
       */
      connection.addConnectionListener(smackConnectionListener);

      connection.login(username, password, resource);

      sendAvailablePresenceWithClientIdentifier();

      localJID = new JID(connection.getUser());

      setConnectionState(ConnectionState.CONNECTED, null);
    } catch (IllegalArgumentException e) {
      /*
       * cleanup is handled in the listener callback as the connection can
       * only be closed due to an error
       */
      throw new XMPPException("connection lost during login attempt");
    } catch (XMPPException e) {
      setConnectionState(ConnectionState.ERROR, e);
      disconnectInternal();
      setConnectionState(ConnectionState.NOT_CONNECTED, null);
      localJID = null;
      throw (e);
    }
  }

  private void sendAvailablePresenceWithClientIdentifier() {
    String version = EntityCapsManager.getInstanceFor(connection).getCapsVersion();
    CapsExtension caps = new CapsExtension(XMPP_CLIENT_IDENTIFIER, version, CAPS_HASH_ALGORITHM);

    Presence presence = new Presence(Presence.Type.available);
    presence.addExtension(caps);
    connection.sendPacket(presence);
  }

  /**
   * Disconnects the service from the current XMPP server if not already disconnected.
   *
   * @blocking
   */
  public synchronized void disconnect() {
    if (isConnected()) {
      setConnectionState(ConnectionState.DISCONNECTING, null);

      disconnectInternal();

      setConnectionState(ConnectionState.NOT_CONNECTED, null);
    }
    localJID = null;
  }

  /** Returns whether the service is currently connected to a XMPP server. */
  public boolean isConnected() {
    return connectionState == ConnectionState.CONNECTED;
  }

  /** Returns the current {@linkplain ConnectionState connection state} of the service. */
  public ConnectionState getConnectionState() {
    return connectionState;
  }

  /**
   * Returns the exception that occurred due to a connection failure. The exception can only be
   * retrieved during a {@linkplain ConnectionState#ERROR} callback and is <code>null</code>
   * otherwise.
   *
   * @see IConnectionListener#connectionStateChanged(Connection, ConnectionState)
   * @return exception that occurred during recent connection failure or <code>null</code> if not
   *     applicable.
   */
  public Exception getConnectionError() {
    return connectionError;
  }

  /**
   * Returns the currently used connection.
   *
   * @return the currently used connection or <code>null</code> if there is none @Note it is
   *     strictly forbidden to call {@linkplain Connection#disconnect()} on the returned instance
   */
  public Connection getConnection() {
    return connection;
  }

  public void addListener(IConnectionListener listener) {
    listeners.add(listener);
  }

  public void removeListener(IConnectionListener listener) {
    listeners.remove(listener);
  }

  private synchronized void disconnectInternal() {

    if (connection == null) return;

    try {
      connection.removeConnectionListener(smackConnectionListener);
      connection.disconnect();
    } catch (RuntimeException e) {
      log.warn("could not disconnect from the current XMPPConnection", e);
    } finally {

      uninitialzeNetworkComponents();
      connection = null;
    }
  }

  /** Sets a new connection state and notifies all connection listeners. */
  private void setConnectionState(ConnectionState state, Exception error) {

    this.connectionState = state;
    this.connectionError = error;

    // Prefix the name of the user for which the state changed
    String prefix = "";
    if (connection != null) {
      String user = connection.getUser();
      if (user != null) prefix = new JID(user).toString();
    }

    if (error == null) {
      log.debug(prefix + " new connection state == " + state);
    } else {
      log.error(prefix + " new connection state == " + state, error);
    }

    for (IConnectionListener listener : listeners) {
      try {
        listener.connectionStateChanged(connection, state);
      } catch (Exception e) {
        log.error("internal error in listener: " + listener, e);
      }
    }
  }

  /**
   * Configures Bytestream related settings, like PacketReplyTimeout, Socks5Proxy configuration,
   * look up streamhost address candidates for Socks5Proxy, update UPnP settings on proxy setting
   * change.
   */
  private void initialzeNetworkComponents() {

    /*
     * Setting the Smack timeout for packet replies. The default of 5000 can
     * be too low for IBB transfers when many other packets are send
     * concurrently (invitation over IBB, concurrently producing many
     * activities)
     */
    SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);

    if (!isProxyEnabled) {
      socks5ProxySupport.disableProxy();
      return; // we are done, STUN and UPNP only affect Socks5
    }

    socks5ProxySupport.enableProxy(
        proxyPort,
        proxyAddresses,
        gatewayDeviceID,
        useExternalGatewayDeviceAddress,
        stunServer,
        stunPort);
  }

  private void uninitialzeNetworkComponents() {
    socks5ProxySupport.disableProxy();
  }
}
