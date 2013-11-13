package de.fu_berlin.inf.dpp.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;
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
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.stun.IStunService;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.util.NetworkingUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This class is responsible for establishing XMPP connections and notifying
 * registered listeners about the state of the current connection.
 */
@Component(module = "net")
public class SarosNet {
    private static final Logger LOG = Logger.getLogger(SarosNet.class);

    private Connection connection;

    private String namespace;
    private String resource;
    private int proxyPort;
    private boolean isProxyEnabled;
    private String stunServer;
    private int stunPort;
    private boolean isPortMappingEnabled;

    private JID localJID;

    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    private Exception connectionError;

    private final List<IConnectionListener> listeners = new CopyOnWriteArrayList<IConnectionListener>();

    // Smack (XMPP) connection listener
    private ConnectionListener smackConnectionListener;

    private final IStunService stunService;

    private final IUPnPService upnpService;

    private int packetReplyTimeout;

    public SarosNet(@Nullable IUPnPService upnpService,
        @Nullable IStunService stunService) {
        this.upnpService = upnpService;
        this.stunService = stunService;

        packetReplyTimeout = Integer.getInteger(
            "de.fu_berlin.inf.dpp.net.smack.PACKET_REPLAY_TIMEOUT", 30000);
    }

    /**
     * Configures the service. Must be at least called once before
     * {@link #connect} is called.
     * 
     * @param namespace
     *            the namespace of the feature (plugin)
     * @param resource
     *            the resource qualifier for a running connection
     * @param enableDebug
     *            true to show Smack Debug Window upon XMPP connection
     * @param proxyEnabled
     *            true to enable Socks5Proxy
     * @param proxyPort
     *            Socks5Proxy port
     * @param stunServer
     *            STUN server (address)
     * @param stunPort
     *            STUN server port if 0 the default stun port will be used
     * @param enablePortMapping
     *            true to enable UPnP port mapping
     */
    public void configure(String namespace, String resource,
        boolean enableDebug, boolean proxyEnabled, int proxyPort,
        String stunServer, int stunPort, boolean enablePortMapping) {
        Connection.DEBUG_ENABLED = enableDebug;
        this.namespace = namespace;
        this.resource = resource;
        this.proxyPort = proxyPort;
        this.isProxyEnabled = proxyEnabled;
        this.stunServer = stunServer;
        this.stunPort = stunPort;
        this.isPortMappingEnabled = enablePortMapping;

        if (this.stunServer.isEmpty())
            this.stunServer = null;
    }

    /**
     * The {@linkplain JID resource qualified JID} of the local user if
     * currently {@linkplain ConnectionState#CONNECTED connected} to a XMPP
     * server.</br> The JID is also available in the states
     * {@linkplain ConnectionState#ERROR error},
     * {@linkplain ConnectionState#DISCONNECTING disconnecting} and
     * {@linkplain ConnectionState#NOT_CONNECTED disconnected} during a
     * {@linkplain IConnectionListener#connectionStateChanged listener}
     * notification if and only if a successful login was performed before.
     * 
     * @return the resource qualified JID of the current connection or
     *         <code>null</code> if not connected to a server
     */
    public JID getMyJID() {
        return localJID;
    }

    /**
     * Returns the {@linkplain Roster roster} of the established connection or
     * <code>null</code> if not connected.
     */
    public Roster getRoster() {
        return isConnected() ? connection.getRoster() : null;
    }

