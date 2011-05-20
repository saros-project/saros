/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.security.sasl.SaslException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.socks5bytestream.Socks5Proxy;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Inject;
import org.picocontainer.injectors.Reinjector;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.wizards.ConfigurationWizard;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.pico.DotGraphMonitor;

/**
 * The main plug-in of Saros.
 * 
 * @author rdjemili
 * @author coezbek
 */
@Component(module = "core")
public class Saros extends AbstractUIPlugin {
    private static final int REFRESH_SECONDS = 3;

    /**
     * The single instance of the Saros plugin.
     */
    protected static Saros plugin;

    /**
     * True if the Saros instance has been initialized so that calling
     * reinject() will be well defined.
     */
    protected static boolean isInitialized;

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */
    public static final String SAROS = "de.fu_berlin.inf.dpp"; //$NON-NLS-1$

    /**
     * The name of the XMPP namespace used by Saros. At the moment it is only
     * used to advertise the Saros feature in the Service Discovery.
     * 
     * TODO Add version information, so that only compatible versions of Saros
     * can use each other.
     */
    public final static String NAMESPACE = SAROS;

    /**
     * The name of the resource identifier used by Saros when connecting to the
     * XMPP server (for instance when logging in as john@doe.com, Saros will
     * connect using john@doe.com/Saros)
     */
    public final static String RESOURCE = "Saros";

    public String sarosVersion;

    public String sarosFeatureID;

    protected SarosSessionManager sessionManager;

    /**
     * The reinjector used to inject dependencies into those objects that are
     * created by Eclipse and not by our PicoContainer.
     */
    protected Reinjector reinjector;

    /**
     * To print an architecture diagram at the end of the plug-in life-cycle
     * initialize the dotMonitor with a new instance:
     * 
     * <code>dotMonitor= new DotGraphMonitor();</code>
     */
    protected DotGraphMonitor dotMonitor;

    protected XMPPConnection connection;

    /**
     * The RQ-JID of the local user or null if the user is
     * {@link ConnectionState#NOT_CONNECTED}.
     */
    protected JID myJID;

    protected ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    protected Exception connectionError;

    protected final List<IConnectionListener> listeners = new CopyOnWriteArrayList<IConnectionListener>();

    // Smack (XMPP) connection listener
    protected ConnectionListener smackConnectionListener;

    /**
     * The global plug-in preferences, shared among all workspaces. Should only
     * be accessed over {@link #getConfigPrefs()} from outside this class.
     */
    protected Preferences configPrefs;

    /**
     * The secure preferences store, used to store sensitive data that may (at
     * the user's option) be stored encrypted.
     */
    protected ISecurePreferences securePrefs;

    public static final Random RANDOM = new Random();

    protected Logger log;

    private SarosContext sarosContext;

    static {
        Roster.setDefaultSubscriptionMode(SubscriptionMode.accept_all);
    }

    /**
     * Create the shared instance.
     */
    public Saros() {

        // Only start a DotGraphMonitor if asserts are enabled (aka debug mode)
        assert (dotMonitor = new DotGraphMonitor()) != null;

        setInitialized(false);
        setDefault(this);

        sarosContext = SarosContext.getContextForSaros(this)
            .withDotMonitor(dotMonitor).build();

        SarosPluginContext.setSarosContext(sarosContext);
    }

