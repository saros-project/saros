/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.mock.MockLocalFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IFile;

import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@PrepareForTest({ LocalFileSystem.class })
@RunWith(PowerMockRunner.class)
public class IntelliJFileImplTest {

    public static final String TESTFILE_NAME = "testfile.txt";
    public static final String NEW_FILE_NAME = "newCreateFile.txt";
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public static final String TEST_PROJECT_NAME = "project";

    //Since {@link IntelliJFileImpl#getContents} getContents is just creating a
    // FileInputStream, a seperate test for getContents is not necessary
    @Test
    public void testSetAndGetContents() throws Exception {
        IFile file = createTestFile();
        byte[] content = { 1, 2, 3, 4 };

        file.setContents(new ByteArrayInputStream(content), true, true);

        byte[] result = new byte[content.length];
        file.getContents().read(result);

        assertArrayEquals(content, result);
    }

    @Test
    public void testIfNotPresentExistIsFalse() throws Exception {
        createTestProjectFolder();
        IFile file = new IntelliJFileImpl(getMockProject(), new File(NEW_FILE_NAME));

        assertTrue(!file.exists());
    }

    @Test
    public void testExists() throws Exception {
        IFile file = createTestFile();

        assertTrue(file.exists());
    }

    @Test
    public void testCreate() throws Exception {
        createTestProjectFolder();
        IFile file = new IntelliJFileImpl(getMockProject(),
            new File("newCreateFile.txt"));

        file.create(new ByteArrayInputStream(new byte[] { }), false);

        assertTrue(file.exists());
    }

    @Test
    public void testGetSize() throws Exception {
        IFile file = createFileWithContent();

        assertEquals(4, file.getSize());
    }

    @Test
    public void testDelete() throws Exception {
        mockFileSystem();
        IFile file = createTestFile();

        file.delete(0);

        assertTrue(!file.exists());
    }

    @Test
    public void testMove() throws Exception {
        IFile file = createTestFile();
        String oldPath = file.getFullPath().toPortableString();
        IPath destination = IntelliJPathImpl.fromString(folder.getRoot().getPath())
            .append(TEST_PROJECT_NAME)
            .append("newFileName.txt");

        file.move(destination, false);

        assertTrue(!new File(oldPath).exists());
        assertTrue(file.exists());
        assertEquals(file.getFullPath(), destination);
        assertTrue(new File(destination.toPortableString()).exists());
    }

    @Test
    public void testGetFullPath() throws Exception {
        IFile file = createTestFile();

        assertEquals(IntelliJPathImpl.fromString(folder.getRoot().getPath())
                .append(TEST_PROJECT_NAME)
                .append(TESTFILE_NAME),
            file.getFullPath());
    }

    @Test
    public void testGetName() throws Exception {
        IFile file = createTestFile();

        assertEquals(TESTFILE_NAME, file.getName());
    }

    @Test
    public void testGetProjectRelativePath() throws Exception {
        IFile file = createTestFile();

        assertEquals(TESTFILE_NAME,
            file.getProjectRelativePath().toPortableString());
    }

    @Test
    public void testIsAccessible() throws Exception {
        IFile file = createTestFile();

        assertTrue(file.isAccessible());
    }

    private IntelliJFileImpl createTestFile() throws IOException {
        createTestProjectFolder();
        folder.newFile(TEST_PROJECT_NAME + "/" + TESTFILE_NAME);
        return new IntelliJFileImpl(getMockProject(), new File(TESTFILE_NAME));
    }

    private void createTestProjectFolder() throws IOException {
        folder.create();
        folder.newFolder(TEST_PROJECT_NAME);
    }

    private IFile createFileWithContent() throws Exception {
        IFile file = createTestFile();
        file.setContents(new ByteArrayInputStream(new byte[] { 1, 1, 1, 1 }),
            true, true);
        return file;
    }

    private void mockFileSystem() {
        PowerMock.mockStatic(LocalFileSystem.class);

        LocalFileSystem fs = new MockLocalFileSystem() {
            @Nullable
            @Override
            public VirtualFile refreshAndFindFileByIoFile(
                @NotNull
                File file) {
                return null;
            }
        };

        EasyMock.expect(LocalFileSystem.getInstance()).andReturn(fs);
        PowerMock.replay(LocalFileSystem.class);
    }

    private IntelliJProjectImpl getMockProject() {
        Project project = EasyMock.createNiceMock(Project.class);

        EasyMock.expect(project.getBasePath())
            .andReturn(folder.getRoot().getAbsolutePath());
        EasyMock.replay(project);
        return new IntelliJProjectImpl(project, TEST_PROJECT_NAME);
    }
}