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
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import de.fu_berlin.inf.dpp.util.Utils;

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
     * Sub-namespace for the server. It is used advertise when a server is
     * active.
     */
    public static final String NAMESPACE_SERVER = NAMESPACE + ".server"; //$NON-NLS-1$

    /**
     * The name of the resource identifier used by Saros when connecting to the
     * XMPP server (for instance when logging in as john@doe.com, Saros will
     * connect using john@doe.com/Saros)
     * 
     * @deprecated Do not use this resource identifier to build a fully
     *             qualified Jabber identifier, e.g the logic connects to a XMPP
     *             server as foo@bar/Saros but the assigned Jabber identifier
     *             may be something like foo@bar/Saros765E18ED !
     */
    @Deprecated
    public final static String RESOURCE = "Saros"; //$NON-NLS-1$

    private String sarosVersion;

    private String sarosFeatureID;

    private ISarosSessionManager sessionManager;

    private PreferenceUtils preferenceUtils;

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

    public static final Random RANDOM = new Random();

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
            + Utils.getPlatformInfo());

        sarosContext = new SarosContext(new SarosEclipseContextFactory(this,
            new SarosCoreContextFactory()), dotMonitor);

        SarosPluginContext.setSarosContext(sarosContext);

        sarosFeatureID = SAROS + "_" + sarosVersion; //$NON-NLS-1$

        // Remove the Bundle if an instance of it was already registered
        sarosContext.removeComponent(Bundle.class);
        sarosContext.addComponent(Bundle.class, getBundle());

        connectionHandler = sarosContext.getComponent(ConnectionHandler.class);
        sessionManager = sarosContext.getComponent(ISarosSessionManager.class);
        preferenceUtils = sarosContext.getComponent(PreferenceUtils.class);

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

        int favoriteColorID = preferenceUtils.getFavoriteColorID();

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

        connectionHandler.convertAccountStore(getPreferenceStore(),
            getSecurePrefs());
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
                "ShutdownProcess", log, new Runnable() { //$NON-NLS-1$
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
            configPrefs = new ConfigurationScope().getNode(SAROS);
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
        try {
            if (securePrefs != null) {
                securePrefs.flush();
            }
        } catch (IOException e) {
            log.error("Exception when trying to store secure preferences: " + e);
        }
    }

    protected void setupLoggers() {
        /*
         * HACK this is not the way OSGi works but it currently fulfill its
         * purpose
         */
        final ClassLoader contextClassLoader = Thread.currentThread()
            .getContextClassLoader();

        try {
            // change the context class loader so Log4J will find the appenders
            Thread.currentThread().setContextClassLoader(
                STFController.class.getClassLoader());

            PropertyConfigurator.configure(Saros.class.getClassLoader()
                .getResource("saros.log4j.properties")); //$NON-NLS-1$
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
}
