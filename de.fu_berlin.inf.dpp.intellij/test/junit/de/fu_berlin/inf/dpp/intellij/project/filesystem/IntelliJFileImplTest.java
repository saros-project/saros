package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@PrepareForTest(
    { LocalFileSystem.class, ApplicationManager.class, Application.class })
@RunWith(PowerMockRunner.class)
public class IntelliJFileImplTest extends AbstractResourceTest {

    @Test
    public void testCreate() throws IOException {
        mockApplicationManager();
        mockFileSystem();
        IFile file = new IntelliJFileImpl(getMockProject(),
            new File(TEST_FILE_NAME));

        file.create(new ByteArrayInputStream(new byte[] { }), false);

        assertTrue(file.exists());
    }

    @Test
    public void testSetAndGetContents() throws Exception {
        mockApplicationManager();
        mockFileSystem();
        IFile file = createTestFile();
        byte[] content = { 1, 2, 3, 4 };

        file.setContents(new ByteArrayInputStream(content), true, true);

        byte[] result = new byte[content.length];
        file.getContents().read(result);

        assertArrayEquals(content, result);
    }

    @Test
    public void testGetSize() throws Exception {
        mockFileSystem();
        IFile file = createFileWithContent();

        assertEquals(file.getSize(), 4);
    }

    @Test
    public void testExists() throws Exception {
        IFile file = createTestFile();

        assertTrue(file.exists());
    }

    @Test
    public void testMove() throws IOException {
        mockApplicationManager();
        mockFileSystem();
        IFile file = createTestFile();

        String oldPath = file.getLocation().toPortableString();
        IPath destination = IntelliJPathImpl
            .fromString(folder.getRoot().getPath()).append(TEST_PROJECT_NAME)
            .append(NEW_FILE_NAME);

        file.move(destination, false);

        assertFalse(new File(oldPath).exists());
        assertTrue(new File(destination.toPortableString()).exists());
        assertEquals(file.getLocation(), destination);
    }

    @Test
    public void testDelete() throws IOException {
        mockApplicationManager();
        mockFileSystem();
        IFile file = createTestFile();

        file.delete(0);

        assertFalse(file.exists());
    }

    private IntelliJFileImpl createTestFile() throws IOException {
        folder.newFile(TEST_PROJECT_NAME + "/" + TEST_FILE_NAME);
        return new IntelliJFileImpl(getMockProject(), new File(TEST_FILE_NAME));
    }

    /* This method do not use IntelliJFileImpl#setContents since the VFS is
     * not be available during the tests. */
    private IFile createFileWithContent() throws Exception {
        IFile file = createTestFile();

        FileOutputStream fos = new FileOutputStream(
            file.getLocation().toFile());
        fos.write(new byte[] { 1, 1, 1, 1 });
        fos.close();

        return file;
    }
}