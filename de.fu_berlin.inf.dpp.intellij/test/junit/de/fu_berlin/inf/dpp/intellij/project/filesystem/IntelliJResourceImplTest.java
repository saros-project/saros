package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IntelliJResourceImplTest extends AbstractResourceTest {

    static class DummyResource extends IntelliJResourceImpl {

        protected DummyResource(IntelliJProjectImpl project, File file) {
            super(project, file);
        }

        @Override
        public void refreshLocal() throws IOException {

        }

        @Override
        public void delete(int updateFlags) throws IOException {

        }

        @Override
        public void move(IPath destination, boolean force) throws IOException {

        }

        @Override
        public Object getAdapter(Class<? extends IResource> clazz) {
            return null;
        }
    }

    private IResource createTestResourceWithFile() throws IOException {
        createTestProjectFolder();
        folder.newFile(RELATIVE_TEST_RESOURCE_PATH);
        return getTestResource();
    }

    @NotNull
    private DummyResource getTestResource() {
        return new DummyResource(getMockProject(), new File(TEST_FILE_NAME));
    }

    @Test
    public void testIfNotPresentExistIsFalse() throws Exception {
        createTestProjectFolder();
        IResource resource = getTestResource();

        assertTrue(!resource.exists());
    }

    @Test
    public void testExists() throws Exception {
        IResource resource = createTestResourceWithFile();

        assertTrue(resource.exists());
    }

    @Test
    public void testGetName() throws Exception {
        IResource resource = getTestResource();

        assertEquals(TEST_FILE_NAME, resource.getName());
    }

    @Test
    public void testGetFullPath() throws Exception {
        IResource resource = getTestResource();

        assertFullPathIsCorrect(resource);
    }

    @Test
    public void testGetProjectRelativePath() throws Exception {
        IResource resource = getTestResource();

        assertProjectRelativePathIsCorrect(resource);
    }

    @Test
    public void testGetLocation() throws Exception {
        IResource resource = getTestResource();

        assertEquals(IntelliJPathImpl.fromString(
                folder.getRoot().getPath() + "/" + RELATIVE_TEST_RESOURCE_PATH),
            resource.getLocation());
    }

    @Test
    public void createResourceWithAbsolutePath() {
        IResource resource = new DummyResource(getMockProject(),
            new File(folder.getRoot().getAbsolutePath(),
                RELATIVE_TEST_RESOURCE_PATH));
        assertFullPathIsCorrect(resource);
        assertProjectRelativePathIsCorrect(resource);
    }

    @Test
    public void equalsWithIdenticalResources() {
        IResource resource1 = getTestResource();
        IResource resource2 = getTestResource();

        assertTrue(resource1.equals(resource2));
    }

    @Test
    public void equalsWithDifferingResources() {
        IResource resource1 = getTestResource();
        IResource resource2 = new DummyResource(getMockProject(),
            new File("file"));

        assertFalse(resource1.equals(resource2));
    }

    private void assertFullPathIsCorrect(IResource resource) {
        assertEquals(IntelliJPathImpl.fromString(RELATIVE_TEST_RESOURCE_PATH),
            resource.getFullPath());
    }

    private void assertProjectRelativePathIsCorrect(IResource resource) {
        assertEquals(TEST_FILE_NAME,
            resource.getProjectRelativePath().toPortableString());
    }
}
