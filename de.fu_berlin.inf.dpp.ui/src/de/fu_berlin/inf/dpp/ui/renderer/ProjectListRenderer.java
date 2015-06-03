/**
 * 
 */
package de.fu_berlin.inf.dpp.ui.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRoot;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree;
import de.fu_berlin.inf.dpp.ui.model.ProjectTreeNode;
import de.fu_berlin.inf.dpp.ui.model.ProjectTreeNode.NodeType;

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

    private static final Logger LOG = Logger
        .getLogger(ProjectListRenderer.class);

    private IWorkspaceRoot workspaceRoot;
    private List<ProjectTree> projectModels;
    private final String ERROR_MSG = "Couldn't load all workspace resources.";

    /**
     * @param workspaceRoot
     *            the root of the workspace.
     */
    public ProjectListRenderer(IWorkspaceRoot workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    @Override
    public synchronized void render(IJQueryBrowser browser) {
        // This shouldn't be done for every browser only the updateModelEvent
        // needs to be called multiple times. TODO: Change the Render Interface
        // to avoid multiple model creation.
        createProjectTreeModel(browser);
        renderProjectTreeModel(browser);
    }

    private void createProjectTreeModel(IJQueryBrowser browser) {
        this.projectModels = new ArrayList<ProjectTree>();

        try {
            // TODO: Use workspaceRoot.getProjects() instead
            IResource[] rootMembers = workspaceRoot.members();
            List<IProject> projects = new ArrayList<IProject>();

            for (IResource rootMember : rootMembers) {
                if (rootMember.getType() == IResource.PROJECT) {
                    projects.add((IProject) rootMember);
                }
            }

            for (IProject project : projects) {
                ProjectTreeNode root = new ProjectTreeNode(
                    project.getFullPath(), NodeType.PROJECT);

                if (project.members().length == 0) {
                    projectModels.add(new ProjectTree(root, project.getName()));
                    return;
                }

                root.getMembers().add(membersToModel(project, browser));
                this.projectModels.add(new ProjectTree(root, root
                    .getDisplayName()));
            }
        } catch (IOException e) {
            // TODO: provide JS API for errors instead of alerts?
            browser.run("alert(" + ERROR_MSG + ")");
            LOG.error(
                "Failed to build Projectmodel while exctracting resources: ", e);
        }
    }

    private ProjectTreeNode membersToModel(IContainer res,
        IJQueryBrowser browser) {

        ProjectTreeNode curNode = new ProjectTreeNode(res.getFullPath(),
            NodeType.FILE);

        // Determinate whether this is a Project, a Folder, or a File
        switch (res.getType()) {
        case IResource.PROJECT:
            curNode.setType(NodeType.PROJECT);
            break;
        case IResource.FOLDER:
            curNode.setType(NodeType.FOLDER);
            break;
        case IResource.FILE:
            curNode.setType(NodeType.FILE);
            break;
        default:
            break;
        }
        try {
            IResource[] members = res.members();
            // Go thought all members and add them to the model
            for (IResource resource : members) {
                switch (resource.getType()) {
                case IResource.PROJECT:
                    curNode.getMembers().add(
                        membersToModel((IProject) resource, browser));
                    break;
                case IResource.FOLDER:
                    curNode.getMembers().add(
                        membersToModel((IFolder) resource, browser));
                    break;

                case IResource.FILE:
                    curNode.getMembers().add(
                        new ProjectTreeNode(resource.getFullPath(),
                            NodeType.FILE));
                    break;
                }
            }
            return curNode;

        } catch (IOException e) {
            // TODO: provide JS API for errors instead of alerts?
            browser.run("alert(" + ERROR_MSG + ")");
            LOG.error(
                "Failed to build Projectmodel while exctracting resources: ", e);
        }

        throw new RuntimeException("Failed to extract member from resources"
            + res.toString());
    }

    private void renderProjectTreeModel(IJQueryBrowser browser) {
        Gson jsModel = new Gson();
        String json = jsModel.toJson(projectModels);
        browser.run("SarosApi.updateProjectsTree(" + json + ")");
    }
}
