package de.fu_berlin.inf.dpp.communication.connection;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.SarosConstants;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.internal.TCPServer;
import de.fu_berlin.inf.dpp.net.mdns.MDNSService;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.preferences.IPreferences;

/**
 * Facade for handling connection establishment and connection events. This
 * facade should be preferred over {@link XMPPConnectionService} and
 * <b>should</b> always be used in UI components and widgets.
 */
// TODO better name
public class ConnectionHandler {

    private static final Logger LOG = Logger.getLogger(ConnectionHandler.class);

    private static final boolean MDNS_MODE = Boolean
        .getBoolean("de.fu_berlin.inf.dpp.net.ENABLE_MDNS");

    private final XMPPConnectionService connectionService;
    private final MDNSService mDNSService;

    private final TCPServer tcpServer;

    private final IProxyResolver proxyResolver;

    private final XMPPAccountStore accountStore;
    private final IPreferences preferences;

    private final IConnectionManager connectionManager;

    private volatile IConnectingFailureCallback callback;

    private List<IConnectionStateListener> stateListeners = new CopyOnWriteArrayList<IConnectionStateListener>();

    private boolean isConnecting;
    private boolean isDisconnecting;

    private ConnectionState currentConnectionState = ConnectionState.NOT_CONNECTED;
    private Exception currentConnectionError = null;

    private final Object notifyLock = new Object();

