package de.fu_berlin.inf.dpp.negotiation;

import org.junit.Test;

import de.fu_berlin.inf.dpp.negotiation.FileList.MetaData;

public class FileListDiffTest extends AbstractFileListTest {

    private static final String FILE_A = "src/file_a";
    private static final String FILE_B = "src/file_b";
    private static final String FILE_C = "src/file_c";
    private static final String FILE_D = "src/file_d";

    private static final String FOLDER_A = "src/folder_a/";
    private static final String FOLDER_B = "src/folder_b/";
    private static final String FOLDER_C = "src/folder_c/";
    private static final String FOLDER_D = "src/folder_d/";

    @Test
    public void testFileDiffWithoutChecksum() {

        FileList a = new FileList();

        a.addPath(FILE_A);
        a.addPath(FILE_B);

        FileList b = new FileList();

        b.addPath(FILE_A);
        b.addPath(FILE_C);

        FileListDiff diff = FileListDiff.diff(a, b, false);

        assertPaths(diff.getAddedFiles(), FILE_C);
        assertPaths(diff.getRemovedFiles(), FILE_B);

        assertPaths(diff.getUnalteredFiles(), FILE_A);
        assertPaths(diff.getAlteredFiles());

        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getRemovedFolders());

        assertPaths(diff.getUnalteredFolders());
    }

    @Test
    public void testFileDiffWithDifferentChecksum() {

        MetaData m;

        FileList a = new FileList();

        a.addPath(FILE_A);

        m = new MetaData();
        m.checksum = 5;

        a.addPath(FILE_B, m, false);

        FileList b = new FileList();

        b.addPath(FILE_A);

        m = new MetaData();
        m.checksum = 6;

        b.addPath(FILE_B, m, false);

        b.addPath(FILE_C);

        FileListDiff diff = FileListDiff.diff(a, b, false);

        assertPaths(diff.getAddedFiles(), FILE_C);
        assertPaths(diff.getRemovedFiles());

        assertPaths(diff.getUnalteredFiles(), FILE_A);
        assertPaths(diff.getAlteredFiles(), FILE_B);

        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getRemovedFolders());

        assertPaths(diff.getUnalteredFolders());
    }

    @Test
    public void testAddFolders() {

        FileList a = new FileList();

        a.addPath(FOLDER_A, null, true);
        a.addPath(FOLDER_B, null, true);

        FileList b = new FileList();

        b.addPath(FOLDER_A, null, true);
        b.addPath(FOLDER_B, null, true);
        b.addPath(FOLDER_C, null, true);
        b.addPath(FOLDER_D, null, true);

        FileListDiff diff = FileListDiff.diff(a, b, false);

        assertPaths(diff.getAddedFiles());
        assertPaths(diff.getRemovedFiles());

        assertPaths(diff.getUnalteredFiles());
        assertPaths(diff.getAlteredFiles());

        assertPaths(diff.getAddedFolders(), FOLDER_C, FOLDER_D);
        assertPaths(diff.getRemovedFolders());

        assertPaths(diff.getUnalteredFolders(), FOLDER_A, FOLDER_B);
    }

    @Test
    public void testRemoveFolders() {

        FileList a = new FileList();

        a.addPath(FOLDER_A, null, true);
        a.addPath(FOLDER_B, null, true);
        a.addPath(FOLDER_C, null, true);
        a.addPath(FOLDER_D, null, true);

        FileList b = new FileList();

        b.addPath(FOLDER_A, null, true);
        b.addPath(FOLDER_B, null, true);

        FileListDiff diff = FileListDiff.diff(a, b, false);

        assertPaths(diff.getAddedFiles());
        assertPaths(diff.getRemovedFiles());

        assertPaths(diff.getUnalteredFiles());
        assertPaths(diff.getAlteredFiles());

        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getRemovedFolders(), FOLDER_C, FOLDER_D);

        assertPaths(diff.getUnalteredFolders(), FOLDER_A, FOLDER_B);
    }

    @Test
    public void testExcludeRemoved() {

        FileList a = new FileList();

        a.addPath(FOLDER_A, null, true);
        a.addPath(FOLDER_B, null, true);
        a.addPath(FOLDER_C, null, true);
        a.addPath(FOLDER_D, null, true);

        a.addPath(FILE_A, null, false);
        a.addPath(FILE_B, null, false);
        a.addPath(FILE_C, null, false);
        a.addPath(FILE_D, null, false);

        FileListDiff diff = FileListDiff.diff(a, new FileList(), true);

        assertPaths(diff.getAddedFiles());
        assertPaths(diff.getRemovedFiles());

        assertPaths(diff.getUnalteredFiles());
        assertPaths(diff.getAlteredFiles());

        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getRemovedFolders());

        assertPaths(diff.getUnalteredFolders());
    }
}
