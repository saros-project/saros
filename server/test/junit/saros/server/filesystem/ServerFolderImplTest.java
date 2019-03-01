package saros.server.filesystem;

import static org.easymock.EasyMock.expect;
import static saros.server.filesystem.FileSystemTestUtils.assertIsFolder;
import static saros.server.filesystem.FileSystemTestUtils.assertResourceExists;
import static saros.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static saros.server.filesystem.FileSystemTestUtils.createFile;
import static saros.server.filesystem.FileSystemTestUtils.createFolder;
import static saros.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static saros.server.filesystem.FileSystemTestUtils.path;

import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;
import saros.filesystem.IFolder;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;

public class ServerFolderImplTest extends EasyMockSupport {

  private static final String FOLDER_PATH = "project/folder";
  private static final String FOLDER_PARENT_PATH = "project";

  private IFolder folder;
  private IWorkspace workspace;

  @Before
  public void setUp() throws Exception {
    workspace = createMock(IWorkspace.class);
    expect(workspace.getLocation()).andStubReturn(createWorkspaceFolder());

    replayAll();

    folder = new ServerFolderImpl(workspace, path(FOLDER_PATH));
  }

  @After
  public void cleanUp() {
    FileUtils.deleteQuietly(workspace.getLocation().toFile());
  }

  @Test
  public void create() throws Exception {
    createFolder(workspace, FOLDER_PARENT_PATH);
    folder.create(IResource.NONE, true);
    assertResourceExists(workspace, FOLDER_PATH);
    assertIsFolder(workspace, FOLDER_PATH);
  }

  @Test(expected = None.class)
  public void createIfFolderExistsAtPath() throws Exception {
    createFolder(workspace, FOLDER_PATH);
    folder.create(IResource.NONE, true);
  }

  @Test(expected = IOException.class)
  public void createIfFileExistsAtPath() throws Exception {
    createFile(workspace, FOLDER_PATH);
    folder.create(IResource.NONE, true);
  }

  @Test(expected = IOException.class)
  public void createAtNonExistentParent() throws Exception {
    assertResourceNotExists(workspace, FOLDER_PARENT_PATH);
    folder.create(IResource.NONE, true);
  }
}
