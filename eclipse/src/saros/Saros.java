package saros;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.lookup.MainMapLookup;
import org.apache.logging.log4j.status.StatusLogger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import saros.annotations.Component;
import saros.util.ThreadUtils;

/** The main plug-in of Saros. */
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

  private static final String LOG4J2_CONFIG_FILENAME = "saros_log4j2.xml";

  private final IWorkbenchListener workbenchShutdownListener =
      new IWorkbenchListener() {

        @Override
        public void postShutdown(final IWorkbench workbench) {
          stopLifeCycle();
        }

        @Override
        public boolean preShutdown(final IWorkbench workbench, final boolean forced) {
          return true;
        }
      };

  private static boolean isInitialized;

  private EclipsePluginLifecycle lifecycle;
  private boolean isLifecycleStarted = false;

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
        StatusLogger.getLogger()
            .warn(
                "could not initialize Saros properties because the 'saros.properties'"
                    + " file could not be found on the current JAVA class path");
      } else {
        System.getProperties().load(sarosProperties);
        sarosProperties.close();
      }
    } catch (Exception e) {
      StatusLogger.getLogger()
          .error("could not load saros property file 'saros.properties'", e); // $NON-NLS-1$
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

    isLifecycleStarted = true;

    getWorkbench().addWorkbenchListener(workbenchShutdownListener);
    isInitialized = true;
  }

  @Override
  public void stop(BundleContext context) throws Exception {

    log.info("Stopping Saros " + getBundle().getVersion());

    saveGlobalPreferences();

    getWorkbench().removeWorkbenchListener(workbenchShutdownListener);

    try {
      stopLifeCycle();
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
      globalPreferences = ConfigurationScope.INSTANCE.getNode(PLUGIN_ID);
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

  /** Stops the Saros Eclipse life cycle. */
  private void stopLifeCycle() {

    log.debug("stopping lifecycle...");

    if (!isLifecycleStarted) {
      log.debug("lifecycle is already stopped");
      return;
    }

    isLifecycleStarted = false;

    final AtomicBoolean isLifeCycleStopped = new AtomicBoolean(false);
    final AtomicBoolean isTimeout = new AtomicBoolean(false);

    final Display display = Display.getCurrent();

    final Thread shutdownThread =
        ThreadUtils.runSafeAsync(
            "shutdown", //$NON-NLS-1$
            log,
            () -> {
              try {
                lifecycle.stop();
              } finally {
                isLifeCycleStopped.set(true);

                if (display != null) {
                  try {
                    display.wake();
                  } catch (SWTException ignore) {
                    // NOP
                  }
                }
              }
            });

    int threadTimeout = 10000;

    // must run the event loop or stopping the lifecycle will timeout
    if (display != null && !display.isDisposed()) {

      display.timerExec(
          threadTimeout,
          () -> {
            isTimeout.set(true);
            display.wake();
          });

      while (!isLifeCycleStopped.get() && !isTimeout.get()) {
        if (!display.readAndDispatch()) display.sleep();
      }

      if (!isLifeCycleStopped.get()) threadTimeout = 1;

      /*
       * fall through to log an error or wait until the thread terminated
       * even it already signal that the life cycle was stopped
       */
    }

    try {
      shutdownThread.join(threadTimeout);
    } catch (InterruptedException e) {
      log.warn("interrupted while waiting for the current lifecycle to stop");
      Thread.currentThread().interrupt();
    }

    if (shutdownThread.isAlive()) log.error("timeout while stopping lifecycle");

    log.debug("lifecycle stopped");
  }

  private void setupLoggers() {

    final boolean isDebugMode = Boolean.getBoolean("saros.debug") || isDebugging(); // $NON-NLS-1$

    try {
      final File pluginLogDir = new File(getStateLocation().toFile(), "log");
      final Level logLevel = isDebugMode ? Level.ALL : Level.WARN;

      MainMapLookup.setMainArguments("logDir", pluginLogDir.getPath(), "logLevel", logLevel.name());

      // trigger reconfiguration with new properties and config file
      Configurator.initialize(
          null,
          ConfigurationSource.fromResource(LOG4J2_CONFIG_FILENAME, Saros.class.getClassLoader()));
    } catch (RuntimeException e) {
      StatusLogger.getLogger().error("initializing loggers failed", e);
      e.printStackTrace();
    }

    log = Logger.getLogger(this.getClass());
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
