package saros.intellij;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import javax.swing.KeyStroke;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.jetbrains.annotations.NotNull;
import saros.intellij.ui.util.NotificationPanel;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;
import saros.util.ThreadUtils;

/**
 * Component that is initialized when a project is loaded. It initializes the logging, shortcuts and
 * the {@link IntellijProjectLifecycle} singleton.
 */
public class SarosComponent implements com.intellij.openapi.components.ProjectComponent {
  /**
   * This is the plugin ID that identifies the saros plugin in the IDEA ecosystem. It is set in
   * plugin.xml with the tag <code>id</code>.
   */
  public static final String PLUGIN_ID = "saros";

  private final Logger log;

  private final Project project;

  private final IntellijProjectLifecycle intellijProjectLifecycle;

  /**
   * Loads the given project project into the current <code>IntellijProjectLifecycle</code>.
   *
   * @param project the opened project
   */
  public SarosComponent(final Project project) {
    loadLoggers();
    log = Logger.getLogger(SarosComponent.class);

    this.project = project;

    intellijProjectLifecycle = IntellijProjectLifecycle.getInstance(project);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Initializes the Saros plugin lifecycle and other needed components.
   */
  @Override
  public void initComponent() {
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
        LogLog.warn(
            "could not initialize Saros properties because "
                + "the 'saros.properties' file could not be found on the "
                + "current JAVA class path");
      } else {
        System.getProperties().load(sarosProperties);
        sarosProperties.close();
      }
    } catch (Exception e) {
      LogLog.error("could not load saros property file 'saros.properties'", e);
    }

    intellijProjectLifecycle.start();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Does nothing for the Saros plugin.
   */
  @Override
  public void disposeComponent() {
    // NOP
  }

  @Override
  @NotNull
  public String getComponentName() {
    return "Saros";
  }

  /**
   * {@inheritDoc}
   *
   * <p>Does nothing for the Saros plugin as the initialization is already done in {@link
   * #initComponent()}.
   */
  @Override
  public void projectOpened() {
    // NOP
  }

  /**
   * {@inheritDoc}
   *
   * <p>Stops the currently running session if such a session exists.
   */
  @Override
  public void projectClosed() {
    ISarosSessionManager sarosSessionManager =
        intellijProjectLifecycle.getSarosContext().getComponent(ISarosSessionManager.class);

    ISarosSession currentSession = sarosSessionManager.getSession();

    if (project.equals(intellijProjectLifecycle.getProject())) {
      if (currentSession != null) {
        log.debug(
            "Leaving current session as the project "
                + project.getName()
                + " containing the shared module was closed");

        ThreadUtils.runSafeAsync(
            "StopSession",
            log,
            () -> sarosSessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT));
      }

      for (Project openProject : ProjectManager.getInstance().getOpenProjects()) {
        NotificationPanel.showProjectSpecificWarning(
            openProject,
            "Saros is currently in a headless state as you closed the shareable project! "
                + "Please open a new project that you would like to share a module of before trying to share something!\n"
                + "More information about this issue will be given in the Saros/I release notes.",
            "Saros entered a headless state");
      }
    }
  }

  public static boolean isSwtBrowserEnabled() {
    return Boolean.getBoolean("saros.swtbrowser");
  }

  private void loadLoggers() {
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    try {
      // change the context class loader so Log4J will find
      // the SarosLogFileAppender
      Thread.currentThread().setContextClassLoader(SarosComponent.class.getClassLoader());

      PropertyConfigurator.configure(
          SarosComponent.class.getClassLoader().getResource("saros.log4j.properties"));
    } catch (RuntimeException e) {
      LogLog.error("initializing loggers failed", e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }
}
