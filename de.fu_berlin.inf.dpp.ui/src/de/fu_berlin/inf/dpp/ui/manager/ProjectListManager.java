package de.fu_berlin.inf.dpp.ui.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
/**
 * 
 */
public class ProjectListManager {
    private IWorkspaceRoot workspaceRoot;
    private List<ProjectTree> projectModels;

    private Map<String, IProject> projectNameToProject;
    private Map<IPath, IResource> pathToResource;

    /**
     * @param workspaceRoot
     *            the workspace root of the current workspace
     */
    public ProjectListManager(IWorkspaceRoot workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
        // This assumes that a project must have a unique project name in
        // every IDE. Multiple projects with the same name will result in
        // errors.
        this.projectNameToProject = new HashMap<String, IProject>();
        this.pathToResource = new HashMap<IPath, IResource>();
    }

    /**
     * <p>
     * Creates a new list of {@link ProjectTree} models containing all available
     * projects in the current workspace. While creating the model, mappings
     * between every {@link ProjectTree} and its respective {@link IProject}
     * instance, as well as between every {@link ProjectTreeNode} and its
     * {@link IResource} will be created.
     * </p>
     * <p>
     * If there are no projects inside the Workspace, this will create an empty
     * list of {@link ProjectTree}s. It's up to the caller to handle this state
     * and inform the user properly.
     * </p>
     * <p>
     * Due to the fact that we only allow to share Resources that are related to
     * a projects, files inside the workspace that are not part of a project
     * will be ignored and not added to the model.
     * </p>
     * <p>
     * Note that this will recreate previous mappings and models, and cause an
     * iteration over all files inside the workspace.
     * </p>
     * 
     * @throws IOException
     *             if a file couldn't be extracted. This error will not be
     *             logged, so its up to the caller to log it if he so desires.
     */
    public void createAndMapProjectModels() throws IOException {
        this.projectModels = new ArrayList<ProjectTree>();
        this.projectNameToProject = new HashMap<String, IProject>();
        this.pathToResource = new HashMap<IPath, IResource>();

        if (workspaceRoot.getProjects().length == 0) {
            // No projects inside this workspace
            return;
        }
        try {
            for (IProject project : workspaceRoot.getProjects()) {
                ProjectTreeNode root = new ProjectTreeNode(
                    project.getFullPath(), NodeType.PROJECT);

                if (project.members().length == 0) {
                    // Empty project
                    ProjectTree pTree = new ProjectTree(root, project.getName());
                    projectModels.add(pTree);

                    projectNameToProject.put(pTree.getProjectName(), project);
                    pathToResource.put(root.getPath(), project);
                    break;
                }
                root.getMembers().add(createModel(project));

                ProjectTree pTree = new ProjectTree(root, root.getDisplayName());
                projectModels.add(pTree);

                projectNameToProject.put(pTree.getProjectName(), project);
                pathToResource.put(root.getPath(), project);
            }
        } catch (IOException e) {
            throw new IOException(
                "Failed to build Projectmodel while exctracting resources:", e);
        }
    }

    /**
     * Creates the {@link ProjectTreeNode} for a given resources, while creating
     * a mapping to it's {@link IResource}.
     * 
     * @param resource
     *            the resource to create the model from.
     * @return the model for the given resources
     */
    private ProjectTreeNode createModel(IContainer resource) throws IOException {
        ProjectTreeNode node = new ProjectTreeNode(resource.getFullPath(),
            NodeType.FILE);
        pathToResource.put(node.getPath(), resource);

        // Determinate whether this is a Project, a Folder, or a File
        switch (resource.getType()) {
        case IResource.PROJECT:
            node.setType(NodeType.PROJECT);
            break;
        case IResource.FOLDER:
            node.setType(NodeType.FOLDER);
            break;
        case IResource.FILE:
            node.setType(NodeType.FILE);
            break;
        default:
            break;
        }

        // Go thought all members and add them to the model
        for (IResource member : resource.members()) {
            switch (member.getType()) {
            case IResource.PROJECT:
                node.getMembers().add(createModel((IProject) member));
                break;
            case IResource.FOLDER:
                node.getMembers().add(createModel((IFolder) member));
                break;
            case IResource.FILE:
                node.getMembers().add(
                    new ProjectTreeNode(member.getFullPath(), NodeType.FILE));
                break;
            }
        }
        return node;
    }

    /**
     * <p>
     * For every given {@link ProjectTree} a map of {@link IProject}:
     * {@link IResource}s will be created. In order to do so a given
     * {@link ProjectTree} is identified by its name, a {@link ProjectTreeNode}
     * will be identified by its path.
     * </p>
     * <p>
     * If there is no match in the mapping for the given model, an empty map
     * will be created. If {@link #createAndMapProjectModels()} hasn't been
     * called yet this returns a list of empty maps.
     * </p>
     * 
     * @param projectTreeModels
     *            the list of {@link ProjectTree}s to extract the resources list
     *            from
     * @return a list of mappings for all found resources from the given
     *         ProjectTreeModel
     */
    public List<Map<IProject, List<IResource>>> getProjectToResourcesMaps(
        ProjectTree[] projectTreeModels) {
        List<Map<IProject, List<IResource>>> resourcesToShare = new ArrayList<Map<IProject, List<IResource>>>();

        for (ProjectTree pTree : projectTreeModels) {
            resourcesToShare.add(getProjectToResourcesMap(pTree));
        }
        return resourcesToShare;
    }