    protected static void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    protected static void checkInitialized() {
        if (plugin == null || !isInitialized()) {
            LogLog.error("Saros not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }

    /**
     * Returns true if the Saros instance has been initialized so that calling
     * {@link SarosContext#reinject(Object)} will be well defined.
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    protected void setBytestreamConnectionProperties() {

        /*
         * Setting the Smack timeout for packet replies. The default of 5000 can
         * be too low for IBB transfers when many other packets are send
         * concurrently (invitation over IBB, concurrently producing many
         * activities)
         */
        SmackConfiguration.setPacketReplyTimeout(30000);

        // Socks5 Proxy Configuration
        boolean settingsChanged = false;
        int port = sarosContext.getComponent(PreferenceUtils.class)
            .getFileTransferPort();
        boolean proxyEnabled = !getPreferenceStore().getBoolean(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED);

        // Note: The proxy gets restarted on port change, too.
        if (port != SmackConfiguration.getLocalSocks5ProxyPort()) {
            settingsChanged = true;
            SmackConfiguration.setLocalSocks5ProxyPort(port);
        }

        /*
         * TODO Fix in Smack: Either always start proxy when enabled in the
         * smack configuration or never start it automatically. Currently it
         * only starts after initiation the singleton on first access.
         */
        if (proxyEnabled != SmackConfiguration.isLocalSocks5ProxyEnabled()) {
            settingsChanged = true;
            SmackConfiguration.setLocalSocks5ProxyEnabled(proxyEnabled);
        }

        // Get & set all IP addresses as potential connect addresses
        Socks5Proxy proxy = Socks5Proxy.getSocks5Proxy();
        try {
            List<String> myAdresses = getAllNonLoopbackIPAdresses();
            if (!myAdresses.isEmpty())
                proxy.replaceLocalAddresses(myAdresses);
        } catch (Exception e) {
            log.debug("Error while retrieving local IP addresses", e);
        }

        if (settingsChanged || proxy.isRunning() != proxyEnabled) {
            StringBuilder sb = new StringBuilder(
                "Socks5Proxy properties changed.");
            if (proxy.isRunning()) {
                proxy.stop();
                sb.append(" Stopping...");
            }
            if (proxyEnabled) {
                sb.append(" Starting.");
                proxy.start();
            }
            if (settingsChanged)
                log.debug(sb);
        }

        // TODO: just pasted from before
        // This disables Jingle if the user has selected to use XMPP
        // file transfer exclusively
        JingleManager.setServiceEnabled(connection, !getPreferenceStore()
            .getBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT));
    }

    /**
     * Retrieves all non-loopback IP addresses from all network devices of the
     * local host. <br>
     * IPv4 addresses are sorted before IPv6 addresses (to let connecting to
     * IPv4 IPs before attempting their IPv6 equivalents when iterating the
     * List).
     * 
     * @return List<{@link String}> of all retrieved IP addresses
     * @throws UnknownHostException
     * @throws SocketException
     */
    protected List<String> getAllNonLoopbackIPAdresses()
        throws UnknownHostException, SocketException {

        List<String> ips = new LinkedList<String>();

        // Holds last ipv4 index in ips list (used to sort IPv4 before IPv6 IPs)
        int ipv4Index = 0;

        Enumeration<NetworkInterface> eInterfaces = NetworkInterface
            .getNetworkInterfaces();

        // Enumerate interfaces and enumerate all internet addresses of each
        if (eInterfaces != null) {
            while (eInterfaces.hasMoreElements()) {
                NetworkInterface ni = eInterfaces.nextElement();

                Enumeration<InetAddress> iaddrs = ni.getInetAddresses();
                while (iaddrs.hasMoreElements()) {
                    InetAddress iaddr = iaddrs.nextElement();
                    if (!iaddr.isLoopbackAddress()) {

                        if (iaddr instanceof Inet6Address)
                            ips.add(iaddr.getHostAddress());
                        else
                            ips.add(ipv4Index++, iaddr.getHostAddress());
                    }
                }
            }
        }

        return ips;
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {

        super.start(context);

        sarosVersion = Utils.getBundleVersion(getBundle(), "Unknown Version");

        sarosFeatureID = SAROS + "_" + sarosVersion;

        Connection.DEBUG_ENABLED = getPreferenceStore().getBoolean(
            PreferenceConstants.DEBUG);

        // Jingle has to be started once!
        JingleManager.setJingleServiceEnabled();

        /*
         * add Saros as XMPP feature once XMPPConnection is connected to the
         * XMPP server
         */
        Connection
            .addConnectionCreationListener(new ConnectionCreationListener() {
                public void connectionCreated(Connection connection) {
                    if (Saros.this.connection != connection) {
                        // Ignore the connections created in createAccount.
                        return;
                    }

                    ServiceDiscoveryManager sdm = ServiceDiscoveryManager
                        .getInstanceFor(connection);
                    sdm.addFeature(Saros.NAMESPACE);

                    setBytestreamConnectionProperties();
                }
            });

        setupLoggers();
        log.info("Starting Saros " + sarosVersion + " running:\n"
            + Utils.getPlatformInfo());

        // Remove the Bundle if an instance of it was already registered
        sarosContext.removeComponent(Bundle.class);
        sarosContext.addComponent(Bundle.class, getBundle());

        // Make sure that all components in the container are
        // instantiated
        sarosContext.getComponents(Object.class);

        this.sessionManager = sarosContext
            .getComponent(SarosSessionManager.class);

        isInitialized = true;

        // determine if auto-connect can and should be performed
        boolean autoConnect = getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_CONNECT);

        if (!autoConnect)
            return;

        StatisticManager statisticManager = sarosContext
            .getComponent(StatisticManager.class);
        ErrorLogManager errorLogManager = sarosContext
            .getComponent(ErrorLogManager.class);

        // we need at least a user name, but also the agreement to the
        // statistic and error log submission
        boolean hasUserName = this.sarosContext.getComponent(
            PreferenceUtils.class).hasUserName();
        boolean hasAgreement = statisticManager.hasStatisticAgreement()
            && errorLogManager.hasErrorLogAgreement();

        if (hasUserName && hasAgreement) {
            asyncConnect();
        }
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {

        // TODO Devise a general way to stop and dispose our components
        saveConfigPrefs();
        saveSecurePrefs();

        if (dotMonitor != null) {
            File f = new File("Saros-" + sarosFeatureID + ".dot");
            log.info("Saving Saros architecture diagram dot file: "
                + f.getAbsolutePath());
            dotMonitor.save(f);
        }

        try {
            if (isConnected()) {
                /*
                 * Need to fork because disconnect should not be run in the SWT
                 * thread.
                 */

                /*
                 * FIXME Provide a unique thread context in which all
                 * connecting/disconnecting is done.
                 */
                Utils.runSafeAsync(log, new Runnable() {
                    public void run() {
                        disconnect();
                    }
                });
            }

            /**
             * This will cause dispose() to be called on all components managed
             * by PicoContainer which implement {@link Disposable}.
             */
            sarosContext.dispose();
        } finally {
            super.stop(context);
        }

        isInitialized = false;
        setDefault(null);
    }

    public void removeChildContainer(PicoContainer child) {
        sarosContext.removeChildContainer(child);
    }

    public static void setDefault(Saros newPlugin) {
        Saros.plugin = newPlugin;

    }

    /**
     * The RQ-JID of the local user
     */
    public JID getMyJID() {
        return this.myJID;
    }

    public Roster getRoster() {
        if (!isConnected()) {
            return null;
        }

        return this.connection.getRoster();
    }

    /**
     * Returns the global {@link Preferences} with {@link ConfigurationScope}
     * for this plug-in or null if the node couldn't be determined. <br>
     * <br>
     * The returned Preferences can be accessed concurrently by multiple threads
     * of the same JVM without external synchronization. If they are used by
     * multiple JVMs no guarantees can be made concerning data consistency (see
     * {@link Preferences} for details).
     * 
     * @return the preferences node for this plug-in containing global
     *         preferences that are visible for all workspaces of this eclipse
     *         installation
     */
    public synchronized Preferences getConfigPrefs() {
        // TODO Singleton-Pattern code smell: ConfigPrefs should be a @component
        if (configPrefs == null) {
            configPrefs = new ConfigurationScope().getNode(SAROS);
        }
        return configPrefs;
    }

    /**
     * Saves the global preferences to disk. Should be called at least before
     * the bundle is stopped to prevent loss of data. Can be called whenever
     * found necessary.
     */
    public synchronized void saveConfigPrefs() {
        /*
         * Note: If multiple JVMs use the config preferences and the underlying
         * backing store, they might not always work with latest data, e.g. when
         * using multiple instances of the same eclipse installation.
         */
        if (configPrefs != null) {
            try {
                configPrefs.flush();
            } catch (BackingStoreException e) {
                log.error("Couldn't store global plug-in preferences", e);
            }
        }
    }

    /**
     * Retrieves the secure preferences store provided by
     * org.eclipse.equinox.security. Preferences entered here are encrypted for
     * storage.
     * 
     * @return The local secure preferences store.
     */
    public synchronized ISecurePreferences getSecurePrefs() {

        if (securePrefs == null) {
            try {
                File storeFile = new File(getStateLocation().toFile(), "/.pref");
                URI workspaceURI = storeFile.toURI();

                /*
                 * The SecurePreferencesFactory does not accept percent-encoded
                 * URLs, so we must decode the URL before passing it.
                 */
                String prefLocation = URLDecoder.decode(
                    workspaceURI.toString(), "UTF-8");
                URL prefURL = new URL(prefLocation);

                securePrefs = SecurePreferencesFactory.open(prefURL, null);
            } catch (MalformedURLException e) {
                log.error("Problem with URL when attempting to access secure preferences: "
                    + e);
            } catch (IOException e) {
                log.error("I/O problem when attempting to access secure preferences: "
                    + e);
            } finally {
                if (securePrefs == null)
                    securePrefs = SecurePreferencesFactory.getDefault();
            }
        }

        return securePrefs;
    }

    public synchronized void saveSecurePrefs() {
        if (xmppAccountStore != null) {
            xmppAccountStore.flush();
        }
        try {
            if (securePrefs != null) {
                securePrefs.flush();
            }
        } catch (IOException e) {
            log.error("Exception when trying to store secure preferences: " + e);
        }
    }

    /**
     * @nonBlocking
     */
    public void asyncConnect() {
        Utils.runSafeAsync("Saros-AsyncConnect-", log, new Runnable() {
            public void run() {
                connect(false);
            }
        });
    }

    @Inject
    PreferenceUtils preferenceUtils;
    @Inject
    StatisticManager statisticManager;
    @Inject
    ErrorLogManager errorLogManager;
    @Inject
    XMPPAccountStore xmppAccountStore;

    /**
     * Connects using the credentials from the preferences. If no credentials
     * are present a wizard is opened before. It uses TLS if possible.
     * 
     * If there is already an established connection when calling this method,
     * it disconnects before connecting (including state transitions!).
     * 
     * @blocking
     */
    public void connect(boolean failSilently) {
        // check if we need to do a re-inject
        if (preferenceUtils == null || statisticManager == null
            || errorLogManager == null)
            sarosContext.reinject(this);

        /*
         * Check if we have a user name; if not show wizard before connecting
         */
        if (!preferenceUtils.hasUserName() || !preferenceUtils.hasServer()) {
            if (!configureXMPPAccount())
                return;
        }

        String username = preferenceUtils.getUserName();
        String password = preferenceUtils.getPassword();

        try {
            ConnectionConfiguration connectionConfiguration = this
                .getConnectionConfiguration();

            if (isConnected()) {
                disconnect();
            }

            this.setConnectionState(ConnectionState.CONNECTING, null);
            this.connection = new XMPPConnection(connectionConfiguration);
            this.connection.connect();

            // add connection listener so we get notified if it will be closed
            if (this.smackConnectionListener == null) {
                this.smackConnectionListener = new SafeConnectionListener(log,
                    new XMPPConnectionListener());
            }
            connection.addConnectionListener(this.smackConnectionListener);

            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

            /*
             * TODO SS Possible race condition, as our packet listeners are
             * registered only after the login (in CONNECTED Connection State),
             * so we might for instance receive subscription requests even
             * though we do not have a packet listener running yet!
             */
            this.connection.login(username, password, Saros.RESOURCE);
            /* other people can now send invitations */

            this.myJID = new JID(this.connection.getUser());
            setConnectionState(ConnectionState.CONNECTED, null);

        } catch (URISyntaxException e) {
            log.info("URI not parseable: " + e.getInput());
            Utils.popUpFailureMessage("URI not parseable", e.getInput()
                + " is not a valid URI.", failSilently);

        } catch (IllegalArgumentException e) {
            log.info("Illegal argument: " + e.getMessage());
            setConnectionState(ConnectionState.ERROR, null);
            Utils.popUpFailureMessage("Illegal argument", e.getMessage(),
                failSilently);

        } catch (XMPPException e) {
            Throwable t = e.getWrappedThrowable();
            Exception cause = (t != null) ? (Exception) t : e;

            setConnectionState(ConnectionState.ERROR, cause);

            if (cause instanceof SaslException) {
                Utils.popUpFailureMessage("Error Connecting via SASL",
                    cause.getMessage(), failSilently);
            } else {
                String question;
                if (cause instanceof UnknownHostException) {
                    log.info("Unknown host: " + cause);

                    question = "The XMPP/Jabber server '"
                        + this.connection.getHost()
                        + "' could not be found.\n\n"
                        + "Do you want edit your XMPP/Jabber account?";
                } else {
                    log.info("xmpp: " + cause.getMessage(), cause);

                    question = "Could not connect to XMPP/Jabber server.\n"
                        + "Server: " + this.connection.getHost() + "\n"
                        + "User: " + username + "\n\n"
                        + "Do you want edit your XMPP/Jabber account?";
                }
                if (Utils.popUpYesNoQuestion("Connecting Error", question,
                    failSilently)) {
                    if (configureXMPPAccount())
                        connect(failSilently);
                }
            }
        } catch (Exception e) {
            log.warn("Unhandled exception:", e);
            setConnectionState(ConnectionState.ERROR, e);
            Utils.popUpFailureMessage(
                "Error Connecting",
                "Could not connect to server '" + connection.getHost()
                    + "' as user '" + username + "'.\nErrorMessage was:\n"
                    + e.getMessage(), failSilently);
        }
    }

    /**
     * Returns a @link{ConnectionConfiguration} representing the settings stored
     * in the Eclipse preferences.
     * 
     * This methods will fall back to use DNS SRV to get the XMPP port of the
     * server if the SERVER configured in the preferences does not have an
     * explicit port set.
     * 
     * Also this method configures the SecurityMode and the reconnection
     * attribute.
     * 
     * @throws URISyntaxException
     *             If the server string in the preferences cannot be transformed
     *             into an URI
     */
    protected ConnectionConfiguration getConnectionConfiguration()
        throws URISyntaxException {
        String serverString = preferenceUtils.getServer();

        URI uri;
        uri = (serverString.matches("://")) ? new URI(serverString) : new URI(
            "jabber://" + serverString);

        String server = uri.getHost();
        if (server == null) {
            throw new URISyntaxException(preferenceUtils.getServer(),
                "The XMPP/Jabber server address is invalid: " + serverString);
        }

        ProxyInfo proxyInfo = getProxyInfo(uri.getHost());
        ConnectionConfiguration conConfig = null;

        if (uri.getPort() < 0) {
            conConfig = proxyInfo == null ? new ConnectionConfiguration(
                uri.getHost()) : new ConnectionConfiguration(uri.getHost(),
                proxyInfo);
        } else {
            conConfig = proxyInfo == null ? new ConnectionConfiguration(
                uri.getHost(), uri.getPort()) : new ConnectionConfiguration(
                uri.getHost(), uri.getPort(), proxyInfo);
        }

        /*
         * TODO It has to ask the user, if s/he wants to use non-TLS connections
         * with PLAIN SASL if TLS is not supported by the server.
         * 
         * TODO use MessageDialog and Util.runSWTSync() to provide a password
         * call-back if the user has no password set in the preferences.
         */
        conConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        /*
         * We handle reconnecting ourselves
         */
        conConfig.setReconnectionAllowed(false);

        return conConfig;
    }

    /**
     * Returns @link{IProxyService} if there is a registered service otherwise
     * null.
     */
    protected IProxyService getProxyService() {
        BundleContext bundleContext = getBundle().getBundleContext();
        ServiceReference serviceReference = bundleContext
            .getServiceReference(IProxyService.class.getName());
        return (IProxyService) bundleContext.getService(serviceReference);
    }

    /**
     * Returns a @link{ProxyInfo}, if a configuration of a proxy for the given
     * host is available. If @link{IProxyData} is of type
     * 
     * @link{IProxyData.HTTP_PROXY_TYPE it tries to use Smacks
     * @link{ProxyInfo.forHttpProxy and if it is of type
     * @link{IProxyData.SOCKS_PROXY_TYPE then it tries to use Smacks
     * @link{ProxyInfo.forSocks5Proxy otherwise it returns null.
     * 
     * @param host
     *            The host to which you want to connect to.
     * 
     * @return Returns a @link{ProxyInfo} if available otherwise null.
     * 
     * @SuppressWarnings("deprecation") -> getProxyDataForHost replacement is
     *                                  only available in Eclipse 3.5
     */
    @SuppressWarnings("deprecation")
    protected ProxyInfo getProxyInfo(String host) {
        IProxyService ips = getProxyService();
        if (ips == null || !ips.isProxiesEnabled())
            return null;

        for (IProxyData pd : ips.getProxyDataForHost(host)) {
            if (IProxyData.HTTP_PROXY_TYPE.equals(pd.getType())) {
                return ProxyInfo.forHttpProxy(pd.getHost(), pd.getPort(),
                    pd.getUserId(), pd.getPassword());
            } else if (IProxyData.SOCKS_PROXY_TYPE.equals(pd.getType())) {
                return ProxyInfo.forSocks5Proxy(pd.getHost(), pd.getPort(),
                    pd.getUserId(), pd.getPassword());
            }
        }

        return null;
    }

    /**
     * Disconnects (if currently connected)
     * 
     * @blocking
     * 
     * @post this.myjid == null && this.connection == null &&
     *       this.connectionState == ConnectionState.NOT_CONNECTED
     */
    public void disconnect() {
        if (isConnected()) {
            setConnectionState(ConnectionState.DISCONNECTING, null);

            disconnectInternal();

            setConnectionState(ConnectionState.NOT_CONNECTED, null);
        }
        this.myJID = null;

        // Make a sanity check on the connection and connection state
        if (this.connectionState != ConnectionState.NOT_CONNECTED) {
            log.warn("Connection state is out of sync");
            this.connectionState = ConnectionState.NOT_CONNECTED;
        }
        if (this.connection != null) {
            log.warn("Connection has not been closed");
            this.connection = null;
        }
    }

    protected void disconnectInternal() {
        if (connection != null) {
            try {
                connection.removeConnectionListener(smackConnectionListener);
                connection.disconnect();
            } catch (RuntimeException e) {
                log.warn("Could not disconnect old XMPPConnection: ", e);
            } finally {
                connection = null;
            }
        }
    }

    public boolean isConnected() {
        return this.connectionState == ConnectionState.CONNECTED;
    }

    /**
     * @return the current state of the connection.
     */
    public ConnectionState getConnectionState() {
        return this.connectionState;
    }

    /**
     * @return an error string that contains the error message for the current
     *         connection error if the state is {@link ConnectionState#ERROR} or
     *         <code>null</code> if there is another state set.
     */
    public Exception getConnectionError() {
        return this.connectionError;
    }

    /**
     * @return the currently established connection or <code>null</code> if
     *         there is none.
     */
    public XMPPConnection getConnection() {
        return this.connection;
    }

    /**
     * Opens the appropriate {@link IWizard} to configure the active
     * {@link XMPPAccount}.<br/>
     * If no active {@link XMPPAccount} exists the {@link ConfigurationWizard}
     * is used instead.
     * 
     * @return
     */
    public boolean configureXMPPAccount() {
        if (xmppAccountStore == null)
            SarosPluginContext.initComponent(this);
        XMPPAccount xmppAccount = xmppAccountStore.getActiveAccount();
        if (xmppAccount == null) {
            return (WizardUtils.openSarosConfigurationWizard() != null);
        } else {
            return (WizardUtils.openEditXMPPAccountWizard(xmppAccount) != null);
        }
    }

    public void addListener(IConnectionListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(IConnectionListener listener) {
        this.listeners.remove(listener);
    }

    protected void assertConnection() throws XMPPException {
        if (!isConnected()) {
            throw new XMPPException("No connection");
        }
    }

    /**
     * Sets a new connection state and notifies all connection listeners.
     */
    protected void setConnectionState(ConnectionState state, Exception error) {

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
            log.debug(prefix + "New Connection State == " + state);
        } else {
            log.error(prefix + "New Connection State == " + state, error);
        }

        for (IConnectionListener listener : this.listeners) {
            try {
                listener.connectionStateChanged(this.connection, state);
            } catch (RuntimeException e) {
                log.error("Internal error in setConnectionState:", e);
            }
        }
    }

    protected void setupLoggers() {
        try {
            log = Logger.getLogger("de.fu_berlin.inf.dpp");

            PropertyConfigurator.configureAndWatch("log4j.properties",
                REFRESH_SECONDS * 1000);

        } catch (SecurityException e) {
            System.err.println("Could not start logging:");
            e.printStackTrace();
        }
    }

    protected class XMPPConnectionListener implements ConnectionListener {

        public void connectionClosed() {
            // self inflicted, controlled disconnect
            setConnectionState(ConnectionState.NOT_CONNECTED, null);
        }

        public void connectionClosedOnError(Exception e) {

            log.error("XMPP Connection Error: ", e);

            if (e.toString().equals("stream:error (conflict)")) {

                disconnect();

                Utils.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        MessageDialog.openError(
                            EditorAPI.getShell(),
                            "Connection error",
                            "You have been disconnected from XMPP/Jabber, because of a resource conflict.\n"
                                + "This indicates that you might have logged on again using the same XMPP/Jabber account"
                                + " and XMPP resource, for instance using Saros or an other instant messaging client.");
                    }
                });
                return;
            }

            // Only try to reconnect if we did achieve a connection...
            if (getConnectionState() != ConnectionState.CONNECTED)
                return;

            setConnectionState(ConnectionState.ERROR, e);

            disconnectInternal();

            Utils.runSafeAsync(log, new Runnable() {
                public void run() {

                    Map<JID, Integer> expectedSequenceNumbers = Collections
                        .emptyMap();
                    if (sessionManager.getSarosSession() != null) {
                        expectedSequenceNumbers = sessionManager
                            .getSarosSession().getSequencer()
                            .getExpectedSequenceNumbers();
                    }

                    // HACK Improve this hack to stop an infinite reconnect
                    int i = 0;
                    final int CONNECTION_RETRIES = 15;

                    while (!isConnected() && i++ < CONNECTION_RETRIES) {

                        try {
                            log.info("Reconnecting...("
                                + InetAddress.getLocalHost().toString() + ")");

                            connect(true);
                            if (!isConnected())
                                Thread.sleep(5000);

                        } catch (InterruptedException e) {
                            log.error("Code not designed to be interruptable",
                                e);
                            Thread.currentThread().interrupt();
                            return;
                        } catch (UnknownHostException e) {
                            log.info("Could not get localhost, maybe the network interface is down.");
                        }
                    }

                    if (isConnected()) {
                        sessionManager.onReconnect(expectedSequenceNumbers);
                        log.debug("XMPP reconnected");
                    }
                }
            });
        }

        public void reconnectingIn(int seconds) {
            // TODO maybe using Smack reconnect is better
            assert false : "Reconnecting is disabled";
            // setConnectionState(ConnectionState.CONNECTING, null);
        }

        public void reconnectionFailed(Exception e) {
            // TODO maybe using Smack reconnect is better
            assert false : "Reconnecting is disabled";
            // setConnectionState(ConnectionState.ERROR, e.getMessage());
        }

        public void reconnectionSuccessful() {
            // TODO maybe using Smack reconnect is better
            assert false : "Reconnecting is disabled";
            // setConnectionState(ConnectionState.CONNECTED, null);
        }
    }

    /**
     * Returns a string representing the Saros Version number for instance
     * "9.5.7.r1266"
     * 
     * This method only returns a valid version string after the plugin has been
     * started.
     * 
     * This is equivalent to the bundle version.
     */
    public String getVersion() {
        return sarosVersion;
    }

    /**
     * Returns the configuration setting for enabled auto follow mode
     * 
     * @return the state of the feature as <code> boolean</code>
     */
    // TODO move to PreferenceUtils
    public boolean getAutoFollowEnabled() {
        return getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_FOLLOW_MODE);
    }

}
