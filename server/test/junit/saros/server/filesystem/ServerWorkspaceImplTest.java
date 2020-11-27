package saros.server.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static saros.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static saros.server.filesystem.FileSystemTestUtils.createFolder;
import static saros.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspaceRunnable;
import saros.monitoring.IProgressMonitor;

public class ServerWorkspaceImplTest extends EasyMockSupport {

  private Path workspaceLocation;
  private ServerWorkspaceImpl workspace;

  @Before
  public void setUp() throws Exception {
    workspaceLocation = createWorkspaceFolder();
    workspace = new ServerWorkspaceImpl(workspaceLocation);
  }

  @After
  public void cleanUp() {
    FileUtils.deleteQuietly(workspaceLocation.toFile());
  }

  @Test
  public void getLocation() {
    assertEquals(workspaceLocation, workspace.getLocation());
  }

  @Test
  public void getProject() {
    IReferencePoint project = workspace.getProject("project");
    assertEquals("project", project.getName());
    assertSame(workspace, ((ServerProjectImpl) project).getWorkspace());
  }

  @Test
  public void run() throws Exception {
    createFolder(workspace, "project");

    workspace.run(
        new IWorkspaceRunnable() {
          @Override
          public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException {

            assertNotNull(monitor);
            workspace.getProject("project").delete();
          }
        });

    assertResourceNotExists(workspace, "project");
  }
}
