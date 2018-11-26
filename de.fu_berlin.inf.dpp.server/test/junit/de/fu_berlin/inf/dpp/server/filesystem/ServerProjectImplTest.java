package de.fu_berlin.inf.dpp.server.filesystem;

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
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerProjectImplTest extends EasyMockSupport {

  private IProject project;
  private IWorkspace workspace;

  @Before
  public void setUp() throws Exception {
    workspace = createMock(IWorkspace.class);
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
    assertEquals(IResource.PROJECT, project.getType());
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
    assertEquals(path("project/folder/file"), file.getFullPath());
  }

  @Test
  public void getFileByPathString() {
    IFile file = project.getFile("folder/file");
    assertEquals(path("project/folder/file"), file.getFullPath());
  }

  @Test
  public void getFolderByPath() {
    IFolder folder = project.getFolder(path("folder/subfolder"));
    assertEquals(path("project/folder/subfolder"), folder.getFullPath());
  }

  @Test
  public void getFolderByPathString() {
    IFolder folder = project.getFolder("folder/subfolder");
    assertEquals(path("project/folder/subfolder"), folder.getFullPath());
  }
}
