package de.fu_berlin.inf.dpp.server.filesystem;

import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.assertResourceNotExists;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerWorkspaceImplTest extends EasyMockSupport {

  private IPath workspaceLocation;
  private IWorkspace workspace;

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
    IProject project = workspace.getProject("project");
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
            workspace.getProject("project").delete(IResource.NONE);
          }
        });

    assertResourceNotExists(workspace, "project");
  }
}
