package saros.server.filesystem;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static saros.server.filesystem.FileSystemTestUtils.createFile;
import static saros.server.filesystem.FileSystemTestUtils.createFolder;
import static saros.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static saros.server.filesystem.FileSystemTestUtils.path;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;
import saros.filesystem.IContainer;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;

public class ServerContainerImplTest extends EasyMockSupport {

  private static class ExampleContainer extends ServerContainerImpl {

    public ExampleContainer(IPath path, IWorkspace workspace) {
      super(workspace, path);
    }

    @Override
    public Type getType() {
      return IResource.Type.FOLDER;
    }
  }

  private static final String CONTAINER_PATH = "project/folder";

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
    container.delete();
    assertResourceNotExists(workspace, CONTAINER_PATH);
  }

  @Test
  public void deleteNonEmpty() throws Exception {
    createFolder(workspace, CONTAINER_PATH);
    createFile(workspace, CONTAINER_PATH + "/file");
    container.delete();
    assertResourceNotExists(workspace, CONTAINER_PATH);
  }

  @Test(expected = None.class)
  public void deleteNonExistent() throws Exception {
    assertResourceNotExists(workspace, CONTAINER_PATH);
    container.delete();
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

    ServerResourceImpl member0 = (ServerResourceImpl) members[0];
    assertEquals(path(CONTAINER_PATH + "/file"), member0.getFullPath());
    assertEquals(IResource.Type.FILE, member0.getType());

    ServerResourceImpl member1 = (ServerResourceImpl) members[1];
    assertEquals(path(CONTAINER_PATH + "/subfolder"), member1.getFullPath());
    assertEquals(IResource.Type.FOLDER, member1.getType());
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
