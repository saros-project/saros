package de.fu_berlin.inf.dpp.server.filesystem;

import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFile;
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
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerResourceImplTest extends EasyMockSupport {

  private static class ExampleResource extends ServerResourceImplV2 {

    public ExampleResource(IPath path, IWorkspace workspace) {
      super(workspace.getLocation(), path);
    }

    @Override
    public int getType() {
      return IResource.FILE;
    }

    @Override
    public void delete(int updateFlags) throws IOException {
      // Do nothing
    }

    @Override
    public void move(IPath destination, boolean force) throws IOException {
      // Do nothing
    }
  }

  private IResource resource;
  private IWorkspace workspace;
  private IFolder project;
  private IFolder parent;

  @Before
  public void setUp() throws Exception {
    IPath sourceRoot = createWorkspaceFolder().append("project");
    workspace = createMock(IWorkspace.class);
    project = new ServerFolderImpl(sourceRoot, path(""));
    parent = new ServerFolderImpl(sourceRoot, path("folder"));

    expect(workspace.getLocation()).andStubReturn(sourceRoot);

    expect(workspace.getReferenceFolder("project")).andStubReturn(project);
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
    assertEquals(path("project/folder/file"), resource.getFullPath());
  }

  @Test
  public void getProjectRelativePath() {
    assertEquals(path("folder/file"), resource.getProjectRelativePath());
  }

  @Test
  public void getLocation() {
    assertEquals(
        workspace.getLocation().append("project/folder/file"),
        ((ServerResourceImplV2) resource).getLocation());
  }

  @Test
  public void getName() {
    assertEquals("file", resource.getName());
  }

  @Test
  public void getProject() {
    assertEquals(project, resource.getReferenceFolder());
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
  public void isNeverDerived() throws Exception {
    assertFalse(resource.isDerived());
    assertFalse(resource.isDerived(true));
  }

  @Test
  public void getAdapterDefault() {
    assertSame(resource, resource.getAdapter(ExampleResource.class));
    assertSame(resource, resource.getAdapter(IResource.class));
    assertNull(resource.getAdapter(IFile.class));
  }

  private void createFileForResource() throws IOException {
    createFile(workspace, "project/folder/file");
  }
}
