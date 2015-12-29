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

import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class IntelliJFileImplTest extends AbstractResourceTest {

    @Test
    public void testCreate() throws Exception {
        createTestProjectFolder();
        IFile file = new IntelliJFileImpl(getMockProject(),
            new File("newCreateFile.txt"));

        file.create(new ByteArrayInputStream(new byte[] {}), false);

        assertTrue(file.exists());
    }

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
        String oldPath = file.getLocation().toPortableString();
        IPath destination = IntelliJPathImpl
            .fromString(folder.getRoot().getPath()).append(TEST_PROJECT_NAME)
            .append("newFileName.txt");

        file.move(destination, false);

        assertTrue(!new File(oldPath).exists());
        assertTrue(file.exists());
        assertEquals(file.getLocation(), destination);
        assertTrue(new File(destination.toPortableString()).exists());
    }

    protected IntelliJFileImpl createTestFile() throws IOException {
        createTestProjectFolder();
        folder.newFile(RELATIVE_TEST_RESOURCE_PATH);
        return new IntelliJFileImpl(getMockProject(), new File(TESTFILE_NAME));
    }

    protected IFile createFileWithContent() throws Exception {
        IFile file = createTestFile();
        file.setContents(new ByteArrayInputStream(new byte[] { 1, 1, 1, 1 }),
            true, true);
        return file;
    }
}