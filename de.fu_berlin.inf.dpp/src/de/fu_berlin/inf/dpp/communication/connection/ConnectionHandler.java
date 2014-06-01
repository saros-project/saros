package de.fu_berlin.inf.dpp.communication.connection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;

/**
 * Facade for handling connection establishment and connection events. This
 * facade should be preferred over {@link XMPPConnectionService} and
 * <b>should</b> always be used in UI components and widgets.
 */
// TODO better name
// TODO move to core
public class ConnectionHandler {

    private static final Logger LOG = Logger.getLogger(ConnectionHandler.class);

    private final XMPPConnectionService connectionService;
    private final XMPPAccountStore accountStore;
    private final PreferenceUtils preferences;

    private final DataTransferManager transferManager;

    private volatile IConnectingFailureCallback callback;

    private List<IConnectionStateListener> stateListeners = new CopyOnWriteArrayList<IConnectionStateListener>();

    private boolean isConnecting;
    private boolean isDisconnecting;

    private final IConnectionListener xmppConnectionListener = new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState state) {

            final Exception error = state == ConnectionState.ERROR ? connectionService
                .getConnectionError() : null;

            for (IConnectionStateListener listener : stateListeners)
                listener.connectionStateChanged(state, error);
        }

    };

    public ConnectionHandler(final XMPPConnectionService connectionService,
        final DataTransferManager transferManager,
        final XMPPAccountStore accountStore, final PreferenceUtils preferences) {
        this.connectionService = connectionService;
        this.transferManager = transferManager;
        this.accountStore = accountStore;
        this.preferences = preferences;

        this.connectionService.addListener(xmppConnectionListener);
    }

    /**
     * @see XMPPConnectionService#getConnectionState()
     */
    public ConnectionState getConnectionState() {
        return connectionService.getConnectionState();
    }

    /**
     * @see XMPPConnectionService#getJID()
     */
    public JID getJID() {
        return connectionService.getJID();
    }

    /**
     * Checks if a connection is currently established.
     * 
     * @return <code>true</code> if a connection is established,
     *         <code>false</code> otherwise
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
            connectInternal(failSilently);
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
            connectionService.disconnect();
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

    private void connectInternal(boolean failSilently) {
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

        connectionService.configure(Saros.NAMESPACE, Saros.RESOURCE,
            preferences.isDebugEnabled(),
            preferences.isLocalSOCKS5ProxyEnabled(),
            preferences.getFileTransferPort(), socks5Candidates,
            preferences.getAutoPortmappingGatewayID(),
            preferences.useExternalGatewayAddress(), preferences.getStunIP(),
            preferences.getStunPort(), preferences.isAutoPortmappingEnabled());

        try {

            if (preferences.forceFileTranserByChat())
                transferManager.setTransport(DataTransferManager.IBB_TRANSPORT);
            else
                transferManager.setTransport(/* use all */-1);

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

    /**
     * Returns the Eclipse {@linkplain ProxyInfo proxy information} for the
     * given host or <code>null</code> if it is not available
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ProxyInfo getProxyInfo(String host) {

        URI hostURI;

        try {
            hostURI = new URI(host);
        } catch (URISyntaxException e) {
            return null;
        }

        BundleContext bundleContext = null;

        // TODO perform the intended logic here
        if (true)
            return null;

        ServiceReference serviceReference = bundleContext
            .getServiceReference(IProxyService.class.getName());

        IProxyService proxyService = (IProxyService) bundleContext
            .getService(serviceReference);

        if (proxyService == null || !proxyService.isProxiesEnabled())
            return null;

        for (IProxyData pd : proxyService.select(hostURI)) {
            if (IProxyData.SOCKS_PROXY_TYPE.equals(pd.getType())) {
                return ProxyInfo.forSocks5Proxy(pd.getHost(), pd.getPort(),
                    pd.getUserId(), pd.getPassword());
            }
        }

        return null;
    }

    private ConnectionConfiguration createConnectionConfiguration(
        String domain, String server, int port, boolean useTLS, boolean useSASL) {

        ProxyInfo proxyInfo;

        if (server.length() != 0)
            proxyInfo = getProxyInfo(server);
        else
            proxyInfo = getProxyInfo(domain);

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
}
