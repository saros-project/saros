package de.fu_berlin.inf.dpp.server.filesystem;

import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.absolutePath;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertFileHasContent;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertIsFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFile;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.path;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
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

public class ServerFolderImplTest extends EasyMockSupport {

  private static final String WORKSPACE_RELATIVE_PATH = "project/folder";
  private static final String FOLDER_PARENT_PATH = "project";

  private static final String FOLDER_NAME = "folder";
  private static final String OTHER_PATH = "project2/other";

  private IFolder referencedFolder;
  private IFolder folder;
  private IWorkspace workspace;

  @Before
  public void setUp() throws Exception {
    IPath workspaceLocation = createWorkspaceFolder();
    workspace = createMock(IWorkspace.class);
    expect(workspace.getLocation()).andStubReturn(workspaceLocation);

    replayAll();

    referencedFolder =
        new ServerFolderImpl(
            workspace.getLocation().append(FOLDER_PARENT_PATH),
            ServerPathImpl.fromString(new String()));
    folder =
        new ServerFolderImpl(
            workspace.getLocation().append(FOLDER_PARENT_PATH),
            ServerPathImpl.fromString(FOLDER_NAME));
  }

  @After
  public void cleanUp() {
    FileUtils.deleteQuietly(workspace.getLocation().toFile());
  }

  @Test
  public void create() throws Exception {
    createFolder(workspace, WORKSPACE_RELATIVE_PATH);
    folder.create(IResource.NONE, true);
    assertResourceExists(workspace, WORKSPACE_RELATIVE_PATH);
    assertIsFolder(workspace, WORKSPACE_RELATIVE_PATH);
  }

  @Test(expected = None.class)
  public void createIfFolderExistsAtPath() throws Exception {
    createFolder(workspace, WORKSPACE_RELATIVE_PATH);
    folder.create(IResource.NONE, true);
  }

  @Test(expected = IOException.class)
  public void createIfFileExistsAtPath() throws Exception {
    createFile(workspace, WORKSPACE_RELATIVE_PATH);
    folder.create(IResource.NONE, true);
  }

  @Test(expected = IOException.class)
  public void createAtNonExistentParent() throws Exception {
    assertResourceNotExists(workspace, FOLDER_PARENT_PATH);
    folder.create(IResource.NONE, true);
  }

  @Test
  public void defaultCharsetUTF8() throws Exception {
    assertEquals("UTF-8", referencedFolder.getDefaultCharset());
  }

  @Test
  public void findMember() throws Exception {
    createFolder(workspace, "project");
    createFolder(workspace, "project/folder");
    createFile(workspace, "project/folder/file");

    IResource folder = referencedFolder.findMember(path("folder"));
    IResource file = referencedFolder.findMember(path("folder/file"));
    IResource nonExistent = referencedFolder.findMember(path("non/existent"));

    assertEquals(IResource.FOLDER, folder.getType());
    assertEquals(IResource.FILE, file.getType());
    assertNull(nonExistent);
  }

  @Test
  public void findMemberAtEmptyPath() throws Exception {
    assertSame(referencedFolder, referencedFolder.findMember(path("")));
  }

  @Test
  public void getFileByPath() {
    IFile file = referencedFolder.getFile(path("folder/file"));
    assertEquals(path("folder/file"), file.getFullPath());
  }

  @Test
  public void getFileByPathString() {
    IFile file = referencedFolder.getFile("folder/file");
    assertEquals(path("folder/file"), file.getFullPath());
  }

  @Test
  public void getFolderByPath() {
    IFolder folder = referencedFolder.getFolder(path("folder/subfolder"));
    assertEquals(path("folder/subfolder"), folder.getFullPath());
  }

  @Test
  public void getFolderByPathString() {
    IFolder folder = referencedFolder.getFolder("folder/subfolder");
    assertEquals(path("folder/subfolder"), folder.getFullPath());
  }

  @Test
  public void delete() throws Exception {
    createFolder(workspace, WORKSPACE_RELATIVE_PATH);
    folder.delete(IResource.NONE);
    assertResourceNotExists(workspace, WORKSPACE_RELATIVE_PATH);
  }

