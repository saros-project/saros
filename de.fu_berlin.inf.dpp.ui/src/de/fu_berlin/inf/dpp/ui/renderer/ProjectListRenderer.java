/**
 * 
 */
package de.fu_berlin.inf.dpp.ui.renderer;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRoot;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.manager.ProjectListManager;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree;

/**
 * This class is responsible for sending the Project list to the HTML UI.
 * 
 * Because the concepts of what an actual project is, varies from IDE to IDE
 * this class use the {@link ProjectTree} model to form a common representation
 * for the UI to display.
 * 
 * This class use a {@link IWorkspaceRoot} to received the necessary data for
 * the model creation.
 */
public class ProjectListRenderer extends Renderer {

    private static final Logger LOG = LogManager
        .getLogger(ProjectListRenderer.class);

    private ProjectListManager projectListManager;

    /**
     * Created by PicoContainer
     * 
     * @param projectListManager
     *            the projectListManager
     * @see HTMLUIContextFactory
     */
    public ProjectListRenderer(ProjectListManager projectListManager) {
        this.projectListManager = projectListManager;
    }

    @Override
    public synchronized void render(IJQueryBrowser browser) {
        // The project model creation shouldn't be done for every browser, only
        // the `SarosApi.trigger('updateProjectTrees', json)` needs to be called
        // for each page this renderer is associated with.
        // TODO: Change the Renderer interface to avoid multiple model
        // creations.

        try {
            projectListManager.createProjectModels();
        } catch (IOException e) {
            LOG.error("Failed to load workspace resources: ", e);
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.ERR_SESSION_PROJECT_LIST_IOEXCEPTION);
        }

        // FIXME: Workspaces with a big number of files cause massive
        // performance problems. See
        // https://github.com/saros-project/saros/issues/71
        JavaScriptAPI.updateProjects(browser,
            projectListManager.getProjectModels());
    }
}
