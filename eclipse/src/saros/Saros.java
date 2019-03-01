package saros;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import saros.annotations.Component;
import saros.editor.annotations.SarosAnnotation;
import saros.util.ThreadUtils;
import saros.versioning.VersionManager;

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
   * <p>This class manages the lifecycle of the Saros plug-in, contains some important supporting
   * data members and provides methods for the integration of Saros into Eclipse.
   *
   * <p>Eclipse will instantiate this class during startup.
   */

  /** This is the Bundle-SymbolicName (a.k.a the pluginID) */
  public static final String PLUGIN_ID = "saros.eclipse"; // $NON-NLS-1$

  private static final String VERSION_COMPATIBILITY_PROPERTY_FILE = "version.comp"; // $NON-NLS-1$

  private static boolean isInitialized;

  private EclipsePluginLifecycle lifecycle;

  /**
   * @JTourBusStop 2, Some Basics:
   *
   * <p>Preferences are managed by Eclipse-provided classes. Most are kept by Preferences, but some
   * sensitive data (like user account data) is kept in a SecurePreference.
   *
   * <p>If you press Ctrl+Shift+R and type in "*preference*" you will see every class in Saros that
   * deals with preferences. Classes named "*PreferencePage" implement individual pages within the
   * Eclipse preferences's Saros section. Preference labels go in PreferenceConstants.java.
   */

  /**
   * The global plug-in preferences, shared among all workspaces. Should only be accessed over
   * {@link #getGlobalPreferences()} from outside this class.
   */
  private Preferences globalPreferences;

  private Logger log;

  public Saros() {

    try {
      InputStream sarosProperties =
          Saros.class.getClassLoader().getResourceAsStream("saros.properties"); // $NON-NLS-1$

      if (sarosProperties == null) {
        LogLog.warn(
            "could not initialize Saros properties because the 'saros.properties'"
                + " file could not be found on the current JAVA class path");
      } else {
        System.getProperties().load(sarosProperties);
        sarosProperties.close();
      }
    } catch (Exception e) {
      LogLog.error("could not load saros property file 'saros.properties'", e); // $NON-NLS-1$
    }

    setInitialized(false);

    lifecycle = EclipsePluginLifecycle.getInstance(this);
  }

  private static void setInitialized(boolean initialized) {
    isInitialized = initialized;
  }

  /**
   * Returns true if the Saros instance has been initialized so that calling {@link
   * SarosPluginContext#initComponent(Object)} will be well defined.
   */
  public static boolean isInitialized() {
    return isInitialized;
  }

  /** This method is called upon plug-in activation */
  @Override
  public void start(BundleContext context) throws Exception {

    super.start(context);

    setupLoggers();

    log.info(
        "Starting Saros " + getBundle().getVersion() + " running:\n" + getPlatformInformation());

    lifecycle.start();

    initVersionCompatibilityChart(
        VERSION_COMPATIBILITY_PROPERTY_FILE,
        lifecycle.getSarosContext().getComponent(VersionManager.class));

    isInitialized = true;

    /*
     * If other colors than the ones we support are set in the
     * PreferenceStore, overwrite them
     */
    SarosAnnotation.resetColors();
  }

  @Override
  public void stop(BundleContext context) throws Exception {

    saveGlobalPreferences();

    try {
      Thread shutdownThread =
          ThreadUtils.runSafeAsync(
              "dpp-shutdown",
              log,
              new Runnable() { //$NON-NLS-1$
                @Override
                public void run() {
                  lifecycle.stop();
                }
              });

      shutdownThread.join(10000);
      if (shutdownThread.isAlive()) log.error("could not shutdown Saros gracefully");

    } finally {
      super.stop(context);
    }

    isInitialized = false;
  }

  /**
   * Returns the global {@link Preferences} with {@link ConfigurationScope} for this plug-in or null
   * if the node couldn't be determined.
   *
   * <p>The returned Preferences can be accessed concurrently by multiple threads of the same JVM
   * without external synchronization. If they are used by multiple JVMs no guarantees can be made
   * concerning data consistency (see {@link Preferences} for details).
   *
   * @return the preferences node for this plug-in containing global preferences that are visible
   *     for all workspaces of this eclipse installation
   */
  public synchronized Preferences getGlobalPreferences() {
    // TODO Singleton-Pattern code smell: ConfigPrefs should be a @component
    if (globalPreferences == null) {
      globalPreferences = new ConfigurationScope().getNode(PLUGIN_ID);
    }
    return globalPreferences;
  }

  /**
   * Saves the global preferences to disk. Should be called at least before the bundle is stopped to
   * prevent loss of data. Can be called whenever found necessary.
   */
  public synchronized void saveGlobalPreferences() {
    /*
     * Note: If multiple JVMs use the global preferences and the underlying
     * backing store, they might not always work with latest data, e.g. when
     * using multiple instances of the same eclipse installation.
     */
    if (globalPreferences == null) return;
    try {
      globalPreferences.flush();
    } catch (BackingStoreException e) {
      log.error("Couldn't store global plug-in preferences", e);
    }
  }

  /**
   * Feature toggle for displaying Saros in a web browser in an additional view. Also checks if
   * required bundle is present.
   *
   * @return <code>true</code> iff the feature is enabled and the UI bundle is present.
   */
  public static boolean useHtmlGui() {
    // TODO store constant string elsewhere
    return Platform.getBundle("saros.ui") != null && Boolean.getBoolean("saros.swtbrowser");
  }

  private void setupLoggers() {
    /*
     * HACK this is not the way OSGi works but it currently fulfill its
     * purpose
     */
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    final boolean isDebugMode = Boolean.getBoolean("saros.debug") || isDebugging(); // $NON-NLS-1$

    final String log4jPropertyFile =
        isDebugMode
            ? "saros_debug.log4j.properties" //$NON-NLS-1$
            : "saros_release.log4j.properties"; //$NON-NLS-1$

    try {
      // change the context class loader so Log4J will find the appenders
      Thread.currentThread().setContextClassLoader(Saros.class.getClassLoader());

      PropertyConfigurator.configure(Saros.class.getClassLoader().getResource(log4jPropertyFile));
    } catch (RuntimeException e) {
      System.err.println("initializing log support failed"); // $NON-NLS-1$
      e.printStackTrace();
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    log = Logger.getLogger(this.getClass());
  }

  private void initVersionCompatibilityChart(
      final String filename, final VersionManager versionManager) {

    if (versionManager == null) {
      log.error("no version manager component available");
      return;
    }

    final InputStream in = Saros.class.getClassLoader().getResourceAsStream(filename);

    final Properties chart = new Properties();

    if (in == null) {
      log.warn("could not find compatibility property file: " + filename);
      return;
    }

    try {
      chart.load(in);
    } catch (IOException e) {
      log.warn("could not read compatibility property file: " + filename, e);

      return;
    } finally {
      IOUtils.closeQuietly(in);
    }

    versionManager.setCompatibilityChart(chart);
  }

  private String getPlatformInformation() {

    String javaVersion = System.getProperty("java.version", "Unknown Java Version");
    String javaVendor = System.getProperty("java.vendor", "Unknown Vendor");
    String os = System.getProperty("os.name", "Unknown OS");
    String osVersion = System.getProperty("os.version", "Unknown Version");
    String hardware = System.getProperty("os.arch", "Unknown Architecture");

    StringBuilder sb = new StringBuilder();

    sb.append("  Java Version: " + javaVersion + "\n");
    sb.append("  Java Vendor: " + javaVendor + "\n");
    sb.append(
        "  Eclipse Runtime Version: "
            + Platform.getBundle("org.eclipse.core.runtime").getVersion().toString()
            + "\n");
    sb.append("  Operating System: " + os + " (" + osVersion + ")\n");
    sb.append("  Hardware Architecture: " + hardware);

    return sb.toString();
  }
}