  @Test
  public void deleteNonEmpty() throws Exception {
    createFolder(workspace, WORKSPACE_RELATIVE_PATH);
    createFile(workspace, WORKSPACE_RELATIVE_PATH + "/file");
    folder.delete(IResource.NONE);
    assertResourceNotExists(workspace, WORKSPACE_RELATIVE_PATH);
  }

  @Test(expected = None.class)
  public void deleteNonExistent() throws Exception {
    assertResourceNotExists(workspace, FOLDER_NAME);
    folder.delete(IResource.NONE);
  }

  @Test(expected = IOException.class)
  public void moveNonExistent() throws Exception {
    assertResourceNotExists(workspace, WORKSPACE_RELATIVE_PATH);
    createFolder(workspace, OTHER_PATH);
    folder.move(absolutePath(OTHER_PATH + "/folder"), true);
  }

  @Test
  public void moveToAbsolutePath() throws Exception {

    createFolder(workspace, WORKSPACE_RELATIVE_PATH);
    createFile(workspace, WORKSPACE_RELATIVE_PATH + "/file", "content");
    createFolder(workspace, OTHER_PATH);

    folder.move(absolutePath(OTHER_PATH + "/folder"), true);

    assertResourceNotExists(workspace, WORKSPACE_RELATIVE_PATH);
    assertResourceExists(workspace, OTHER_PATH + "/folder");
    assertResourceExists(workspace, OTHER_PATH + "/folder/file");
    assertFileHasContent(workspace, OTHER_PATH + "/folder/file", "content");
  }

  @Test
  public void moveToRelativePath() throws Exception {
    createFolder(workspace, WORKSPACE_RELATIVE_PATH);
    createFile(workspace, WORKSPACE_RELATIVE_PATH + "/file", "content");

    String siblingFolderPath = FOLDER_PARENT_PATH + "/destination";
    createFolder(workspace, siblingFolderPath);

    folder.move(path("destination/folder"), true);

    assertResourceNotExists(workspace, WORKSPACE_RELATIVE_PATH);
    assertResourceExists(workspace, siblingFolderPath + "/folder");
    assertResourceExists(workspace, siblingFolderPath + "/folder/file");
    assertFileHasContent(workspace, siblingFolderPath + "/folder/file", "content");
  }

  @Test(expected = IOException.class)
  public void membersOfNonExistent() throws Exception {
    assertResourceNotExists(workspace, WORKSPACE_RELATIVE_PATH);
    folder.members();
  }

  @Test
  public void members() throws Exception {
    createFolder(workspace, WORKSPACE_RELATIVE_PATH);
    createFile(workspace, WORKSPACE_RELATIVE_PATH + "/file");
    createFolder(workspace, WORKSPACE_RELATIVE_PATH + "/subfolder");

    IResource[] members = folder.members();
    assertEquals(2, members.length);

    Arrays.sort(
        members,
        new Comparator<IResource>() {
          @Override
          public int compare(IResource r1, IResource r2) {
            return r1.getName().compareTo(r2.getName());
          }
        });

    assertEquals(path(FOLDER_NAME + "/file"), members[0].getFullPath());
    assertEquals(IResource.FILE, members[0].getType());
    assertEquals(path(FOLDER_NAME + "/subfolder"), members[1].getFullPath());
    assertEquals(IResource.FOLDER, members[1].getType());
  }

  @Test
  public void existsChild() throws Exception {
    createFolder(workspace, WORKSPACE_RELATIVE_PATH);
    createFile(workspace, WORKSPACE_RELATIVE_PATH + "/file1");
    createFolder(workspace, WORKSPACE_RELATIVE_PATH + "/subfolder");
    createFile(workspace, WORKSPACE_RELATIVE_PATH + "/subfolder/file2");

    assertTrue(folder.exists(path("file1")));
    assertTrue(folder.exists(path("subfolder")));
    assertTrue(folder.exists(path("subfolder/file2")));
    assertFalse(folder.exists(path("something/else")));
  }

  @Test
  public void defaultCharset() throws IOException {
    assertEquals("UTF-8", folder.getDefaultCharset());
  }
}
