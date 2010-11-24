/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universit�t Berlin - Fachbereich Mathematik und Informatik - 2006
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

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.FileListDiff;
import de.fu_berlin.inf.dpp.test.stubs.FileStub;

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

    protected IFile fileInRoot1 = new FileStub("root1", "fileInRoot1");
    protected IFile fileInRoot2 = new FileStub("root2", "fileInRoot2");
    protected IFile fileInSubDir1 = new FileStub("subdir/file1",
        "fileInSubDir1");
    protected IFile fileInSubDir2 = new FileStub("subdir/file2",
        "fileInSubDir2");
    protected IFile fileInSubDir1changed = new FileStub("subdir/file1",
        "changed fileInSubDir1");

    protected IFile[] threeFileArray = new IFile[] { fileInRoot1, fileInRoot2,
        fileInSubDir1 };

    protected FileList threeEntryList;
    protected FileList fourEntryList; // contains one additional entry
    // in respect to threeEntryList
    protected FileList modifiedFourEntryList; // contains one modified entry in
    // respect to fourEntryList
    protected FileList emptyFileList;

    @Before
    public void setUp() throws Exception {
        threeEntryList = new FileList(new IFile[] { fileInRoot1, fileInRoot2,
            fileInSubDir1 }, false);
        fourEntryList = new FileList(new IFile[] { fileInRoot1, fileInRoot2,
            fileInSubDir1, fileInSubDir2 }, false);
        modifiedFourEntryList = new FileList(new IFile[] { fileInRoot1,
            fileInRoot2, fileInSubDir1changed, fileInSubDir2 }, false);

        emptyFileList = new FileList();
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
    public void testMatch() {
        assertEquals(75, threeEntryList.computeMatch(fourEntryList));
        assertEquals(75, fourEntryList.computeMatch(threeEntryList));
        assertEquals(100, threeEntryList.computeMatch(threeEntryList));
        assertEquals(0, threeEntryList.computeMatch(emptyFileList));
        assertEquals(0, emptyFileList.computeMatch(threeEntryList));
        assertEquals(50, threeEntryList.computeMatch(modifiedFourEntryList));
        assertEquals(50, modifiedFourEntryList.computeMatch(threeEntryList));
    }

    @Test
    public void testEquals() throws CoreException {
        FileList sameFileList = new FileList(threeFileArray, false);
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

        assertEquals("Expected: '" + expected.toString() + "' actual: '"
            + actual + "'", expected.length, actual.size());
    }

}