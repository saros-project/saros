/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.FileListDiff;
import de.fu_berlin.inf.dpp.FileListFactory;

/**
 * TODO [TEST] Add Testcases for non-existing files florianthiel: Does FileList
 * care about existence of files?
 * 
 * TODO [TEST] Add Testcases for derived files florianthiel: What are
 * "derived files" in this context?
 * 
 * FIXME FileList now uses IResource.getProject(), which isn't implemented yet
 * by FileStub, so any test
 */
public class FileListTest {

    private static IProject project;

    static {
        project = EasyMock.createNiceMock(IProject.class);
        EasyMock.expect(project.getName()).andReturn("Foo").anyTimes();
        EasyMock.replay(project);
    }

    private IFile fileInRoot1;
    private IFile fileInRoot2;
    private IFile fileInSubDir1;
    private IFile fileInSubDir2;
    private IFile fileInSubDir1changed;

    private List<IResource> threeFileList = new ArrayList<IResource>();

    private FileList threeEntryList;
    private FileList fourEntryList; // contains one additional entry
    // in respect to threeEntryList
    private FileList modifiedFourEntryList; // contains one modified entry in
    // respect to fourEntryList
    private FileList emptyFileList;

    // needed as the mocks will always return the same input stream instance
    private static class ResetingInputStream extends InputStream {

        private final String content;
        private ByteArrayInputStream in;

        public ResetingInputStream(String content) {
            this.content = content;
            in = new ByteArrayInputStream(this.content.getBytes());
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public void close() throws IOException {
            in = new ByteArrayInputStream(this.content.getBytes());
        }

    }

    private IFile createFileMock(String path, String content) throws Exception {
        IPath p = new Path(path);
        IPath f = new Path(project.getName() + "/" + path);

        IFile fileMock;

        fileMock = EasyMock.createMock(IFile.class);

        EasyMock.expect(fileMock.getProject()).andStubReturn(project);
        EasyMock.expect(fileMock.getProjectRelativePath()).andStubReturn(p);
        EasyMock.expect(fileMock.getContents()).andStubReturn(
            new ResetingInputStream(content));
        EasyMock.expect(fileMock.isDerived()).andStubReturn(false);
        EasyMock.expect(fileMock.exists()).andStubReturn(true);
        EasyMock.expect(fileMock.getType()).andStubReturn(IResource.FILE);
        EasyMock.expect(fileMock.getName()).andStubReturn(p.lastSegment());
        EasyMock.expect(fileMock.getFullPath()).andStubReturn(f);
        EasyMock.replay(fileMock);
        return fileMock;
    }

    @Before
    public void setUp() throws Exception {

        fileInRoot1 = createFileMock("root1", "fileInRoot1");
        fileInRoot2 = createFileMock("root2", "fileInRoot2");
        fileInSubDir1 = createFileMock("subdir/file1", "fileInSubDir1");
        fileInSubDir2 = createFileMock("subdir/file2", "fileInSubDir2");
        fileInSubDir1changed = createFileMock("subdir/file1",
            "changed fileInSubDir1");

        List<IResource> resources = new ArrayList<IResource>();
        resources.add(fileInRoot1);
        resources.add(fileInRoot2);
        resources.add(fileInSubDir1);
        threeFileList.addAll(resources);

        threeEntryList = FileListFactory.createFileList(null, resources, null,
            false, null);

        resources.add(fileInSubDir2);

        fourEntryList = FileListFactory.createFileList(null, resources, null,
            false, null);

        resources.remove(fileInSubDir1);
        resources.add(fileInSubDir1changed);

        modifiedFourEntryList = FileListFactory.createFileList(null, resources,
            null, false, null);

        emptyFileList = FileListFactory.createEmptyFileList();
    }

    @Test
    public void testGetFilePaths() {
        List<IPath> paths = threeEntryList.getPaths();

        assertPaths(new String[] { "root1", "root2", "subdir/file1" }, paths);
    }

    // @Test
    // public void testGetFileUnalteredPaths() {
    // Collection<IPath> paths = threeEntryList.getUnalteredPaths();
    //
    // assertPaths(new String[] { "root1", "root2", "subdir/file1" }, paths);
    // }

    @Test
    public void testDiffGetAddedFilePaths() {
        Collection<IPath> paths = FileListDiff.diff(threeEntryList,
            fourEntryList).getAddedPaths();

        assertPaths(new String[] { "subdir/file2" }, paths);
    }

