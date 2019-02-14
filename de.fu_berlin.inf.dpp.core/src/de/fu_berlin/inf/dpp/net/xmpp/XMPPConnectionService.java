package de.fu_berlin.inf.dpp.net.xmpp;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.stun.IStunService;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.util.NetworkingUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.picocontainer.annotations.Nullable;

/**
 * This class is responsible for establishing XMPP connections and notifying registered listeners
 * about the state of the current connection.
 *
 * <p>In addition it will also setup an Socks5 server if configured and use UPnP if possible to
 * ensure that the Socks5 server is reachable.
 */
@Component(module = "net")
public class XMPPConnectionService {
  private static final Logger LOG = Logger.getLogger(XMPPConnectionService.class);

  // DO NOT CHANGE THE CONTENT OF THIS STRING, NEVER NEVER NEVER !!!
  private static final String PORT_MAPPING_DESCRIPTION = "Saros Socks5 TCP";

  private Connection connection;

  private String resource;
  private int proxyPort;
  private boolean isProxyEnabled;
  private String gatewayDeviceID;

  /** The current IP address of the STUN server or <code>null</code>. */
  private String stunServer;

  /** The current port of the STUN server or <code>null</code>. */
  private int stunPort;

  /** The current gateway device to use for port mapping or <code>null</code>. */
  private GatewayDevice device;

  private boolean useExternalGatewayDeviceAddress;

  /** The listening port of the current Socks5Proxy. Is <b>-1</b> if the proxy is not running. */
  private int socks5ProxyPort;

  private List<String> proxyAddresses;

  private final Object portMappingLock = new Object();

  private JID localJID;

  private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

  private Exception connectionError;

  private final List<IConnectionListener> listeners =
      new CopyOnWriteArrayList<IConnectionListener>();

  private final IStunService stunService;

  private final IUPnPService upnpService;

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
            LOG.error("XMPP connection error: ", e);
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
    this.upnpService = upnpService;
    this.stunService = stunService;

    packetReplyTimeout =
        Integer.getInteger("de.fu_berlin.inf.dpp.net.smack.PACKET_REPLAY_TIMEOUT", 30000);
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

    this.device = null;

    if (this.stunServer != null && this.stunServer.isEmpty()) this.stunServer = null;

    if (this.gatewayDeviceID != null && this.gatewayDeviceID.isEmpty()) this.gatewayDeviceID = null;

    if (this.gatewayDeviceID == null) return;

    /*
     * perform blocking tasks in the background meanwhile to speed up first
     * connection attempt, currently it is only UPNP discovery
     */

