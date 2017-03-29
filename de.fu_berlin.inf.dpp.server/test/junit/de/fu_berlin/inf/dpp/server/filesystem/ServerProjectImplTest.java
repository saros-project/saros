package de.fu_berlin.inf.dpp.server.filesystem;

import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.createWorkspaceFolder;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.path;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;

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
