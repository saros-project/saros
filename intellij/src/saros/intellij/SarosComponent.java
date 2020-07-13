package saros.intellij;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStream;
import javax.swing.KeyStroke;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.lookup.MainMapLookup;
import org.apache.logging.log4j.status.StatusLogger;

/**
 * Component that is initialized when a application is loaded. It initializes the logging, shortcuts
 * and the {@link IntellijApplicationLifecycle} singleton.
 */
public class SarosComponent {

  /**
   * This is the plugin ID that identifies the saros plugin in the IDEA ecosystem. It is set in
   * plugin.xml with the tag <code>id</code>.
   */
  public static final String PLUGIN_ID = "saros";

  private static final String LOG4J2_CONFIG_FILENAME = "saros_log4j2.xml";

  public SarosComponent() {
    setupLoggers();

    Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
    keymap.addShortcut(
        "ActivateSarosToolWindow",
        new KeyboardShortcut(
            KeyStroke.getKeyStroke(KeyEvent.VK_F11, java.awt.event.InputEvent.ALT_DOWN_MASK),
            null));

    try {
      InputStream sarosProperties =
          SarosComponent.class.getClassLoader().getResourceAsStream("saros.properties");

      if (sarosProperties == null) {
        StatusLogger.getLogger()
            .warn(
                "could not initialize Saros properties because "
                    + "the 'saros.properties' file could not be found on the "
                    + "current JAVA class path");
      } else {
        System.getProperties().load(sarosProperties);
        sarosProperties.close();
      }
    } catch (Exception e) {
      StatusLogger.getLogger().error("could not load saros property file 'saros.properties'", e);
    }

    IntellijApplicationLifecycle.getInstance().start();
  }

  private void setupLoggers() {
    try {
      final String logDir = PathManager.getLogPath() + File.separator + "SarosLogs";
      final boolean isDebugMode = Boolean.getBoolean("saros.debug");
      final Level logLevel = isDebugMode ? Level.ALL : Level.INFO;

      // make arguments accessible in the log configuration file
      MainMapLookup.setMainArguments("logDir", logDir, "logLevel", logLevel.name());

      // trigger reconfiguration with new properties and config file
      Configurator.initialize(
          null,
          ConfigurationSource.fromResource(
              LOG4J2_CONFIG_FILENAME, SarosComponent.class.getClassLoader()));
    } catch (RuntimeException e) {
      StatusLogger.getLogger().error("initializing loggers failed", e);
      e.printStackTrace();
    }
  }
}
