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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Random;

import javax.security.sasl.SaslException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.proxy.ProxyInfo;
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
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.UPnP.UPnPAccessWeupnp;
import de.fu_berlin.inf.dpp.net.UPnP.UPnPManager;
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

    /**
     * @JTourBusStop 1, Some Basics:
     * 
     *               This class manages the lifecycle of the Saros plug-in,
     *               contains some important supporting data members and
     *               provides methods for the integration of Saros into Eclipse.
     * 
     *               Browse the data members. Some are quite obvious (version,
     *               feature etc.) some need closer examination.
     * 
     */

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
    public final static String RESOURCE = "Saros"; //$NON-NLS-1$

    private String sarosVersion;

    private String sarosFeatureID;

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

    /**
     * @JTourBusStop 2, Some Basics:
     * 
     *               Preferences are managed by Eclipse-provided classes. Most
     *               are kept by Preferences, but some sensitive data (like user
     *               account data) is kept in a SecurePreference.
     * 
     *               If you press Ctrl+Shift+R and type in "*preference*" you
     *               will see every class in Saros that deals with preferences.
     *               Classes named "*PreferencePage" implement individual pages
     *               within the Eclipse preferences's Saros section. Preference
     *               labels go in PreferenceConstants.java.
     */

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

    /**
     * @JTourBusStop 4, Invitation Process:
     * 
     *               If you haven't already read about PicoContainer, stop and
     *               do so now (www.picocontainer.org).
     * 
     *               Saros uses PicoContainer to manage dependencies on our
     *               behalf. The SarosContext class encapsulates our usage of
     *               PicoContainer. It's a well documented class, so take a look
     *               at it.
     */

    protected SarosContext sarosContext;

    static {
        Roster.setDefaultSubscriptionMode(SubscriptionMode.accept_all);
    }

    /**
     * Create the shared instance.
     */
    public Saros() {

        try {
            InputStream sarosPropertiers = Saros.class.getClassLoader()
                .getResourceAsStream("de/fu_berlin/inf/dpp/saros.properties");
            System.getProperties().load(sarosPropertiers);
            sarosPropertiers.close();
        } catch (Exception e) {
            LogLog.error("could not load saros property file", e);
        }

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
            LogLog.error("Saros not initialized", new StackTrace()); //$NON-NLS-1$
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

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {

        super.start(context);

        sarosVersion = getBundle().getVersion().toString();

        sarosFeatureID = SAROS + "_" + sarosVersion; //$NON-NLS-1$

        setupLoggers();
        log.info("Starting Saros " + sarosVersion + " running:\n" //$NON-NLS-1$ //$NON-NLS-2$
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

        getSarosNet().initialize();

        sarosContext.getComponent(UPnPManager.class).init(
            new UPnPAccessWeupnp(), getPreferenceStore());

        // determine if auto-connect can and should be performed
        if (getPreferenceStore().getBoolean(PreferenceConstants.AUTO_CONNECT)
            && !sarosContext.getComponent(XMPPAccountStore.class).isEmpty()
            && sarosContext.getComponent(StatisticManager.class)
                .hasStatisticAgreement()
            && sarosContext.getComponent(ErrorLogManager.class)
                .hasErrorLogAgreement()) {
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
            File file = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                .toFile();
            file = new File(file, ".metadata");
            file = new File(file, "saros-" + sarosFeatureID + ".dot");
            log.info("Saving Saros architecture diagram dot file: " //$NON-NLS-1$
                + file.getAbsolutePath());
            dotMonitor.save(file);
        }

        try {

            sarosContext.getComponent(SarosSessionManager.class)
                .stopSarosSession();

            getSarosNet().uninitialize();
            // Remove UPnP port mapping for Saros
            UPnPManager upnpManager = sarosContext
                .getComponent(UPnPManager.class);
            if (upnpManager.isMapped())
                upnpManager.removeSarosPortMapping();

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
                log.error("Couldn't store global plug-in preferences", e); //$NON-NLS-1$
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
                File storeFile = new File(getStateLocation().toFile(), "/.pref"); //$NON-NLS-1$
                URI workspaceURI = storeFile.toURI();

                /*
                 * The SecurePreferencesFactory does not accept percent-encoded
                 * URLs, so we must decode the URL before passing it.
                 */
                String prefLocation = URLDecoder.decode(
                    workspaceURI.toString(), "UTF-8"); //$NON-NLS-1$
                URL prefURL = new URL(prefLocation);

                securePrefs = SecurePreferencesFactory.open(prefURL, null);
            } catch (MalformedURLException e) {
                log.error("Problem with URL when attempting to access secure preferences: " //$NON-NLS-1$
                    + e);
            } catch (IOException e) {
                log.error("I/O problem when attempting to access secure preferences: " //$NON-NLS-1$
                    + e);
            } finally {
                if (securePrefs == null)
                    securePrefs = SecurePreferencesFactory.getDefault();
            }
        }

        return securePrefs;
    }

    @Inject
    XMPPAccountStore xmppAccountStore;

    public synchronized void saveSecurePrefs() {
        try {
            if (securePrefs != null) {
                securePrefs.flush();
            }
        } catch (IOException e) {
            log.error("Exception when trying to store secure preferences: " + e); //$NON-NLS-1$
        }
    }

    protected void setupLoggers() {
        try {

            PropertyConfigurator.configure(Saros.class.getClassLoader()
                .getResource("saros.log4j.properties"));

            log = Logger.getLogger("de.fu_berlin.inf.dpp"); //$NON-NLS-1$
        } catch (SecurityException e) {
            System.err.println("Could not start logging:"); //$NON-NLS-1$
            e.printStackTrace();
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

    public SarosNet getSarosNet() {
        return sarosContext.getComponent(SarosNet.class);
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

    public static boolean isWorkbenchAvailable() {
        boolean result = false;
        try {
            result = PlatformUI.getWorkbench() != null;
        } catch (Exception e) {
            // do nothing ...
        }
        return result;
    }

    /**
     * Returns @link{IProxyService} if there is a registered service otherwise
     * null.
     */
    protected IProxyService getProxyService() {
        IProxyService result = null;
        if (Saros.isWorkbenchAvailable()) {
            BundleContext bundleContext = getBundle().getBundleContext();
            ServiceReference serviceReference = bundleContext
                .getServiceReference(IProxyService.class.getName());
            result = (IProxyService) bundleContext.getService(serviceReference);
        }
        return result;
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
    public ProxyInfo getProxyInfo(String host) {
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
    protected ConnectionConfiguration getConnectionConfiguration(
        String serverString) throws URISyntaxException {

        PreferenceUtils preferenceUtils = this.sarosContext
            .getComponent(PreferenceUtils.class);

        URI uri;
        uri = (serverString.matches("://")) ? new URI(serverString) : new URI( //$NON-NLS-1$
            "jabber://" + serverString); //$NON-NLS-1$

        String server = uri.getHost();
        if (server == null) {
            throw new URISyntaxException(preferenceUtils.getServer(),
                "The XMPP/Jabber server address is invalid: " + serverString); //$NON-NLS-1$
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

        if (xmppAccountStore.isEmpty())
            return (WizardUtils.openSarosConfigurationWizard() != null);

        return (WizardUtils.openEditXMPPAccountWizard(xmppAccountStore
            .getActiveAccount()) != null);
    }

    /**
     * @nonBlocking
     */
    public void asyncConnect() {
        Utils.runSafeAsync("Saros-AsyncConnect-", log, new Runnable() { //$NON-NLS-1$
                public void run() {
                    connect(false);
                }
            });
    }

    @Inject
    PreferenceUtils preferenceUtils;
    @Inject
    UPnPManager upnpManager;

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
        if (preferenceUtils == null)
            sarosContext.reinject(this);

        getSarosNet().setSettings(preferenceUtils.isDebugEnabled(),
            preferenceUtils.isLocalSOCKS5ProxyEnabled(),
            preferenceUtils.getFileTransferPort(), preferenceUtils.getStunIP(),
            preferenceUtils.getStunPort(),
            preferenceUtils.isAutoPortmappingEnabled());

        if (xmppAccountStore == null)
            SarosPluginContext.initComponent(this);

        if (xmppAccountStore.isEmpty() && !configureXMPPAccount())
            return;

        XMPPAccount account = xmppAccountStore.getActiveAccount();

        String username = account.getUsername();
        String password = account.getPassword();
        String server = account.getServer();

        // FIXME use domain and server values to connect

        /*
         * 
         * Google Talk users have to keep their server portion in the username;
         * see http://code.google.com/apis/talk/talk_developers_home.html
         */

        if (server.equalsIgnoreCase("gmail.com")

        || server.equalsIgnoreCase("googlemail.com")) {

            if (!username.contains("@")) {

                username += "@" + server;

            }
        }

        try {
            ConnectionConfiguration connectionConfiguration = this
                .getConnectionConfiguration(server);
            getSarosNet().connect(connectionConfiguration, username, password,
                failSilently);

        } catch (URISyntaxException e) {
            log.info("URI not parseable: " + e.getInput()); //$NON-NLS-1$
            Utils.popUpFailureMessage(Messages.Saros_0, e.getInput()
                + Messages.Saros_1, failSilently);

        } catch (IllegalArgumentException e) {
            log.info("Illegal argument: " + e.getMessage()); //$NON-NLS-1$

            Utils.popUpFailureMessage(Messages.Saros_2, e.getMessage(),
                failSilently);

        } catch (XMPPException e) {
            Throwable t = e.getWrappedThrowable();
            Exception cause = (t != null) ? (Exception) t : e;

            if (cause instanceof SaslException) {
                Utils.popUpFailureMessage(Messages.Saros_3, cause.getMessage(),
                    failSilently);
            } else {
                String question;
                if (cause instanceof UnknownHostException) {
                    log.info("Unknown host: " + cause); //$NON-NLS-1$

                    question = Messages.Saros_28 + Messages.Saros_29
                        + Messages.Saros_30;
                } else {
                    log.info("xmpp: " + cause.getMessage(), cause); //$NON-NLS-1$

                    question = Messages.Saros_32
                    // + "Server: " + this.connection.getHost() + "\n"
                        + Messages.Saros_33 + username + "\n\n" //$NON-NLS-2$
                        + Messages.Saros_30;
                }
                if (Utils.popUpYesNoQuestion(Messages.Saros_36, question,
                    failSilently)) {
                    if (configureXMPPAccount())
                        connect(failSilently);
                }
            }
        } catch (Exception e) {
            log.warn("Unhandled exception:", e); //$NON-NLS-1$
            Utils.popUpFailureMessage(Messages.Saros_36, Messages.Saros_39
                + username + Messages.Saros_40 + e.getMessage(), failSilently);
        }
    }
}
