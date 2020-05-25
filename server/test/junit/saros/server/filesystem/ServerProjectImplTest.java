package saros.server.filesystem;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static saros.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static saros.server.filesystem.FileSystemTestUtils.path;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;

public class ServerProjectImplTest extends EasyMockSupport {

  private IReferencePoint project;
  private ServerWorkspaceImpl workspace;

  @Before
  public void setUp() throws Exception {
    workspace = createMock(ServerWorkspaceImpl.class);
    expect(workspace.getLocation()).andStubReturn(createWorkspaceFolder());

    replayAll();

    project = new ServerProjectImpl(workspace, "project");
  }

  @After
  public void cleanUp() {
    FileUtils.deleteQuietly(workspace.getLocation().toFile());
  }

  @Test
  public void getType() {
    assertEquals(IResource.Type.REFERENCE_POINT, project.getType());
  }

  @Test
  public void getFileByPath() {
    IFile file = project.getFile(path("folder/file"));
    assertEquals(path("project/folder/file"), ((ServerFileImpl) file).getFullPath());
  }

  @Test
  public void getFileByPathString() {
    IFile file = project.getFile("folder/file");
    assertEquals(path("project/folder/file"), ((ServerFileImpl) file).getFullPath());
  }

  @Test
  public void getFolderByPath() {
    IFolder folder = project.getFolder(path("folder/subfolder"));
    assertEquals(path("project/folder/subfolder"), ((ServerFolderImpl) folder).getFullPath());
  }

  @Test
  public void getFolderByPathString() {
    IFolder folder = project.getFolder("folder/subfolder");
    assertEquals(path("project/folder/subfolder"), ((ServerFolderImpl) folder).getFullPath());
  }
}