    private final IConnectionListener xmppConnectionListener = new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState state) {

            if (state == ConnectionState.CONNECTING) {
                ServiceDiscoveryManager.getInstanceFor(connection).addFeature(
                    SarosConstants.XMPP_FEATURE_NAMESPACE);
            }

            final Exception error = state == ConnectionState.ERROR ? connectionService
                .getConnectionError() : null;

            setConnectionState(state, error, true);
        }

    };

    public ConnectionHandler(final XMPPConnectionService connectionService,
        final TCPServer tcpServer, final MDNSService mDNSService,
        final IConnectionManager transferManager,
        @Nullable final IProxyResolver proxyResolver,
        final XMPPAccountStore accountStore, final IPreferences preferences) {

        this.connectionService = connectionService;
        this.tcpServer = tcpServer;
        this.mDNSService = mDNSService;
        this.connectionManager = transferManager;
        this.proxyResolver = proxyResolver;
        this.accountStore = accountStore;
        this.preferences = preferences;

        this.connectionService.addListener(xmppConnectionListener);
    }

    /**
     * Returns the current connection state.
     */
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
     * @return a unique id for the current connection or <code>null</code> if no
     *         connection is established or the connection has no unique id
     */
    public String getConnectionID() {
        if (MDNS_MODE)
            return mDNSService.getQualifiedServiceName();

        final JID jid = connectionService.getJID();

        if (jid == null)
            return null;

        return jid.toString();
    }

    /**
     * Checks if a connection is currently established.
     *
     * @return <code>true</code> if a connection is established,
     *         <code>false</code> otherwise
     */
    public boolean isConnected() {

        if (MDNS_MODE)
            return mDNSService.getQualifiedServiceName() != null;

        return connectionService.isConnected();
    }

    public void addConnectionStateListener(IConnectionStateListener listener) {
        stateListeners.add(listener);
    }

    public void removeConnectionStateListener(IConnectionStateListener listener) {
        stateListeners.remove(listener);
    }

    /**
     * Connects using the active account from the {@link XMPPAccountStore}. If a
     * connection establishment (connect or disconnect) is already in progress
     * this connection attempt will be ignored.
     *
     * If there is already an established connection this connection will be
     * disconnected.
     *
     * @param failSilently
     *            if set to <code>true</code> a connection failure will not be
     *            reported to the {@linkplain IConnectingFailureCallback
     *            callback}
     * @blocking this method may block for several seconds
     * @see XMPPAccountStore#setAccountActive(XMPPAccount)
     */

    public void connect(boolean failSilently) {

        synchronized (this) {
            if (isConnecting || isDisconnecting)
                return;

            isConnecting = true;
        }

        try {
            if (MDNS_MODE)
                connectMDNSInternal(failSilently);
            else
                connectXMPPInternal(failSilently);
        } finally {
            synchronized (this) {
                isConnecting = false;
            }
        }
    }

    // TODO javadoc
    public void disconnect() {
        synchronized (this) {
            if (isConnecting || isDisconnecting)
                return;

            isDisconnecting = true;
        }

        try {
            if (MDNS_MODE)
                disconnectMDNSInternal();
            else
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

    private void disconnectMDNSInternal() {
        setConnectionState(ConnectionState.DISCONNECTING, null, true);
        mDNSService.stop();
        tcpServer.stop();
        setConnectionState(ConnectionState.NOT_CONNECTED, null, true);
    }

    private void connectMDNSInternal(boolean failSilently) {
        IConnectingFailureCallback callbackTmp = callback;

        if (accountStore.isEmpty() && callbackTmp != null && !failSilently) {
            synchronized (this) {
                isConnecting = false;
            }

            callbackTmp.connectingFailed(null);
            return;
        }

        final XMPPAccount account = accountStore.getActiveAccount();

        // misuse the XMPP account credentials for now;

        if (isConnected()) {
            disconnectMDNSInternal();
        }

        // misuse the Socks5 proxy port for now
        int portUsed = 0;
        try {
            portUsed = tcpServer.start(null, preferences.getFileTransferPort());
        } catch (IOException e) {
            LOG.error("failed to start TCP server: " + e.getMessage(), e);

            synchronized (this) {
                isConnecting = false;
            }

            if (callbackTmp != null && !failSilently) {
                callbackTmp.connectingFailed(e);
                return;
            }
        }

        String serviceName = account.getUsername();

        mDNSService.configure("_dpp._tcp.local.", serviceName, portUsed, null);

        setConnectionState(ConnectionState.CONNECTING, null, true);
        try {
            mDNSService.start();
            setConnectionState(ConnectionState.CONNECTED, null, true);
        } catch (IOException e) {
            LOG.error("failed to start MDNS service", e);
            setConnectionState(ConnectionState.ERROR, e, true);
            setConnectionState(ConnectionState.NOT_CONNECTED, null, true);
        }
    }

    private void connectXMPPInternal(boolean failSilently) {
        IConnectingFailureCallback callbackTmp = callback;

        if (accountStore.isEmpty() && callbackTmp != null && !failSilently) {
            synchronized (this) {
                isConnecting = false;
            }

            callbackTmp.connectingFailed(null);
            return;
        }

        XMPPAccount account = accountStore.getActiveAccount();

        String username = account.getUsername();
        String password = account.getPassword();
        String domain = account.getDomain();
        String server = account.getServer();
        int port = account.getPort();
        boolean useTLS = account.useTLS();
        boolean useSASL = account.useSASL();

        connectionService.disconnect();

        List<String> socks5Candidates = preferences.getSocks5Candidates();

        if (socks5Candidates.isEmpty())
            socks5Candidates = null;

        connectionService.configure(SarosConstants.RESOURCE,
            preferences.isDebugEnabled(),
            preferences.isLocalSOCKS5ProxyEnabled(),
            preferences.getFileTransferPort(), socks5Candidates,
            preferences.getAutoPortmappingGatewayID(),
            preferences.useExternalGatewayAddress(), preferences.getStunIP(),
            preferences.getStunPort(), preferences.isAutoPortmappingEnabled());

        try {

            if (preferences.forceIBBTransport())
                connectionManager
                    .setTransport(IConnectionManager.IBB_TRANSPORT);
            else
                connectionManager.setTransport(/* use all */-1);

            connectionService.connect(
                createConnectionConfiguration(domain, server, port, useTLS,
                    useSASL), username, password);
        } catch (Exception e) {
            if (!(e instanceof XMPPException))
                LOG.error(
                    "internal error while connecting to the XMPP server: "
                        + e.getMessage(), e);

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

        ProxyInfo proxyInfo = null;

        if (proxyResolver != null) {
            if (server.length() != 0)
                proxyInfo = proxyResolver.resolve(server);
            else
                proxyInfo = proxyResolver.resolve(domain);
        }

        ConnectionConfiguration connectionConfiguration = null;

        if (server.length() == 0 && proxyInfo == null)
            connectionConfiguration = new ConnectionConfiguration(domain);
        else if (server.length() == 0 && proxyInfo != null)
            connectionConfiguration = new ConnectionConfiguration(domain,
                proxyInfo);
        else if (server.length() != 0 && proxyInfo == null)
            connectionConfiguration = new ConnectionConfiguration(server, port,
                domain);
        else
            connectionConfiguration = new ConnectionConfiguration(server, port,
                domain, proxyInfo);

        connectionConfiguration.setSASLAuthenticationEnabled(useSASL);

        if (!useTLS)
            connectionConfiguration
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        connectionConfiguration.setReconnectionAllowed(false);

        return connectionConfiguration;
    }

    private void setConnectionState(final ConnectionState state,
        final Exception error, final boolean fireChanges) {
        synchronized (notifyLock) {
            this.currentConnectionState = state;
            this.currentConnectionError = error;
        }

        for (IConnectionStateListener listener : stateListeners)
            listener.connectionStateChanged(state, error);

    }
}
