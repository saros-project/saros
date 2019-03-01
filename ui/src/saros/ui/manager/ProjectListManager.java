package saros.ui.manager;

import static saros.filesystem.IResource.FILE;
import static saros.filesystem.IResource.FOLDER;
import static saros.filesystem.IResource.PROJECT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import saros.HTMLUIContextFactory;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspaceRoot;
import saros.ui.model.ProjectTree;
import saros.ui.model.ProjectTree.Node;
import saros.ui.model.ProjectTree.Node.Type;

/**
 * This class is responsible for creating and managing the {@link ProjectTree} models for the HTML
 * UI. It takes care of mapping the {@link IProject} and its {@link IResource}s to with a {@link
 * ProjectTree} with its {@link Node}s.
 *
 * <p>Call {@link #createProjectModels()} to create the models once, and use {@link
 * #getProjectModels()} to retrieve them. Call {@link #getAllResources(ProjectTree[])} to get back
 * the actual resources selected for sharing.
 */
// TODO Make this a core facade
public class ProjectListManager {

  private IWorkspaceRoot workspaceRoot;

  /**
   * This is for caching. In theory {@link #getProjectModels()} could call {@link
   * #createProjectModels()} directly every time.
   */
  private List<ProjectTree> projectModels;

  /**
   * Stores the relationship between the UI models instances and the actual resources, which would
   * otherwise get lost during the Java-JavaScript-Java handover.
   */
  private Map<Node, IResource> resourceMap;

  /**
   * Created by PicoContainer
   *
   * @param workspaceRoot the workspace root of the current workspace
   * @see HTMLUIContextFactory
   */
  public ProjectListManager(IWorkspaceRoot workspaceRoot) {
    this.workspaceRoot = workspaceRoot;

    this.projectModels = new ArrayList<ProjectTree>();
    this.resourceMap = new HashMap<Node, IResource>();
  }

  /**
   * Creates the {@link ProjectTree} models representing all available projects in the current
   * workspace. These models can be retrieved through {@link #getProjectModels()}. While creating
   * the models, a mapping between every {@link IResource} and its respective {@link Node} will be
   * created and stored for later usage.
   *
   * <p>If there are no projects inside the workspace, this will create an empty list of {@link
   * ProjectTree}s. It's up to the caller to handle this state and inform the user properly.
   *
   * <p>Note that this will recreate the models every time it's called, and thereby cause an
   * iteration over all files in the workspace.
   *
   * @throws IOException if a file couldn't be extracted. This error will not be logged, so it's up
   *     to the caller to handle it
   */
  public void createProjectModels() throws IOException {
    this.projectModels = new ArrayList<ProjectTree>();
    this.resourceMap = new HashMap<Node, IResource>();

    if (workspaceRoot.getProjects().length == 0) {
      // No projects inside this workspace | IDE
      return;
    }

    for (IProject project : workspaceRoot.getProjects()) {
      Node root;
      try {
        root = createModel(project);
      } catch (IOException e) {
        throw new IOException("Failed to build ProjectModel while extracting resources", e);
      }
      ProjectTree pTree = new ProjectTree(root);

      projectModels.add(pTree);
      resourceMap.put(root, project);
    }
  }

  /**
   * Creates the {@link Node} for a given container (project or folder), while creating a mapping to
   * its underlying {@link IResource}.
   *
   * @param container the resource to create the model from.
   * @return the model for the given resource
   */
  private Node createModel(IContainer container) throws IOException {
    // Go through all members and add them to the model recursively
    List<Node> members = new ArrayList<Node>();
    for (IResource member : container.members()) {
      Node memberNode = null;
      switch (member.getType()) {
        case PROJECT:
        case FOLDER:
          memberNode = createModel((IContainer) member);
          break;
        case FILE:
          memberNode = createModel((IFile) member);
          break;
        default:
          continue;
      }
      members.add(memberNode);
    }

    // We don't expect any other container types besides projects and folder
    // here
    Type type = (container.getType() == FOLDER) ? Type.FOLDER : Type.PROJECT;

    Node node = new Node(members, container.getFullPath().lastSegment(), type, true);

    resourceMap.put(node, container);

    return node;
  }

  /**
   * Creates the {@link Node} for a given file, while creating a mapping to its underlying {@link
   * IResource}.
   *
   * @param file the resource to create the model from.
   * @return the model for the given resource
   */
  private Node createModel(IFile file) {
    Node memberNode = Node.fileNode(file.getFullPath().lastSegment(), true);
    resourceMap.put(memberNode, file);
    return memberNode;
  }

  /**
   * Retrieve the models created by calling {@link #createProjectModels()}.
   *
   * @return the project models of the current workspace, or <code>null</code> if {@link
   *     #createProjectModels()} wasn't called yet.
   */
  public List<ProjectTree> getProjectModels() {
    return new ArrayList<ProjectTree>(projectModels);
  }

  /**
   * Extracts a list of selected resources from the given {@link ProjectTree} s.
   *
   * @param projectTreeModels the list of {@link ProjectTree}s to extract the resources list from
   * @return A list of all selected resources from the given models. Will be empty if {@link
   *     #createProjectModels()} was not called before.
   */
  public List<IResource> getAllResources(ProjectTree[] projectTreeModels) {
    List<IResource> resourcesToShare = new ArrayList<IResource>();

    for (ProjectTree pTree : projectTreeModels) {
      resourcesToShare.addAll(getResources(pTree));
    }

    return resourcesToShare;
  }

  private List<IResource> getResources(ProjectTree projectTree) {
    List<IResource> resources = new ArrayList<IResource>();

    for (Node node : flatten(projectTree)) {
      if (!node.isSelectedForSharing()) continue;

      IResource resource = resourceMap.get(node);

      if (resource != null && resource.exists()) resources.add(resource);
    }

    return resources;
  }

  private List<Node> flatten(ProjectTree projectTree) {
    List<Node> collector = new ArrayList<Node>();
    addMembersRecursively(projectTree.getRoot(), collector);
    return collector;
  }

  private void addMembersRecursively(Node currentNode, List<Node> collector) {
    collector.add(currentNode);

    for (Node projectTreeNode : currentNode.getMembers()) {
      addMembersRecursively(projectTreeNode, collector);
    }
  }
}
