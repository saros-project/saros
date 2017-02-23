package de.fu_berlin.inf.dpp.intellij;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.jetbrains.annotations.NotNull;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

/**
 * Component that is initalized when a project is loaded.
 * It initializes the logging, shortcuts and the {@link IntelliJSarosLifecycle} singleton.
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

        IntellijProjectLifecycle.getInstance(project).start();
    }

    public static boolean isSwtBrowserEnabled() {
        return Boolean.getBoolean("saros.swtbrowser");
    }

    private void loadLoggers() {
        final ClassLoader contextClassLoader = Thread.currentThread()
            .getContextClassLoader();

        try {
            // change the context class loader so Log4J will find
            // the SarosLogFileAppender
            Thread.currentThread()
                .setContextClassLoader(SarosComponent.class.getClassLoader());

            PropertyConfigurator.configure(SarosComponent.class.getClassLoader()
                .getResource("saros.log4j.properties"));
        } catch (RuntimeException e) {
            LogLog.error("initializing loggers failed", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
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
