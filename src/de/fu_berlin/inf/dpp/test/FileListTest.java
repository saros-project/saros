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
package de.fu_berlin.inf.dpp.test;

import java.io.IOException;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.test.stubs.FileStub;

public class FileListTest extends TestCase {
    private IFile[]  files;
    private IFile[]  otherFiles;

    private FileList fileList;
    private FileList otherFileList;
    
    @Override
    protected void setUp() throws Exception {
        files = new IFile[] {
            new FileStub("root.txt", "this in the root"),
            new FileStub("foo/bar/unit.java", "class Test {}"),
            new FileStub("foo/bar/test.txt", "")
        };
        fileList = new FileList(files);
        
        otherFiles = new IFile[] {
            new FileStub("root.txt", "this in the root"),
            new FileStub("foo/bar/unit.java", "class Test {void foo(){}}"),
            new FileStub("foo/test.txt", "another test content")
        };
        otherFileList = new FileList(otherFiles);
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testGetFilePaths() {
        Set<IPath> paths = fileList.getPaths();
        
        assertPaths(new String[]{"root.txt", "foo/bar/unit.java", "foo/bar/test.txt"}, paths);
    }
    
    public void testGetFileUnalteredPaths() {
        Set<IPath> paths = fileList.getUnalteredPaths();
        
        assertPaths(new String[]{"root.txt", "foo/bar/unit.java", "foo/bar/test.txt"}, paths);
    }
    
    public void testDiffGetAddedFilePaths() {
        Set<IPath> paths = fileList.diff(otherFileList).getAddedPaths();        

        assertPaths(new String[]{"foo/test.txt"}, paths);
    }
    
    public void testReversedDiffGetAddedFilePaths() {
        Set<IPath> paths = otherFileList.diff(fileList).getAddedPaths(); 

        assertPaths(new String[]{"foo/bar/test.txt"}, paths);
    }
    
    public void testDiffGetRemovedFilePaths() {
        Set<IPath> paths = fileList.diff(otherFileList).getRemovedPaths();        

        assertPaths(new String[]{"foo/bar/test.txt"}, paths);
    }
    
    public void testReversedDiffGetRemovedFilePaths() {
        Set<IPath> paths = otherFileList.diff(fileList).getRemovedPaths();        

        assertPaths(new String[]{"foo/test.txt"}, paths);
    }
    
    public void testDiffGetAlteredFilePaths() {
        Set<IPath> paths = fileList.diff(otherFileList).getAlteredPaths();        

        assertPaths(new String[]{"foo/bar/unit.java"}, paths);
    }
    
    public void testReversedDiffGetAlteredFilePaths() {
        Set<IPath> paths = otherFileList.diff(fileList).getAlteredPaths();        

        assertPaths(new String[]{"foo/bar/unit.java"}, paths);
    }
    
    public void testDiffGetUnalteredFilePaths() {
        Set<IPath> paths = fileList.diff(otherFileList).getUnalteredPaths();        

        assertPaths(new String[]{"root.txt"}, paths);
    }
    
    public void testReversedDiffGetUnalteredFilePaths() {
        Set<IPath> paths = otherFileList.diff(fileList).getUnalteredPaths();        

        assertPaths(new String[]{"root.txt"}, paths);
    }
    
    public void testDiffGetFilePaths() {
        Set<IPath> paths = fileList.diff(otherFileList).getPaths();
        
        assertPaths(new String[]{"root.txt", "foo/bar/unit.java", "foo/test.txt"}, paths);
    }
    
    public void testMatch() {
        assertEquals(33, fileList.match(otherFileList));
        assertEquals(33, otherFileList.match(fileList));
        assertEquals(100, fileList.match(fileList));
    }
    
    public void testEquals() {
        FileList sameFileList = new FileList(files);
        assertEquals(fileList, sameFileList);
        
        assertFalse(fileList.equals(otherFileList));
    }
    
    public void testRoundtripSerialization() throws XmlPullParserException, IOException {
        FileList replicated = new FileList(fileList.toXML());
        assertEquals(fileList, replicated);
    }
    
    private void assertPaths(String[] expected, Set<IPath> actual) {
        for (int i = 0; i < expected.length; i++) {
            Path path = new Path(expected[i]);
            assertTrue(actual.contains(path));
        }
        
        assertEquals(expected.length, actual.size());
    }
}