    /**
     * <p>
     * This will create a map of {@link IProject}: {@link IResource}s for the
     * given ProjectTree. {@link ProjectTree} is identified by it's name, a
     * {@link ProjectTreeNode} is identified by it's path. The resource is only
     * added if it still exist in the workspace.
     * </p>
     * <p>
     * If there is no match in the mapping for the given model, an empty list
     * will be created. This also return an empty map, if
     * {@link #createAndMapProjectModels()} hasn't been called yet.
     * </p>
     * 
     * @param projectTree
     *            the {@link ProjectTree}'s to extract the resource list from
     * 
     * @return a map of resources associated to the given ProjectTreeModel, or
     *         an empty map if no association is present.
     */
    public Map<IProject, List<IResource>> getProjectToResourcesMap(
        ProjectTree projectTree) {
        Map<IProject, List<IResource>> result = new HashMap<IProject, List<IResource>>();
        IProject project;
        List<IResource> resources = new ArrayList<IResource>();

        for (ProjectTree pTree : this.projectModels) {
            if (projectNameToProject.containsKey(pTree.getProjectName())) {
                project = projectNameToProject.get(pTree.getProjectName());

                List<ProjectTreeNode> nodes = new ArrayList<ProjectTreeNode>();
                getNodes(projectTree.getRoot(), nodes);

                for (ProjectTreeNode node : nodes) {
                    if (pathToResource.containsKey(node.getPath())) {
                        IResource resource = pathToResource.get(node.getPath());
                        if (resource.exists()) {
                            resources.add(resource);
                        }
                    }
                }
                result.put(project, resources);
                return result;
            }
        }
        return result;
    }

    /**
     * <p>
     * For every given {@link ProjectTree} a list of {@link IProject}:
     * {@link IResource}s will be created and added to the return list. In order
     * to do so a given {@link ProjectTree} is identified by its name, a
     * {@link ProjectTreeNode} will be identified by its path.
     * </p>
     * 
     * <p>
     * If there is no match in the mapping for a given {@link ProjectTree},
     * nothing will be added. If {@link #createAndMapProjectModels()} hasn't
     * been called yet this returns a empty list.
     * </p>
     * 
     * @param projectTreeModels
     *            the list of {@link ProjectTree}s to extract the resources list
     *            from
     * @return a list of mappings for all found resources from the given
     *         ProjectTreeModel
     */
    public List<IResource> getAllResources(ProjectTree[] projectTreeModels) {
        List<IResource> resourcesToShare = new ArrayList<IResource>();

        for (ProjectTree pTree : projectTreeModels) {
            if (!getResources(pTree).isEmpty()) {
                resourcesToShare.addAll(getResources(pTree));
            }
        }
        return resourcesToShare;
    }

    /**
     * <p>
     * This will create a list of all {@link IResource}s for the given
     * ProjectTree. {@link ProjectTree} is identified by it's name , a
     * {@link ProjectTreeNode} is identified by it's path. The resource is only
     * added if it still exist in the workspace.
     * </p>
     * <p>
     * If there is no match in the mapping for the given model, an empty list
     * will be created. This also return an empty list, if
     * {@link #createAndMapProjectModels()} hasn't been called yet.
     * </p>
     * 
     * @param projectTree
     *            the {@link ProjectTree}'s to extract the resource list from
     * 
     * @return a list of resources associated to the given ProjectTreeModel, or
     *         an empty map if no association is present.
     */
    public List<IResource> getResources(ProjectTree projectTree) {
        List<IResource> resources = new ArrayList<IResource>();

        for (ProjectTree pTree : this.projectModels) {
            if (projectNameToProject.containsKey(pTree.getProjectName())) {
                List<ProjectTreeNode> nodes = new ArrayList<ProjectTreeNode>();
                getNodes(projectTree.getRoot(), nodes);

                for (ProjectTreeNode node : nodes) {
                    if (pathToResource.containsKey(node.getPath())) {
                        IResource resource = pathToResource.get(node.getPath());
                        if (resource.exists()) {
                            resources.add(resource);
                        }
                    }
                }
                return resources;
            }
        }
        // No match was found return empty list
        return resources;
    }

    private ProjectTreeNode getNodes(ProjectTreeNode curNode,
        List<ProjectTreeNode> nodes) {

        for (ProjectTreeNode projectTreeNode : curNode.getMembers()) {
            nodes.add(getNodes(projectTreeNode, nodes));
        }
        return curNode;
    }

    /**
     * @return the project models of the current workspace, or null if
     *         {@link #createAndMapProjectModels()} hasn't been called yet.
     */
    public List<ProjectTree> getProjectModels() {
        return projectModels;
    }

}