    @Test
    public void testReversedDiffGetAddedFilePaths() {
        Collection<IPath> paths = FileListDiff.diff(fourEntryList,
            threeEntryList).getAddedPaths();

        assertPaths(new String[] {}, paths);
    }

    @Test
    public void testDiffGetRemovedFilePaths() {
        Collection<IPath> paths = fourEntryList.diff(threeEntryList)
            .getRemovedPaths();

        assertPaths(new String[] { "subdir/file2" }, paths);
    }

    @Test
    public void testReversedDiffGetRemovedFilePaths() {
        Collection<IPath> paths = threeEntryList.diff(fourEntryList)
            .getRemovedPaths();

        assertPaths(new String[] {}, paths);
    }

    @Test
    public void testDiffGetAlteredFilePaths() {
        Collection<IPath> paths = fourEntryList.diff(modifiedFourEntryList)
            .getAlteredPaths();

        assertPaths(new String[] { "subdir/file1" }, paths);
    }

    @Test
    public void testReversedDiffGetAlteredFilePaths() {
        Collection<IPath> paths = modifiedFourEntryList.diff(fourEntryList)
            .getAlteredPaths();

        assertPaths(new String[] { "subdir/file1" }, paths);
    }

    @Test
    public void testDiffGetAlteredFilesAddedFiles() {
        Collection<IPath> paths = threeEntryList.diff(fourEntryList)
            .getAlteredPaths();

        assertPaths(new String[] {}, paths);
    }

    @Test
    public void testDiffGetUnalteredFilePaths() {
        Collection<IPath> paths = fourEntryList.diff(modifiedFourEntryList)
            .getUnalteredPaths();

        assertPaths(new String[] { "root1", "root2", "subdir/file2" }, paths);
    }

    @Test
    public void testReversedDiffGetUnalteredFilePaths() {
        Collection<IPath> paths = modifiedFourEntryList.diff(threeEntryList)
            .getUnalteredPaths();

        assertPaths(new String[] { "root1", "root2" }, paths);
    }

    @Test
    // FIXME ndh: Needs checking.
    public void testDiffGetFilePaths() {
        Collection<IPath> paths = threeEntryList.diff(modifiedFourEntryList)
            .getAddedPaths();

        assertPaths(new String[] { "subdir/file2" }, paths);

        paths = emptyFileList.diff(threeEntryList).getAddedPaths();
        assertPaths(new String[] { "root1", "root2", "subdir/file1" }, paths);
        paths = threeEntryList.diff(emptyFileList).getRemovedPaths();
        assertPaths(new String[] { "root1", "root2", "subdir/file1" }, paths);
    }

    @Test
    public void testEquals() throws CoreException {
        FileList sameFileList = FileListFactory.createFileList(null,
            threeFileList, null, false, null);
        assertEquals(threeEntryList, sameFileList);
        assertEquals(emptyFileList, emptyFileList);

        assertFalse(threeEntryList.equals(fourEntryList));
        assertFalse(emptyFileList.equals(threeEntryList));
    }

    @Test
    public void testRoundtripSerialization() {
        FileList replicated = FileList.fromXML(threeEntryList.toXML());
        assertEquals(threeEntryList, replicated);
    }

    private void assertPaths(String[] expected, Collection<IPath> actual) {
        for (int i = 0; i < expected.length; i++) {
            Path path = new Path(expected[i]);
            assertTrue("Expected " + path + " to appear in: " + actual,
                actual.contains(path));
        }

        assertEquals(
            Arrays.toString(expected) + " != "
                + Arrays.toString(actual.toArray()), expected.length,
            actual.size());
    }

    @Test
    public void testToXmlAndBack() throws Exception {

        List<IPath> files = new ArrayList<IPath>();
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int a = 0; a < 2; a++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 2; k++) {
                    builder.setLength(0);
                    builder.append("string12345");
                    for (int j = 0; j < 5; j++) {
                        builder.append((char) (random.nextInt(26) + 65));
                    }
                    files.add(new Path("foo1234567890" + i + "/bar1234567890"
                        + a + "/" + builder.toString()));
                }
            }
        }

        FileList list = FileListFactory.createPathFileList(files);
        String xml = list.toXML();
        FileList listFromXml = FileList.fromXML(xml);
        assertEquals(list, listFromXml);
    }
}