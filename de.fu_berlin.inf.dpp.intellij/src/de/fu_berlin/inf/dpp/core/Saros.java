package de.fu_berlin.inf.dpp.core;

import java.util.ArrayList;

import org.apache.log4j.helpers.LogLog;

import com.intellij.openapi.project.Project;

import de.fu_berlin.inf.dpp.ISarosContextFactory;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.context.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.context.SarosIntellijContextFactory;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.intellij.ui.swt_browser.SwtLibLoader;
import de.fu_berlin.inf.dpp.misc.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * Saros plugin class for bundling globally necessary variables like project.
 */
public class Saros {

    /**
     * This is the plugin ID that identifies the saros plugin in the IDEA
     * ecosystem.
     */
    public static final String PLUGIN_ID = "de.fu_berlin.inf.dpp.intellij";

    private static Saros instance;

    private static boolean isInitialized;

    private Project project;

    private Preferences preferences;

    private IWorkspace workspace;

    private SarosContext sarosContext;

    /**
     * Returns true if the Saros instance has been initialized so that calling
     * {@link SarosContext#reinject(Object)} will be well defined.
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Checks if Saros was already initialized by create(). Throws an
     * IllegalStateException if not initialized.
     * 
     * @throws IllegalStateException
     */
    public static void checkInitialized() {
        if (!isInitialized()) {
            LogLog.error("Saros not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }

    /**
     * Creates a new Saros singleton instance from a project.
     * 
     * @param project
     * @return
     */
    public synchronized static Saros create(Project project) {
        if (instance == null) {
            instance = new Saros(project);
            instance.start();
        }
        return instance;
    }

    private Saros(Project project) {
        this.project = project;
        this.workspace = new Workspace(project);
    }

    public static boolean isSwtBrowserEnabled() {
        return Boolean.getBoolean("saros.swtbrowser");
    }

    /**
     * If not initialized yet, this method initializes fields, the
     * SarosPluginContext and the XMPPConnectionService.
     */
    public void start() {

        if (isInitialized) {
            return;
        }

        ArrayList<ISarosContextFactory> factories = new ArrayList<ISarosContextFactory>();
        factories.add(new SarosIntellijContextFactory(this));
        factories.add(new SarosCoreContextFactory());

        if (isSwtBrowserEnabled()) {
            SwtLibLoader.loadSwtLib();
            factories.add(new HTMLUIContextFactory());
        }

        sarosContext = new SarosContext(factories, new DotGraphMonitor());

        SarosPluginContext.setSarosContext(sarosContext);
        preferences = sarosContext.getComponent(Preferences.class);

        isInitialized = true;
        // Make sure that all components in the container are
        // instantiated
        sarosContext.getComponents(Object.class);
    }

    // FIXME: Properly stop network and context classes
    void stop() {
        isInitialized = false;
    }

    public Project getProject() {
        return project;
    }

    public IWorkspace getWorkspace() {
        return workspace;
    }
}
