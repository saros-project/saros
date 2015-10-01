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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IResourceAttributes;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;

public class ServerResourceImplTest extends EasyMockSupport {

    private static class ExampleResource extends ServerResourceImpl {

        public ExampleResource(IPath path, IWorkspace workspace) {
            super(workspace, path);
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
    private IProject project;
    private IFolder parent;

    @Before
    public void setUp() throws Exception {
        workspace = createMock(IWorkspace.class);
        project = createMock(IProject.class);
        parent = createMock(IFolder.class);

        expect(workspace.getLocation()).andStubReturn(createWorkspaceFolder());

        expect(workspace.getProject("project")).andStubReturn(project);
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
        assertEquals(workspace.getLocation().append("project/folder/file"),
            ((ServerResourceImpl) resource).getLocation());
    }

    @Test
    public void getName() {
        assertEquals("file", resource.getName());
    }

    @Test
    public void getProject() {
        assertEquals(project, resource.getProject());
    }

    @Test
    public void getParentIfParentIsFolder() {
        assertEquals(parent, resource.getParent());
    }

    @Test
    public void exists() throws Exception {
        assertFalse(resource.exists());
        createFileForResource();
        assertTrue(resource.exists());
    }

    @Test
    public void isAccessible() throws Exception {
        assertFalse(resource.isAccessible());
        createFileForResource();
        assertTrue(resource.isAccessible());
    }

    @Test
    public void isNeverDerived() throws Exception {
        assertFalse(resource.isDerived());
        assertFalse(resource.isDerived(true));
    }

    @Test
    public void getResourceAttributes() throws Exception {
        createFileForResource();

        resource.getLocation().toFile().setWritable(false);
        assertTrue(resource.getResourceAttributes().isReadOnly());
        resource.getLocation().toFile().setWritable(true);
        assertFalse(resource.getResourceAttributes().isReadOnly());
    }

    @Test
    public void getResourceAttributesOfNonExistentFile() throws Exception {
        assertNull(resource.getResourceAttributes());
    }

    @Test
    public void setResourceAttributes() throws Exception {
        createFileForResource();
        IPath resourceLocation = resource.getLocation();

        IResourceAttributes attributes = new ServerResourceAttributesImpl();

        attributes.setReadOnly(true);
        resource.setResourceAttributes(attributes);
        assertFalse(resourceLocation.toFile().canWrite());

        attributes.setReadOnly(false);
        resource.setResourceAttributes(attributes);
        assertTrue(resourceLocation.toFile().canWrite());
    }

    @Test(expected = FileNotFoundException.class)
    public void setResourceAttributesOfNonExistentFile() throws Exception {
        IResourceAttributes attributes = new ServerResourceAttributesImpl();
        attributes.setReadOnly(true);
        resource.setResourceAttributes(attributes);
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
