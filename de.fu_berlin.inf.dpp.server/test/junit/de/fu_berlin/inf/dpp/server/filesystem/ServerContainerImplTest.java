package de.fu_berlin.inf.dpp.server.filesystem;

import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.absolutePath;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertFileHasContent;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFile;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.path;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;

public class ServerContainerImplTest extends EasyMockSupport {

  private static class ExampleContainer extends ServerContainerImpl {

    public ExampleContainer(IPath path, IWorkspace workspace) {
      super(workspace, path);
    }

    @Override
    public int getType() {
      return IResource.FOLDER;
    }
  }

  private static final String CONTAINER_PATH = "project/folder";
  private static final String CONTAINER_PARENT_PATH = "project";
  private static final String OTHER_PATH = "project2/other";

  private IContainer container;
  private IWorkspace workspace;
  private IProject project;

  @Before
  public void setUp() throws Exception {
    workspace = createMock(IWorkspace.class);
    project = createMock(IProject.class);

    expect(workspace.getLocation()).andStubReturn(createWorkspaceFolder());

    expect(workspace.getProject("project")).andStubReturn(project);
    expect(project.getDefaultCharset()).andStubReturn("UTF-8");

    replayAll();

    container = new ExampleContainer(path(CONTAINER_PATH), workspace);
  }

  @After
  public void cleanUp() {
    FileUtils.deleteQuietly(workspace.getLocation().toFile());
  }

  @Test
  public void delete() throws Exception {
    createFolder(workspace, CONTAINER_PATH);
    container.delete(IResource.NONE);
    assertResourceNotExists(workspace, CONTAINER_PATH);
  }

  @Test
  public void deleteNonEmpty() throws Exception {
    createFolder(workspace, CONTAINER_PATH);
    createFile(workspace, CONTAINER_PATH + "/file");
    container.delete(IResource.NONE);
    assertResourceNotExists(workspace, CONTAINER_PATH);
  }

  @Test(expected = None.class)
  public void deleteNonExistent() throws Exception {
    assertResourceNotExists(workspace, CONTAINER_PATH);
    container.delete(IResource.NONE);
  }

  @Test
  public void moveToAbsolutePath() throws Exception {
    createFolder(workspace, CONTAINER_PATH);
    createFile(workspace, CONTAINER_PATH + "/file", "content");
    createFolder(workspace, OTHER_PATH);

    container.move(absolutePath(OTHER_PATH + "/folder"), true);

    assertResourceNotExists(workspace, CONTAINER_PATH);
    assertResourceExists(workspace, OTHER_PATH + "/folder");
    assertResourceExists(workspace, OTHER_PATH + "/folder/file");
    assertFileHasContent(workspace, OTHER_PATH + "/folder/file", "content");
  }

  @Test
  public void moveToRelativePath() throws Exception {
    createFolder(workspace, CONTAINER_PATH);
    createFile(workspace, CONTAINER_PATH + "/file", "content");

    String siblingFolderPath = CONTAINER_PARENT_PATH + "/destination";
    createFolder(workspace, siblingFolderPath);

    container.move(path("destination/folder"), true);

    assertResourceNotExists(workspace, CONTAINER_PATH);
    assertResourceExists(workspace, siblingFolderPath + "/folder");
    assertResourceExists(workspace, siblingFolderPath + "/folder/file");
    assertFileHasContent(workspace, siblingFolderPath + "/folder/file", "content");
  }

  @Test(expected = IOException.class)
  public void moveNonExistent() throws Exception {
    assertResourceNotExists(workspace, CONTAINER_PATH);
    createFolder(workspace, OTHER_PATH);
    container.move(absolutePath(OTHER_PATH + "/folder"), true);
  }

  @Test
  public void members() throws Exception {
    createFolder(workspace, CONTAINER_PATH);
    createFile(workspace, CONTAINER_PATH + "/file");
    createFolder(workspace, CONTAINER_PATH + "/subfolder");

    IResource[] members = container.members();
    assertEquals(2, members.length);

    Arrays.sort(
        members,
        new Comparator<IResource>() {
          @Override
          public int compare(IResource r1, IResource r2) {
            return r1.getName().compareTo(r2.getName());
          }
        });

    assertEquals(path(CONTAINER_PATH + "/file"), members[0].getFullPath());
    assertEquals(IResource.FILE, members[0].getType());
    assertEquals(path(CONTAINER_PATH + "/subfolder"), members[1].getFullPath());
    assertEquals(IResource.FOLDER, members[1].getType());
  }

  @Test(expected = IOException.class)
  public void membersOfNonExistent() throws Exception {
    assertResourceNotExists(workspace, CONTAINER_PATH);
    container.members();
  }

  @Test
  public void existsChild() throws Exception {
    createFolder(workspace, CONTAINER_PATH);
    createFile(workspace, CONTAINER_PATH + "/file1");
    createFolder(workspace, CONTAINER_PATH + "/subfolder");
    createFile(workspace, CONTAINER_PATH + "/subfolder/file2");

    assertTrue(container.exists(path("file1")));
    assertTrue(container.exists(path("subfolder")));
    assertTrue(container.exists(path("subfolder/file2")));
    assertFalse(container.exists(path("something/else")));
  }

  @Test
  public void defaultCharset() throws IOException {
    assertEquals("UTF-8", container.getDefaultCharset());
  }
}
