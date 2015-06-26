package de.fu_berlin.inf.dpp.ui.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRoot;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree;
import de.fu_berlin.inf.dpp.ui.model.ProjectTreeNode;
import de.fu_berlin.inf.dpp.ui.model.ProjectTreeNode.NodeType;

/**
 * This class is responsible for creating and managing the {@link ProjectTree}
 * models for the HTML UI. It also provides a mapping of the {@link IProject}
 * and its {@link IResource}s associated with a ProjectTree.
 */
public class ProjectListManager {
    private static final Logger LOG = Logger
        .getLogger(ProjectListManager.class);

    private IWorkspaceRoot workspaceRoot;
    private List<ProjectTree> projectModels;

    private Map<String, IProject> projectNameToProject;
    private Map<IPath, IResource> pathToResource;

    // TODO:Since this will be prompted to the user, this could be more precise
    private static final String ERROR_MSG = "Couldn't load all workspace resources. Some resources might be missing.";

    /**
     * @param workspaceRoot
     *            the workspaceroot of the current workspace
     */
    public ProjectListManager(IWorkspaceRoot workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
        this.projectNameToProject = new HashMap<String, IProject>();
        this.pathToResource = new HashMap<IPath, IResource>();
    }

    /**
     * Creates a new list of {@link ProjectTree} models containing all available
     * projects in the current workspace. While creating the model, mappings
     * between every {@link ProjectTree} and its respective {@link IProject}
     * instance, as well as between every {@link ProjectTreeNode} and its
     * {@link IResource} will be created.
     * 
     * Note that this will recreate previous mappings and models, and cause an
     * iteration over all files inside the workspace.
     * 
     * @throws IOException
     *             if a file couldn't be extracted.
     */
    public void createAndMapProjectModels() throws IOException {
        this.projectModels = new ArrayList<ProjectTree>();
        this.projectNameToProject = new HashMap<String, IProject>();
        this.pathToResource = new HashMap<IPath, IResource>();

        try {
            for (IProject p : workspaceRoot.getProjects()) {
                ProjectTreeNode root = new ProjectTreeNode(p.getFullPath(),
                    NodeType.PROJECT);

                if (p.members().length == 0) {
                    ProjectTree pTree = new ProjectTree(root, p.getName());
                    projectModels.add(pTree);

                    projectNameToProject.put(pTree.getProjectName(), p);
                    pathToResource.put(root.getPath(), p);
                    break;
                }
                root.getMembers().add(createModel(p));

                ProjectTree pTree = new ProjectTree(root, root.getDisplayName());
                projectModels.add(pTree);

                projectNameToProject.put(pTree.getProjectName(), p);
                pathToResource.put(root.getPath(), p);
            }
        } catch (IOException e) {
            LOG.error(
                "Failed to build Projectmodel while exctracting resources: ", e);
            throw e;
        }
    }

    /**
     * Creates the {@link ProjectTreeNode} for a given resources, while creating
     * a mapping to it's {@link IResource}.
     * 
     * @param curRes
     *            the resource to create the model from.
     * @return the model for the given resources
     */
    private ProjectTreeNode createModel(IContainer curRes) throws IOException {
        ProjectTreeNode curNode = new ProjectTreeNode(curRes.getFullPath(),
            NodeType.FILE);
        pathToResource.put(curNode.getPath(), curRes);

        // Determinate whether this is a Project, a Folder, or a File
        switch (curRes.getType()) {
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

        // Go thought all members and add them to the model
        for (IResource resource : curRes.members()) {
            switch (resource.getType()) {
            case IResource.PROJECT:
                curNode.getMembers().add(createModel((IProject) resource));
                break;
            case IResource.FOLDER:
                curNode.getMembers().add(createModel((IFolder) resource));
                break;
            case IResource.FILE:
                curNode.getMembers().add(
                    new ProjectTreeNode(resource.getFullPath(), NodeType.FILE));
                break;
            }
        }
        return curNode;
    }

    /**
     * This will compare the existing {@link ProjectTree} models with the given
     * models. In order to do so, the given {@link ProjectTree} are identified
     * and compared by their names, a {@link ProjectTreeNode} will be identified
     * and compared by its path. For every given {@link ProjectTree} a map of
     * {@link IProject}:{@link IResource}s will be created.
     * 
     * @param projectTreeModels
     *            the list of {@link ProjectTree}s to extract the resource list
     * @return a list of all resourcesToShare created from the given
     *         ProjectTreeModel
     */
    public List<Map<IProject, List<IResource>>> getResourcesToShare(
        ProjectTree[] projectTreeModels) {
        List<Map<IProject, List<IResource>>> resourcesToShare = new ArrayList<Map<IProject, List<IResource>>>();

        for (ProjectTree pTree : projectTreeModels) {
            resourcesToShare.add(getResources(pTree));
        }
        return resourcesToShare;
    }

    /**
     * This will compare the existing {@link ProjectTree} model with the given
     * model and search for a match. In order to do so, the given
     * {@link ProjectTree} are identified and compared by it's name, a
     * {@link ProjectTreeNode} will be identified and compared by it's path.
     * 
     * @param projectTree
     *            the {@link ProjectTree}'s to extract the resource list
     * 
     * @return a map of resources created from the given ProjectTreeModel, or an
     *         empty map if no match was found.
     */
    public Map<IProject, List<IResource>> getResources(ProjectTree projectTree) {
        Map<IProject, List<IResource>> result = new HashMap<IProject, List<IResource>>();
        IProject project = null;
        List<IResource> resources = new ArrayList<IResource>();

        for (ProjectTree pTree : this.projectModels) {
            if (projectNameToProject.containsKey(pTree.getProjectName())) {
                project = projectNameToProject.get(pTree.getProjectName());

                List<ProjectTreeNode> nodes = new ArrayList<ProjectTreeNode>();
                getNodes(projectTree.getRoot(), nodes);

                for (ProjectTreeNode node : nodes) {
                    if (pathToResource.containsKey(node.getPath())) {
                        resources.add(pathToResource.get(node.getPath()));
                    }
                }
                result.put(project, resources);
                return result;
            }
        }
        // No match was found
        return result;
    }

    private ProjectTreeNode getNodes(ProjectTreeNode curNode,
        List<ProjectTreeNode> nodes) {

        for (ProjectTreeNode projectTreeNode : curNode.getMembers()) {
            nodes.add(getNodes(projectTreeNode, nodes));
        }
        return curNode;
    }

    /**
     * @return the projectModels, or null if no models are created
     */
    public List<ProjectTree> getProjectModels() {
        return projectModels;
    }

}