package saros.server.filesystem;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static saros.server.filesystem.FileSystemTestUtils.absolutePath;
import static saros.server.filesystem.FileSystemTestUtils.assertFileHasContent;
import static saros.server.filesystem.FileSystemTestUtils.assertResourceExists;
import static saros.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static saros.server.filesystem.FileSystemTestUtils.createFile;
import static saros.server.filesystem.FileSystemTestUtils.createFolder;
import static saros.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static saros.server.filesystem.FileSystemTestUtils.path;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;

public class ServerFileImplTest extends EasyMockSupport {

  private IFile file;
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
    file = new ServerFileImpl(workspace, path("project/file"));
  }

  @After
  public void cleanUp() {
    FileUtils.deleteQuietly(workspace.getLocation().toFile());
  }

  @Test
  public void getType() {
    assertEquals(IResource.FILE, file.getType());
  }

  @Test
  public void getCharsetIfDefault() throws Exception {
    assertEquals(project.getDefaultCharset(), file.getCharset());
  }

  @Test
  public void getCharsetIfExplicitlySet() throws Exception {
    ((ServerFileImpl) file).setCharset("ISO-8859-1");
    assertEquals("ISO-8859-1", file.getCharset());
  }

  @Test
  public void delete() throws Exception {
    createFile(workspace, "project/file");
    file.delete(IResource.NONE);
    assertResourceNotExists(workspace, "project/file");
  }

  @Test(expected = None.class)
  public void deleteNonExistent() throws Exception {
    assertResourceNotExists(workspace, "project/file");
    file.delete(IResource.NONE);
  }

  @Test
  public void moveAbsolute() throws Exception {
    createFile(workspace, "project/file", "content");
    createFolder(workspace, "project/destination");

    file.move(absolutePath("project/destination/newname"), true);

    assertResourceNotExists(workspace, "project/file");
    assertResourceExists(workspace, "project/destination/newname");
    assertFileHasContent(workspace, "project/destination/newname", "content");
  }

  @Test
  public void moveRelative() throws Exception {
    createFile(workspace, "project/file", "content");
    createFolder(workspace, "project/destination");

    file.move(path("destination/newname"), true);

    assertResourceNotExists(workspace, "project/file");
    assertResourceExists(workspace, "project/destination/newname");
    assertFileHasContent(workspace, "project/destination/newname", "content");
  }

  @Test(expected = IOException.class)
  public void moveNonExistent() throws Exception {
    assertResourceNotExists(workspace, "project/file");
    createFolder(workspace, "project/destination");
    file.move(path("project/destination/newname"), true);
  }

  @Test(expected = IOException.class)
  public void moveToNonExistentFolder() throws Exception {
    createFile(workspace, "project/file");
    assertResourceNotExists(workspace, "project/desination");
    file.move(path("project/destination/newname"), true);
  }

  public void create() throws Exception {
    assertResourceNotExists(workspace, "project/file");

    StringReader reader = new StringReader("content");
    file.create(new ReaderInputStream(reader), true);

    assertResourceExists(workspace, "project/file");
    assertFileHasContent(workspace, "project/file", "content");
  }

  @Test(expected = IOException.class)
  public void createExistent() throws Exception {
    createFile(workspace, "project/file");

    StringReader reader = new StringReader("content");
    file.create(new ReaderInputStream(reader), true);
  }

  @Test
  public void getContents() throws Exception {
    createFile(workspace, "project/file", "content");

    InputStream input = file.getContents();
    byte[] actualContent = IOUtils.toByteArray(input);

    assertArrayEquals(actualContent, "content".getBytes("UTF-8"));
  }

  @Test(expected = IOException.class)
  public void getContentsOfNonExistent() throws Exception {
    file.getContents();
  }

  @Test
  public void setContents() throws Exception {
    createFile(workspace, "project/file", "old content");

    StringReader reader = new StringReader("new content");
    file.setContents(new ReaderInputStream(reader), true, true);

    assertFileHasContent(workspace, "project/file", "new content");
  }

  @Test(expected = IOException.class)
  public void setContentsOfNonExistent() throws Exception {
    assertResourceNotExists(workspace, "project/file");

    StringReader reader = new StringReader("new content");
    file.setContents(new ReaderInputStream(reader), true, true);

    assertFileHasContent(workspace, "project/file", "new content");
  }

  @Test
  public void setContentsToNull() throws Exception {
    createFile(workspace, "project/file", "old content");
    file.setContents(null, true, true);
    assertFileHasContent(workspace, "project/file", "");
  }

  @Test
  public void getSize() throws Exception {
    createFile(workspace, "project/file", "content");
    assertEquals(7, file.getSize());
  }
}
