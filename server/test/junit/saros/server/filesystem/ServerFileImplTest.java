package saros.server.filesystem;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static saros.server.filesystem.FileSystemTestUtils.assertFileHasContent;
import static saros.server.filesystem.FileSystemTestUtils.assertResourceExists;
import static saros.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static saros.server.filesystem.FileSystemTestUtils.createFile;
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

public class ServerFileImplTest extends EasyMockSupport {

  private IFile file;
  private ServerWorkspaceImpl workspace;
  private IProject project;

  @Before
  public void setUp() throws Exception {
    workspace = createMock(ServerWorkspaceImpl.class);
    project = createMock(IProject.class);

    expect(workspace.getLocation()).andStubReturn(createWorkspaceFolder());

    expect(workspace.getProject("project")).andStubReturn(project);

    replayAll();
    file = new ServerFileImpl(workspace, path("project/file"));
  }

  @After
  public void cleanUp() {
    FileUtils.deleteQuietly(workspace.getLocation().toFile());
  }

  @Test
  public void getType() {
    assertEquals(IResource.Type.FILE, file.getType());
  }

  @Test
  public void getCharsetIfExplicitlySet() throws Exception {
    file.setCharset("ISO-8859-1");
    assertEquals("ISO-8859-1", file.getCharset());
  }

  @Test
  public void delete() throws Exception {
    createFile(workspace, "project/file");
    file.delete();
    assertResourceNotExists(workspace, "project/file");
  }

  @Test(expected = None.class)
  public void deleteNonExistent() throws Exception {
    assertResourceNotExists(workspace, "project/file");
    file.delete();
  }

  public void create() throws Exception {
    assertResourceNotExists(workspace, "project/file");

    StringReader reader = new StringReader("content");
    file.create(new ReaderInputStream(reader));

    assertResourceExists(workspace, "project/file");
    assertFileHasContent(workspace, "project/file", "content");
  }

  @Test(expected = IOException.class)
  public void createExistent() throws Exception {
    createFile(workspace, "project/file");

    StringReader reader = new StringReader("content");
    file.create(new ReaderInputStream(reader));
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
    file.setContents(new ReaderInputStream(reader));

    assertFileHasContent(workspace, "project/file", "new content");
  }

  @Test(expected = IOException.class)
  public void setContentsOfNonExistent() throws Exception {
    assertResourceNotExists(workspace, "project/file");

    StringReader reader = new StringReader("new content");
    file.setContents(new ReaderInputStream(reader));

    assertFileHasContent(workspace, "project/file", "new content");
  }

  @Test
  public void setContentsToNull() throws Exception {
    createFile(workspace, "project/file", "old content");
    file.setContents(null);
    assertFileHasContent(workspace, "project/file", "");
  }

  @Test
  public void getSize() throws Exception {
    createFile(workspace, "project/file", "content");
    assertEquals(7, file.getSize());
  }
}
