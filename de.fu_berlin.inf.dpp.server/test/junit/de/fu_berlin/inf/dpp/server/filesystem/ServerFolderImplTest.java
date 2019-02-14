package de.fu_berlin.inf.dpp.server.filesystem;

import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertIsFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFile;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.path;
import static org.easymock.EasyMock.expect;

import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;

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
