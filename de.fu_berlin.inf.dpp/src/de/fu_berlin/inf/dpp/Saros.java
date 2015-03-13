/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.feedback.FeedbackPreferences;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.browser.EclipseHTMLUIContextFactory;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import de.fu_berlin.inf.dpp.versioning.VersionManager;

/**
 * The main plug-in of Saros.
 * 
 * @author rdjemili
 * @author coezbek
 */
@Component(module = "core")
public class Saros extends AbstractUIPlugin {

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */
    public static final String PLUGIN_ID = "de.fu_berlin.inf.dpp"; //$NON-NLS-1$

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

    private static final String VERSION_COMPATIBILITY_PROPERTY_FILE = "version.comp"; //$NON-NLS-1$

    private String sarosVersion;

    private String sarosFeatureID;

    private ISarosSessionManager sessionManager;

    private de.fu_berlin.inf.dpp.preferences.Preferences preferences;

    private ConnectionHandler connectionHandler;

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
     * be accessed over {@link #getGlobalPreferences()} from outside this class.
     */
    protected Preferences configPrefs;

    /**
     * The secure preferences store, used to store sensitive data that may (at
     * the user's option) be stored encrypted.
     */
    protected ISecurePreferences securePrefs;

    protected Logger log;

    /**
     * @JTourBusStop 4, Invitation Process:
     * 
     *               If you haven't already read about PicoContainer, stop and
     *               do so now (http://picocontainer.codehaus.org).
     * 
     *               Saros uses PicoContainer to manage dependencies on our
     *               behalf. The SarosContext class encapsulates our usage of
     *               PicoContainer. It's a well documented class, so take a look
     *               at it.
     */

    protected SarosContext sarosContext;

    /**
     * Create the shared instance.
     */
    public Saros() {

        try {
            InputStream sarosProperties = Saros.class.getClassLoader()
                .getResourceAsStream("saros.properties"); //$NON-NLS-1$

            if (sarosProperties == null) {
                LogLog
                    .warn("could not initialize Saros properties because the 'saros.properties'"
                        + " file could not be found on the current JAVA class path");
            } else {
                System.getProperties().load(sarosProperties);
                sarosProperties.close();
            }
        } catch (Exception e) {
            LogLog.error(
                "could not load saros property file 'saros.properties'", e); //$NON-NLS-1$
        }

        // Only start a DotGraphMonitor if asserts are enabled (aka debug mode)
        assert (dotMonitor = new DotGraphMonitor()) != null;

        setInitialized(false);
        setDefault(this);
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

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {

        super.start(context);

        setupLoggers();

        sarosVersion = getBundle().getVersion().toString();

        log.info("Starting Saros " + sarosVersion + " running:\n"
            + getPlatformInfo());

        ArrayList<ISarosContextFactory> factories = new ArrayList<ISarosContextFactory>();
        factories.add(new SarosEclipseContextFactory(this));
        factories.add(new SarosCoreContextFactory());

        if (isSwtBrowserEnabled()) {
            factories.add(new SarosHTMLUIContextFactory());
            factories.add(new EclipseHTMLUIContextFactory());
        }

        sarosContext = new SarosContext(factories, dotMonitor);

        SarosPluginContext.setSarosContext(sarosContext);

        sarosFeatureID = PLUGIN_ID + "_" + sarosVersion; //$NON-NLS-1$

        // Remove the Bundle if an instance of it was already registered
        sarosContext.removeComponent(Bundle.class);
        sarosContext.addComponent(Bundle.class, getBundle());

        connectionHandler = sarosContext.getComponent(ConnectionHandler.class);
        sessionManager = sarosContext.getComponent(ISarosSessionManager.class);
        preferences = sarosContext
            .getComponent(de.fu_berlin.inf.dpp.preferences.Preferences.class);

        // additional initialization

        FeedbackPreferences.setPreferences(sarosContext
            .getComponent(Preferences.class));

        initVersionCompatibilityChart(VERSION_COMPATIBILITY_PROPERTY_FILE,
            sarosContext.getComponent(VersionManager.class));

        // Make sure that all components in the container are
        // instantiated
        sarosContext.getComponents(Object.class);

        isInitialized = true;

        /*
         * If other colors than the ones we support are set in the
         * PreferenceStore, overwrite them
         */
        SarosAnnotation.resetColors();

        /*
         * Hack for MARCH 2013 release, ensure a good favorite color
         * distribution for upgrading clients
         */

        int favoriteColorID = preferences.getFavoriteColorID();

        if (!UserColorID.isValid(favoriteColorID)
            && getPreferenceStore().getBoolean(
                "FAVORITE_COLOR_ID_HACK_CREATE_RANDOM_COLOR")) {
            favoriteColorID = new Random().nextInt(SarosAnnotation.SIZE);
            log.debug("autogenerated favorite color id is: " + favoriteColorID);
            getPreferenceStore().setValue(
                PreferenceConstants.FAVORITE_SESSION_COLOR_ID, favoriteColorID);
        }

        getPreferenceStore().setValue(
            "FAVORITE_COLOR_ID_HACK_CREATE_RANDOM_COLOR", false);

        convertAccountStore();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {

        // TODO Devise a general way to stop and dispose our components
        saveGlobalPreferences();
        saveSecurePrefs();

        if (dotMonitor != null) {
            File file = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                .toFile();
            file = new File(file, ".metadata"); //$NON-NLS-1$
            file = new File(file, "saros-" + sarosFeatureID + ".dot"); //$NON-NLS-1$ //$NON-NLS-2$
            log.info("Saving Saros architecture diagram dot file: "
                + file.getAbsolutePath());
            dotMonitor.save(file);
        }

        try {
            Thread shutdownThread = ThreadUtils.runSafeAsync(
                "dpp-shutdown", log, new Runnable() { //$NON-NLS-1$
                    @Override
                    public void run() {

                        try {

                            sessionManager.stopSarosSession();
                            connectionHandler.disconnect();
                        } finally {
                            /*
                             * Always shutdown the network to ensure a proper
                             * cleanup(currently only UPNP)
                             */

                            /*
                             * This will cause dispose() to be called on all
                             * components managed by PicoContainer which
                             * implement {@link Disposable}.
                             */
                            sarosContext.dispose();
                        }
                    }
                });

            shutdownThread.join(10000);
            if (shutdownThread.isAlive())
                log.error("could not shutdown Saros gracefully");

        } finally {
            super.stop(context);
        }

        isInitialized = false;
        setDefault(null);
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
    public synchronized Preferences getGlobalPreferences() {
        // TODO Singleton-Pattern code smell: ConfigPrefs should be a @component
        if (configPrefs == null) {
            configPrefs = new ConfigurationScope().getNode(PLUGIN_ID);
        }
        return configPrefs;
    }

    /**
     * Saves the global preferences to disk. Should be called at least before
     * the bundle is stopped to prevent loss of data. Can be called whenever
     * found necessary.
     */
    public synchronized void saveGlobalPreferences() {
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

    protected void setupLoggers() {
        /*
         * HACK this is not the way OSGi works but it currently fulfill its
         * purpose
         */
        final ClassLoader contextClassLoader = Thread.currentThread()
            .getContextClassLoader();

        final boolean isDebugMode = Boolean
            .getBoolean("de.fu_berlin.inf.dpp.debug") || isDebugging(); //$NON-NLS-1$

        final String log4jPropertyFile = isDebugMode ? "saros_debug.log4j.properties" //$NON-NLS-1$
            : "saros_release.log4j.properties"; //$NON-NLS-1$

        try {
            // change the context class loader so Log4J will find the appenders
            Thread.currentThread().setContextClassLoader(
                Saros.class.getClassLoader());

            PropertyConfigurator.configure(Saros.class.getClassLoader()
                .getResource(log4jPropertyFile));
        } catch (RuntimeException e) {
            System.err.println("initializing log support failed"); //$NON-NLS-1$
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        log = Logger.getLogger("de.fu_berlin.inf.dpp"); //$NON-NLS-1$

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
     * @deprecated Only of one-time use to convert from the old, IDE-dependent
     *             format to the new, IDE-independent format. Will be removed in
     *             Release n+2
     */
    @Deprecated
    public void convertAccountStore() {

        final XMPPAccountStore accountStore = sarosContext
            .getComponent(XMPPAccountStore.class);

        if (!accountStore.isEmpty()) {
            log.debug("skipping conversion of old XMPP accounts, because there are already new ones");
            return;
        }

        try {
            de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore oldStore = new de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore(
                getPreferenceStore(), getSecurePrefs());

            if (oldStore.isEmpty())
                return;

            de.fu_berlin.inf.dpp.accountManagement.XMPPAccount oldActiveAccount;

            oldActiveAccount = oldStore.getActiveAccount();

            List<de.fu_berlin.inf.dpp.accountManagement.XMPPAccount> accounts = oldStore
                .getAllAccounts();

            accounts.remove(oldActiveAccount);
            accounts.add(0, oldActiveAccount);

            for (de.fu_berlin.inf.dpp.accountManagement.XMPPAccount account : accounts) {
                log.debug("converting old account to new one: " + account);

                try {
                    accountStore.createAccount(account.getUsername(),
                        account.getPassword(), account.getDomain(),
                        account.getServer(), account.getPort(),
                        account.useTLS(), account.useSASL());
                } catch (RuntimeException e) {
                    log.error("failed to convert old account: " + account, e);
                }
            }

        } catch (RuntimeException e) {
            log.error("failed to convert old account store", e);
        }
    }

    /**
     * @deprecated remove after next release
     */
    @Deprecated
    private synchronized ISecurePreferences getSecurePrefs() {

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

    /**
     * @deprecated remove after next release
     */
    @Deprecated
    private synchronized void saveSecurePrefs() {
        try {
            if (securePrefs != null) {
                securePrefs.flush();
            }
        } catch (IOException e) {
            log.error("Exception when trying to store secure preferences: " + e);
        }
    }

    private void initVersionCompatibilityChart(final String filename,
        final VersionManager versionManager) {

        if (versionManager == null) {
            log.error("no version manager component available");
            return;
        }

        final InputStream in = Saros.class.getClassLoader()
            .getResourceAsStream(filename);

        final Properties chart = new Properties();

        if (in == null) {
            log.warn("could not find compatibility property file: " + filename);
            return;
        }

        try {
            chart.load(in);
        } catch (IOException e) {
            log.warn("could not read compatibility property file: " + filename,
                e);

            return;
        } finally {
            IOUtils.closeQuietly(in);
        }

        versionManager.setCompatibilityChart(chart);
    }

    private String getPlatformInfo() {

        String javaVersion = System.getProperty("java.version",
            "Unknown Java Version");
        String javaVendor = System.getProperty("java.vendor", "Unknown Vendor");
        String os = System.getProperty("os.name", "Unknown OS");
        String osVersion = System.getProperty("os.version", "Unknown Version");
        String hardware = System.getProperty("os.arch", "Unknown Architecture");

        StringBuilder sb = new StringBuilder();

        sb.append("  Java Version: " + javaVersion + "\n");
        sb.append("  Java Vendor: " + javaVendor + "\n");
        sb.append("  Eclipse Runtime Version: "
            + Platform.getBundle("org.eclipse.core.runtime").getVersion()
                .toString() + "\n");
        sb.append("  Operating System: " + os + " (" + osVersion + ")\n");
        sb.append("  Hardware Architecture: " + hardware);

        return sb.toString();
    }

    /**
     * Feature toggle for displaying Saros in a web browser in an additional
     * view. Also checks if required bundle is present.
     * 
     * @return true if this feature is enabled, false otherwise
     */
    private static boolean isSwtBrowserEnabled() {
        // TODO store constant string elsewhere
        return Platform.getBundle("de.fu_berlin.inf.dpp.ui") != null
            && Boolean.getBoolean("saros.swtbrowser");
    }
}