    ThreadUtils.runSafeAsync(
        "dpp-upnp-resolver",
        LOG,
        new Runnable() {
          @Override
          public void run() {
            if (upnpService != null) upnpService.getGateways(true);
          }
        });
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
      LOG.warn("could not disconnect from the current XMPPConnection", e);
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
      LOG.debug(prefix + " new connection state == " + state);
    } else {
      LOG.error(prefix + " new connection state == " + state, error);
    }

    for (IConnectionListener listener : listeners) {
      try {
        listener.connectionStateChanged(connection, state);
      } catch (Exception e) {
        LOG.error("internal error in listener: " + listener, e);
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

    SmackConfiguration.setLocalSocks5ProxyEnabled(isProxyEnabled);

    if (!isProxyEnabled) return; // we are done, STUN and UPNP only affect Socks5

    SmackConfiguration.setLocalSocks5ProxyPort(proxyPort);

    final Socks5Proxy proxy = Socks5Proxy.getSocks5Proxy();

    proxy.start();
    socks5ProxyPort = proxy.getPort();

    LOG.debug(
        "started Socks5 proxy on port: " + socks5ProxyPort + " [listening on all interfaces]");

    List<String> interfaceAddresses = new ArrayList<String>();

    if (proxyAddresses != null) {
      interfaceAddresses.addAll(proxyAddresses);

      if (interfaceAddresses.isEmpty())
        LOG.warn("Socks5 preconfigured addresses list is empty, using autodetect mode");
      else LOG.debug("using preconfigured addresses: " + interfaceAddresses);
    }

    if (interfaceAddresses.isEmpty()) {
      for (InetAddress interfaceAddress : NetworkingUtils.getAllNonLoopbackLocalIPAddresses(true)) {
        interfaceAddresses.add(interfaceAddress.getHostAddress());
      }
      LOG.debug("using autodetected addresses: " + interfaceAddresses);
    }

    proxy.replaceLocalAddresses(interfaceAddresses);

    /*
     * The public IP address from the STUN result may be added to late but
     * this is definitely better then blocking the connection establishment
     * for several seconds. Also it is very uncommon the a connection
     * attempt to another is done just several seconds after the connection
     * to the XMPP server is established
     */
    if (stunService != null && stunServer != null) {
      ThreadUtils.runSafeAsync(
          "dpp-stun-discovery",
          LOG,
          new Runnable() {
            @Override
            public void run() {
              Collection<InetSocketAddress> addresses =
                  stunService.discover(stunServer, stunPort, 10000);

              for (InetSocketAddress address : addresses)
                NetworkingUtils.addProxyAddress(address.getAddress().getHostAddress(), true);
            }
          });
    }

    final CountDownLatch mappingStart = new CountDownLatch(1);

    if (gatewayDeviceID != null && upnpService != null) {
      final String gatewayDeviceIDToFind = gatewayDeviceID;
      ThreadUtils.runSafeAsync(
          "dpp-upnp-portmapping",
          LOG,
          new Runnable() {
            @Override
            public void run() {
              synchronized (portMappingLock) {
                mappingStart.countDown();

                List<GatewayDevice> devices = upnpService.getGateways(false);

                if (devices == null) {
                  LOG.warn("aborting UPNP port mapping due to network failure");
                  return;
                }

                for (GatewayDevice currentDevice : devices) {
                  if (gatewayDeviceIDToFind.equals(currentDevice.getUSN())) {
                    device = currentDevice;
                    break;
                  }
                }

                if (device == null) {
                  LOG.warn(
                      "could not find gateway device with id: + "
                          + gatewayDeviceID
                          + " in the current network environment");
                  return;
                }

                upnpService.deletePortMapping(device, socks5ProxyPort, IUPnPService.TCP);

                LOG.debug(
                    "creating port mapping on device: "
                        + device.getFriendlyName()
                        + " ["
                        + socks5ProxyPort
                        + "|"
                        + IUPnPService.TCP
                        + "]");

                if (!upnpService.createPortMapping(
                    device, socks5ProxyPort, IUPnPService.TCP, PORT_MAPPING_DESCRIPTION)) {

                  LOG.warn(
                      "failed to create port mapping on device: "
                          + device.getFriendlyName()
                          + " ["
                          + socks5ProxyPort
                          + "|"
                          + IUPnPService.TCP
                          + "]");

                  device = null;
                  return;
                }

                if (!useExternalGatewayDeviceAddress) return;

                InetAddress externalAddress = upnpService.getExternalAddress(device);

                if (externalAddress != null)
                  NetworkingUtils.addProxyAddress(externalAddress.getHostAddress(), true);
              }
            }
          });

      try {
        mappingStart.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void uninitialzeNetworkComponents() {
    if (socks5ProxyPort == -1) return;

    Socks5Proxy.getSocks5Proxy().stop();

    deleteMapping:
    synchronized (portMappingLock) {
      if (device == null) break deleteMapping;

      if (!upnpService.isMapped(device, socks5ProxyPort, IUPnPService.TCP)) break deleteMapping;

      LOG.debug(
          "deleting port mapping on device: "
              + device.getFriendlyName()
              + " ["
              + socks5ProxyPort
              + "|"
              + IUPnPService.TCP
              + "]");

      if (!upnpService.deletePortMapping(device, socks5ProxyPort, IUPnPService.TCP)) {
        LOG.warn(
            "failed to delete port mapping on device: "
                + device.getFriendlyName()
                + " ["
                + socks5ProxyPort
                + "|"
                + IUPnPService.TCP
                + "]");
      }
    }

    socks5ProxyPort = -1;
    device = null;
  }
}
