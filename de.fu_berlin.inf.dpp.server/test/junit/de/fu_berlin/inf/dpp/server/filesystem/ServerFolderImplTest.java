package de.fu_berlin.inf.dpp.server.filesystem;

import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertIsFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFile;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.path;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import de.fu_berlin.inf.dpp.filesystem.IFile;
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
  private IFolder project;
  private IWorkspace workspace;

  @Before
  public void setUp() throws Exception {
    workspace = createMock(IWorkspace.class);
    expect(workspace.getLocation()).andStubReturn(createWorkspaceFolder());

    replayAll();

    folder = new ServerFolderImplV2(workspace.getLocation(), path(FOLDER_PATH));
    project =
        new ServerFolderImplV2(
            workspace.getLocation().append(FOLDER_PARENT_PATH),
            ServerPathImpl.fromString(new String()));
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

  @Test
  public void defaultCharsetUTF8() throws Exception {
    assertEquals("UTF-8", project.getDefaultCharset());
  }

  @Test
  public void findMember() throws Exception {
    createFolder(workspace, "project");
    createFolder(workspace, "project/folder");
    createFile(workspace, "project/folder/file");

    IResource folder = project.findMember(path("folder"));
    IResource file = project.findMember(path("folder/file"));
    IResource nonExistent = project.findMember(path("non/existent"));

    assertEquals(IResource.FOLDER, folder.getType());
    assertEquals(IResource.FILE, file.getType());
    assertNull(nonExistent);
  }

  @Test
  public void findMemberAtEmptyPath() throws Exception {
    assertSame(project, project.findMember(path("")));
  }

  @Test
  public void getFileByPath() {
    IFile file = project.getFile(path("folder/file"));
    assertEquals(path("folder/file"), file.getFullPath());
  }

  @Test
  public void getFileByPathString() {
    IFile file = project.getFile("folder/file");
    assertEquals(path("folder/file"), file.getFullPath());
  }

  @Test
  public void getFolderByPath() {
    IFolder folder = project.getFolder(path("folder/subfolder"));
    assertEquals(path("folder/subfolder"), folder.getFullPath());
  }

  @Test
  public void getFolderByPathString() {
    IFolder folder = project.getFolder("folder/subfolder");
    assertEquals(path("folder/subfolder"), folder.getFullPath());
  }
}
