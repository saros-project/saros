package saros.server.filesystem;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.filesystem.IResource.Type.FILE;
import static saros.server.filesystem.FileSystemTestUtils.createFile;
import static saros.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static saros.server.filesystem.FileSystemTestUtils.path;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.filesystem.IFolder;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;

public class ServerResourceImplTest extends EasyMockSupport {

  private static class ExampleResource extends ServerResourceImpl {

    public ExampleResource(Path path, ServerWorkspaceImpl workspace) {
      super(workspace, path);
    }

    @Override
    public Type getType() {
      return FILE;
    }

    @Override
    public void delete() throws IOException {
      // Do nothing
    }
  }

  private IResource resource;
  private ServerWorkspaceImpl workspace;
  private IReferencePoint project;
  private IFolder parent;

  @Before
  public void setUp() throws Exception {
    workspace = createMock(ServerWorkspaceImpl.class);
    project = createMock(IReferencePoint.class);
    parent = createMock(IFolder.class);

    expect(workspace.getLocation()).andStubReturn(createWorkspaceFolder());

    expect(workspace.getProject("project")).andStubReturn(project);
    expect(project.getFolder(path("folder"))).andStubReturn(parent);

    replayAll();

    resource = new ExampleResource(path("project/folder/file"), workspace);
  }

  @After
  public void cleanUp() {
    FileUtils.deleteQuietly(workspace.getLocation().toFile());
  }

  @Test
  public void getFullPath() {
    assertEquals(path("project/folder/file"), ((ServerResourceImpl) resource).getFullPath());
  }

  @Test
  public void getProjectRelativePath() {
    assertEquals(path("folder/file"), resource.getReferencePointRelativePath());
  }

  @Test
  public void getLocation() {
    assertEquals(
        workspace.getLocation().resolve("project/folder/file"),
        ((ServerResourceImpl) resource).getLocation());
  }

  @Test
  public void getName() {
    assertEquals("file", resource.getName());
  }

  @Test
  public void getProject() {
    assertEquals(project, resource.getReferencePoint());
  }

  @Test
  public void getParentIfParentIsFolder() {
    assertEquals(parent, resource.getParent());
  }

  @Test
  public void getParentIfParentIsProject() {
    resource = new ExampleResource(path("project/file"), workspace);
    assertEquals(project, resource.getParent());
  }

  @Test
  public void exists() throws Exception {
    assertFalse(resource.exists());
    createFileForResource();
    assertTrue(resource.exists());
  }

  @Test
  public void isNeverIgnored() throws Exception {
    assertFalse(resource.isIgnored());
  }

  private void createFileForResource() throws IOException {
    createFile(workspace, "project/folder/file");
  }
}
