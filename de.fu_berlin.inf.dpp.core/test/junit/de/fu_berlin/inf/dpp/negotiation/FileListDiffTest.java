package de.fu_berlin.inf.dpp.negotiation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.fu_berlin.inf.dpp.filesystem.IResource;

public class FileListDiffTest extends AbstractFileListTest {

    @Test
    public void addedOne() {
        FileListDiff diff = FileListDiff.diff(threeEntryList, fourEntryList);

        /* Same as removedOne(), but removed and added are switched */
        assertPaths(diff.getAddedPaths(), SUBDIR_FILE2);
        assertPaths(diff.getRemovedPaths());
        assertPaths(diff.getAlteredPaths());
        assertPaths(diff.getUnalteredPaths(), ROOT1, ROOT2, SUBDIR_FILE1);
    }

    @Test
    public void addedFolders() throws Exception {
        String subdir = "subdir2/";

        List<IResource> resources = new ArrayList<IResource>();
        resources.add(createFolderMock(subdir));
        resources.add(fileInSubDir1);

        FileList list = FileListFactory.createFileList(null, resources, null,
            null);

        FileListDiff diff = FileListDiff.diff(emptyFileList, list);

        assertPaths(diff.getAddedFolders(), subdir);
    }

    @Test
    public void clearAddedFolders() throws Exception {
        List<IResource> resources = new ArrayList<IResource>();
        resources.add(createFolderMock("subdir2/"));
        resources.add(fileInSubDir1);

        FileList list = FileListFactory.createFileList(null, resources, null,
            null);

        FileListDiff diff = FileListDiff.diff(emptyFileList, list);

        diff.clearAddedFolders();

        assertPaths(diff.getAddedFolders());

        /* everything else should be unchanged */
        assertPaths(diff.getAddedPaths(), SUBDIR_FILE1);
        assertPaths(diff.getRemovedPaths());
        assertPaths(diff.getAlteredPaths());
        assertPaths(diff.getUnalteredPaths());
    }

    @Test
    public void removedOne() {
        FileListDiff diff = FileListDiff.diff(fourEntryList, threeEntryList);

        /* Same as addedOne(), but removed and added are switched */
        assertPaths(diff.getAddedPaths());
        assertPaths(diff.getRemovedPaths(), SUBDIR_FILE2);
        assertPaths(diff.getAlteredPaths());
        assertPaths(diff.getUnalteredPaths(), ROOT1, ROOT2, SUBDIR_FILE1);
    }

    @Test
    public void clearRemoved() {
        FileListDiff diff = FileListDiff.diff(fourEntryList, threeEntryList);

        diff.clearRemovedPaths();

        assertPaths(diff.getAddedPaths());
        assertPaths(diff.getRemovedPaths());
        assertPaths(diff.getAlteredPaths());
        assertPaths(diff.getUnalteredPaths(), ROOT1, ROOT2, SUBDIR_FILE1);
    }

    @Test
    public void alteredOne() {
        FileListDiff diff = FileListDiff.diff(fourEntryList,
            modifiedFourEntryList);

        /* Exact the same as the other way around */
        assertPaths(diff.getAddedPaths());
        assertPaths(diff.getRemovedPaths());
        assertPaths(diff.getAlteredPaths(), SUBDIR_FILE1);
        assertPaths(diff.getUnalteredPaths(), ROOT1, ROOT2, SUBDIR_FILE2);
    }

    @Test
    public void alteredOneReversed() {
        FileListDiff diff = FileListDiff.diff(modifiedFourEntryList,
            fourEntryList);

        /* Exact the same as the other way around */
        assertPaths(diff.getAddedPaths());
        assertPaths(diff.getRemovedPaths());
        assertPaths(diff.getAlteredPaths(), SUBDIR_FILE1);
        assertPaths(diff.getUnalteredPaths(), ROOT1, ROOT2, SUBDIR_FILE2);
    }

    @Test
    public void empty() {
        FileListDiff diff = FileListDiff.diff(fourEntryList, emptyFileList);

        assertPaths(diff.getAddedPaths());
        assertPaths(diff.getRemovedPaths(), ROOT1, ROOT2, SUBDIR_FILE1,
            SUBDIR_FILE2);
        assertPaths(diff.getAlteredPaths());
        assertPaths(diff.getUnalteredPaths());
    }

    @Test
    public void full() {
        FileListDiff diff = FileListDiff.diff(emptyFileList, fourEntryList);

        assertPaths(diff.getAddedPaths(), ROOT1, ROOT2, SUBDIR_FILE1,
            SUBDIR_FILE2);
        assertPaths(diff.getRemovedPaths());
        assertPaths(diff.getAlteredPaths());
        assertPaths(diff.getUnalteredPaths());
    }
}
