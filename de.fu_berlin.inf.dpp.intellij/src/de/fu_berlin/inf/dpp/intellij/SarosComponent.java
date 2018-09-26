package de.fu_berlin.inf.dpp.intellij;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;
import org.jetbrains.annotations.NotNull;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Component that is initalized when a project is loaded.
 * It initializes the logging, shortcuts and the {@link IntellijProjectLifecycle} singleton.
 */
public class SarosComponent
    implements com.intellij.openapi.components.ProjectComponent {

    /**
     * This is the plugin ID that identifies the saros plugin in the IDEA
     * ecosystem.
     */
    public static final String PLUGIN_ID = "de.fu_berlin.inf.dpp.intellij";

    public SarosComponent(final Project project) {
        loadLoggers();

        Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
        keymap.addShortcut("ActivateSarosToolWindow", new KeyboardShortcut(
            KeyStroke.getKeyStroke(KeyEvent.VK_F11,
                java.awt.event.InputEvent.ALT_DOWN_MASK), null));

        try {
            InputStream sarosProperties = SarosComponent.class.getClassLoader()
                .getResourceAsStream("saros.properties");

            if (sarosProperties == null) {
                StatusLogger.getLogger().warn("could not initialize Saros properties because "
                    + "the 'saros.properties' file could not be found on the "
                    + "current JAVA class path");
            } else {
                System.getProperties().load(sarosProperties);
                sarosProperties.close();
            }
        } catch (Exception e) {
           StatusLogger.getLogger() 
                .error("could not load saros property file 'saros.properties'",
                    e);
        }

        IntellijProjectLifecycle.getInstance(project).start();
    }

    public static boolean isSwtBrowserEnabled() {
        return Boolean.getBoolean("saros.swtbrowser");
    }

    private void loadLoggers() {

        try {
            String logDir = PathManager.getLogPath() + File.separator + "SarosLogs";

            System.setProperty("log4j.configurationFile", "saros_log4j2.xml");
            System.setProperty("logging.logDir", logDir);
            System.setProperty("logging.consoleLevel", "warn");

            // trigger reconfiguration with new properties
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            context.reconfigure();

        } catch (RuntimeException e) {
            StatusLogger.getLogger().error("initializing loggers failed", e);
        }
    }

    @Override
    public void initComponent() {
        //NOP
    }

    @Override
    public void disposeComponent() {
        //NOP
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Saros";
    }

    @Override
    public void projectOpened() {
        //TODO: Update project
    }

    @Override
    public void projectClosed() {
        //TODO: Update project
    }
}