    /**
     * Connects the service to a XMPP server using the given configuration and
     * credentials.
     * 
     * @param connectionConfiguration
     *            {@link ConnectionConfiguration} Configuration for connecting
     *            to the server.
     * @param username
     *            the username of the XMPP account
     * @param password
     *            the password of the XMPP Account
     * 
     * @blocking
     */
    public void connect(ConnectionConfiguration connectionConfiguration,
        String username, String password) throws XMPPException {

        if (isConnected())
            disconnect();

        setBytestreamConnectionProperties();

        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

        connection = new XMPPConnection(connectionConfiguration);

        try {
            setConnectionState(ConnectionState.CONNECTING, null);

            connection.connect();

            ServiceDiscoveryManager.getInstanceFor(connection).addFeature(
                namespace);

            // add connection listener so we get notified if it will be closed
            if (smackConnectionListener == null)
                smackConnectionListener = new XMPPConnectionListener();

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
     * Disconnects the service from the current XMPP server if not already
     * disconnected.
     * 
     * @blocking
     */
    public void disconnect() {
        if (isConnected()) {
            setConnectionState(ConnectionState.DISCONNECTING, null);

            disconnectInternal();

            setConnectionState(ConnectionState.NOT_CONNECTED, null);
        }
        localJID = null;
    }

    /**
     * Returns whether the service is currently connected to a XMPP server.
     */
    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    /**
     * Returns the current {@linkplain ConnectionState connection state} of the
     * service.
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Returns the exception that occurred due to a connection failure. The
     * exception can only be retrieved during a
     * {@linkplain ConnectionState#ERROR} callback and is <code>null</code>
     * otherwise.
     * 
     * @see IConnectionListener#connectionStateChanged(Connection,
     *      ConnectionState)
     * @return exception that occurred during recent connection failure or
     *         <code>null</code> if not applicable.
     */
    public Exception getConnectionError() {
        return connectionError;
    }

    /**
     * Returns the currently used connection.
     * 
     * @return the currently used connection or <code>null</code> if there is
     *         none
     * 
     * @Note it is strictly forbidden to call
     *       {@linkplain Connection#disconnect()} on the returned instance
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

    private void disconnectInternal() {
        if (connection == null)
            return;

        try {
            connection.removeConnectionListener(smackConnectionListener);
            connection.disconnect();
        } catch (RuntimeException e) {
            LOG.warn("could not disconnect from the current XMPPConnection", e);
        } finally {
            connection = null;
        }
    }

    /**
     * Sets a new connection state and notifies all connection listeners.
     */
    private void setConnectionState(ConnectionState state, Exception error) {

        this.connectionState = state;
        this.connectionError = error;

        // Prefix the name of the user for which the state changed
        String prefix = "";
        if (connection != null) {
            String user = connection.getUser();
            if (user != null)
                prefix = Utils.prefix(new JID(user));
        }

        if (error == null) {
            LOG.debug(prefix + "new connection state == " + state);
        } else {
            LOG.error(prefix + "new connection state == " + state, error);
        }

        for (IConnectionListener listener : this.listeners) {
            try {
                listener.connectionStateChanged(this.connection, state);
            } catch (Exception e) {
                LOG.error("internal error in listener: " + listener, e);
            }
        }
    }

    /**
     * Configures Bytestream related settings, like PacketReplyTimeout,
     * Socks5Proxy configuration, look up streamhost address candiates for
     * Socks5Proxy, update UPnP settings on proxy setting change.
     */
    private void setBytestreamConnectionProperties() {

        /*
         * Setting the Smack timeout for packet replies. The default of 5000 can
         * be too low for IBB transfers when many other packets are send
         * concurrently (invitation over IBB, concurrently producing many
         * activities)
         */
        SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);

        // Socks5 Proxy Configuration
        boolean settingsChanged = false;
        boolean portChanged = false;

        /*
         * TODO Fix in Smack: Either always start proxy when enabled in the
         * smack configuration or never start it automatically. Currently it
         * only starts after initiation the singleton on first access.
         */

        Socks5Proxy proxy = NetworkingUtils.getSocks5ProxySafe();

        if (isProxyEnabled != SmackConfiguration.isLocalSocks5ProxyEnabled()) {
            settingsChanged = true;
            SmackConfiguration.setLocalSocks5ProxyEnabled(isProxyEnabled);
        }

        // Note: The proxy gets restarted on port change, too.
        if (proxyPort != SmackConfiguration.getLocalSocks5ProxyPort()) {
            settingsChanged = true;
            portChanged = true;
            SmackConfiguration.setLocalSocks5ProxyPort(proxyPort);
        }

        // Get & set all (useful) internal and external IP addresses as
        // potential connect addresses
        if (isProxyEnabled) {

            try {
                List<InetAddress> myAdresses = NetworkingUtils
                    .getAllNonLoopbackLocalIPAdresses(true);

                if (!myAdresses.isEmpty()) {
                    // convert to List of Strings
                    List<String> myAdressesStr = new LinkedList<String>();
                    for (InetAddress ip : myAdresses)
                        myAdressesStr.add(ip.getHostAddress());
                    proxy.replaceLocalAddresses(myAdressesStr);
                }

                if (stunService != null && stunServer != null) {
                    Utils.runSafeAsync("StunAddProxys", LOG, new Runnable() {
                        @Override
                        public void run() {
                            Collection<InetSocketAddress> addresses = stunService
                                .discover(stunServer, stunPort, 10000);

                            for (InetSocketAddress address : addresses)
                                NetworkingUtils.addProxyAddress(address
                                    .getAddress().getHostAddress(), true);
                        }
                    });
                }
            } catch (Exception e) {
                LOG.debug("Error while retrieving IP addresses", e);
            }
        }

        if (settingsChanged || proxy.isRunning() != isProxyEnabled) {
            StringBuilder sb = new StringBuilder();
            if (settingsChanged)
                sb.append("Socks5Proxy properties changed. ");

            if (proxy.isRunning()) {
                proxy.stop();
                sb.append("Socks5Proxy stopped. ");
            }
            if (isProxyEnabled) {
                proxy.start();
                sb.append("Socks5Proxy started on port " + proxy.getPort()
                    + "...");
            }
            LOG.debug(sb);

            if (upnpService != null) {
                // Update the mapped port on Socks5Proy port change
                if (isProxyEnabled && portChanged && upnpService.isMapped())
                    upnpService.createSarosPortMapping();

                // create UPnP port mapping if not existing
                if (isProxyEnabled && isPortMappingEnabled
                    && !upnpService.isMapped())
                    upnpService.createSarosPortMapping();

                // remove UPnP port mapping if not required
                if (!isProxyEnabled && upnpService.isMapped())
                    upnpService.removeSarosPortMapping();

                if (upnpService.isMapped()) {
                    String gatewayPublicIP = upnpService.getPublicGatewayIP();
                    if (gatewayPublicIP != null)
                        NetworkingUtils.addProxyAddress(gatewayPublicIP, true);
                }
            }
        }
    }

    private class XMPPConnectionListener implements ConnectionListener {

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
            LOG.error("XMPP connection error: ", e);
            setConnectionState(ConnectionState.ERROR, e);
            disconnectInternal();
            setConnectionState(ConnectionState.NOT_CONNECTED, null);
            localJID = null;
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
    }
}
