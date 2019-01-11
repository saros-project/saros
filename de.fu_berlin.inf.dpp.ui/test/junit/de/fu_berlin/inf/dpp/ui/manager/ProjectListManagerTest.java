package de.fu_berlin.inf.dpp.ui.manager;

import static java.text.MessageFormat.format;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IFolder_V2;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRoot;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree.Node;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree.Node.Type;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProjectListManagerTest {

  private static final String PROJECT = "myproject";
  private static final String FOLDER = "src";
  private static final String JAVA_FILE = "Main.java";
  private static final String TEXT_FILE = "README.txt";

  private IFolder_V2 project;
  private IFolder_V2 srcFolder;
  private IFile javaFile;
  private IFile textFile;

  private ProjectListManager mgrForEmpty;
  private ProjectListManager mgrForSingleProject;

  private boolean failedCreationForEmpty;
  private boolean failedCreationForSingleProject;

  /**
   * Creates two ProjectListManagers ({@link #mgrForEmpty} and {@link #mgrForSingleProject}) and
   * triggers the creation of the models representing their respective workspaces (one empty
   * workspace, another with a single project).
   *
   * <p>Since not all test cases are interested in this, calling {@link Assert#fail()} on model
   * creation problems during the setup is not an option; instead {@link
   * Assert#assertFalse(boolean)} on the fields {@link #failedCreationForEmpty} or {@link
   * #failedCreationForSingleProject} is used in those test cases that do care.
   */
  @Before
  public void setUp() {
    textFile = file(PROJECT + "/" + TEXT_FILE);
    javaFile = file(PROJECT + "/" + FOLDER + "/" + JAVA_FILE);
    srcFolder = folder(PROJECT + "/" + FOLDER, javaFile);
    project = project(PROJECT, srcFolder, textFile);

    mgrForEmpty = new ProjectListManager(root());
    failedCreationForEmpty = false;
    try {
      mgrForEmpty.createProjectModels();
    } catch (IOException e) {
      failedCreationForEmpty = true;
    }

    mgrForSingleProject = new ProjectListManager(root(project));
    failedCreationForSingleProject = false;
    try {
      mgrForSingleProject.createProjectModels();
    } catch (IOException e) {
      failedCreationForSingleProject = true;
    }
  }

  @Test
  public void noModelsBeforeCreation() {
    ProjectListManager mgr = new ProjectListManager(root());
    assertTrue(
        "there should be no project models before their creation",
        mgr.getProjectModels().isEmpty());
  }

  @Test
  public void noModelsForEmptyWorkspace() {
    assertFalse("Could not create project model for empty workspace", failedCreationForEmpty);

    List<ProjectTree> projectTrees = mgrForEmpty.getProjectModels();
    assertTrue("there should be no project models for an empty workspace", projectTrees.isEmpty());
  }

  @Test
  public void correctModelForSingleSimpleProject() {
    assertFalse(
        "Could not create project model for workspace with single project",
        failedCreationForSingleProject);

    List<ProjectTree> projectTrees = mgrForSingleProject.getProjectModels();

    assertEquals(
        "there should be exactly one ProjectTree for a one-project workspace",
        1,
        projectTrees.size());

    Node projectNode = projectTrees.get(0).getRoot();
    List<Node> members = projectNode.getMembers();

    assertEquals("the root node should be of type project", Type.PROJECT, projectNode.getType());
    assertEquals("the project node should have its proper label", PROJECT, projectNode.getLabel());
    assertEquals(
        "there should be two nodes below the project level of the model", 2, members.size());

    Node folderNode = null;
    Node textFileNode = null;

    // There is no guaranteed order in the list
    for (Node node : members) {
      if (node.getType() == Type.FOLDER) {
        folderNode = node;
      } else if (node.getType() == Type.FILE) {
        textFileNode = node;
      } else {
        fail("unexpected node type " + node.getType());
      }
    }

    assertNotNull("folder could not be found in the model", folderNode);

    if (folderNode == null || textFileNode == null) {
      // this is not for the control flow, but to avoid the Java compiler
      // warnings concerning "potential NullPointerExceptions" below
      return;
    }

    List<Node> folderMembers = folderNode.getMembers();

    assertEquals("the folder node should have its proper label", FOLDER, folderNode.getLabel());
    assertEquals("there should be one node in the folder", 1, folderMembers.size());

    Node javaFileNode = folderMembers.get(0);
    assertEquals("the file node should have its proper label", JAVA_FILE, javaFileNode.getLabel());
    assertEquals("the file node should have the correct type", Type.FILE, javaFileNode.getType());
    assertTrue("a file node should have no members", javaFileNode.getMembers().isEmpty());

    assertNotNull("top-level file could not be found in the model", textFileNode);
    assertEquals(
        "top-level file node should have its proper label", TEXT_FILE, textFileNode.getLabel());
    assertEquals(
        "top-level file node should have the correct type", Type.FILE, textFileNode.getType());
    assertTrue("a file node should have no members", textFileNode.getMembers().isEmpty());
  }

  @Test
  public void correctModelToResourceMapping() {
    assertFalse(
        "Could not create project model for workspace with single project",
        failedCreationForSingleProject);

    List<ProjectTree> projectTrees = mgrForSingleProject.getProjectModels();

    // Create a new set of ProjectTree instances to test the UI
    // model-to-resource mapping
    Gson gson = new Gson();
    String json = gson.toJson(projectTrees);
    ProjectTree[] convertedProjectTrees = gson.fromJson(json, ProjectTree[].class);

    List<IResource> resources = mgrForSingleProject.getAllResources(convertedProjectTrees);

    assertEquals("there should be four resources in total", 4, resources.size());

    boolean foundProject = false;
    boolean foundFolder = false;
    boolean foundJavaFile = false;
    boolean foundTextFile = false;

    for (IResource resource : resources) {
      int type = resource.getType();
      if (type == IResource.PROJECT) {
        foundProject = resource.equals(project);
      } else if (type == IResource.FOLDER) {
        foundFolder = resource.equals(srcFolder);
      } else if (type == IResource.FILE) {
        foundJavaFile = foundJavaFile || resource.equals(javaFile);
        foundTextFile = foundTextFile || resource.equals(textFile);
      } else {
        fail("unexpected resource type " + type);
      }
    }

    String msg = "could not retrieve original resource for ''{0}'' from UI model";
    assertTrue(format(msg, PROJECT), foundProject);
    assertTrue(format(msg, FOLDER), foundFolder);
    assertTrue(format(msg, JAVA_FILE), foundJavaFile);
    assertTrue(format(msg, TEXT_FILE), foundTextFile);
  }

  private IWorkspaceRoot root(IFolder_V2... projects) {
    IWorkspaceRoot root = createMock(IWorkspaceRoot.class);
    expect(root.getReferenceFolders()).andStubReturn(projects);
    replay(root);
    return root;
  }

  private IFolder_V2 project(String name, IResource... members) {
    IFolder_V2 project = createMock(IFolder_V2.class);
    try {
      expect(project.members()).andStubReturn(members);
    } catch (IOException e) {
      // cannot happen, it's a mock
    }
    expect(project.getFullPath()).andStubReturn(path(name));
    expect(project.getName()).andStubReturn(name);
    expect(project.getType()).andStubReturn(IResource.PROJECT);
    expect(project.exists()).andStubReturn(true);
    replay(project);
    return project;
  }

  private IFolder_V2 folder(String name, IResource... members) {
    IFolder_V2 folder = createMock(IFolder_V2.class);
    expect(folder.getType()).andStubReturn(IResource.FOLDER);
    try {
      expect(folder.members()).andStubReturn(members);
    } catch (IOException e) {
      // cannot happen, it's a mock
    }
    expect(folder.getFullPath()).andStubReturn(path(name));
    expect(folder.exists()).andStubReturn(true);
    replay(folder);
    return folder;
  }

  private IFile file(String name) {
    IFile file = createMock(IFile.class);
    expect(file.getType()).andStubReturn(IResource.FILE);
    expect(file.getFullPath()).andStubReturn(path(name));
    expect(file.exists()).andStubReturn(true);
    replay(file);
    return file;
  }

  private IPath path(String path) {
    String[] segments = path.split(Pattern.quote("/"));
    IPath pathMock = createMock(IPath.class);
    expect(pathMock.toPortableString()).andStubReturn(path);
    expect(pathMock.lastSegment()).andStubReturn(segments[segments.length - 1]);
    replay(pathMock);
    return pathMock;
  }
}
