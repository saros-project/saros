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

    private static final String FOLDER_A_FILE_A = "src/folder_a/file_a";

    @Test
    public void testFileDiffWithoutChecksum() {

        FileList a = new FileList();
        a.addPath(FILE_A);
        a.addPath(FILE_B);

        FileList b = new FileList();
        b.addPath(FILE_A);
        b.addPath(FILE_B);
        b.addPath(FILE_C);

        FileListDiff diff = FileListDiff.diff(a, b);

        assertPaths(diff.getAddedPaths(), FILE_C);
        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getAlteredPaths());
        assertPaths(diff.getRemovedPaths());
        assertPaths(diff.getUnalteredPaths(), FILE_A, FILE_B);
        assertPaths(diff.getRemovedPathsSanitized());
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

        FileListDiff diff = FileListDiff.diff(a, b);

        assertPaths(diff.getAddedPaths(), FILE_C);
        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getAlteredPaths(), FILE_B);
        assertPaths(diff.getRemovedPaths());
        assertPaths(diff.getUnalteredPaths(), FILE_A);
        assertPaths(diff.getRemovedPathsSanitized());
    }

    @Test
    public void addedFolders() throws Exception {

        FileList a = new FileList();
        a.addPath(FOLDER_B, null, true);

        FileList b = new FileList();
        b.addPath(FOLDER_A_FILE_A, null, false);
        b.addPath(FOLDER_C, null, true);

        FileListDiff diff = FileListDiff.diff(a, b);

        assertPaths(diff.getAddedPaths(), FOLDER_A_FILE_A, FOLDER_C);
        assertPaths(diff.getAddedFolders(), FOLDER_C);
        assertPaths(diff.getAlteredPaths());
        assertPaths(diff.getRemovedPaths(), FOLDER_B);
        assertPaths(diff.getUnalteredPaths());
        assertPaths(diff.getRemovedPathsSanitized(), FOLDER_B);
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

    @Test
    public void testFileDiffWithoutChecksumV2() {

        FileList a = new FileList();
        a.addPath(FILE_A);
        a.addPath(FILE_B);

        FileList b = new FileList();
        b.addPath(FILE_A);
        b.addPath(FILE_B);
        b.addPath(FILE_C);

        FileListDiff diff = FileListDiff.diff(a, b);

        assertPaths(diff.getAddedFiles(), FILE_C);
        assertPaths(diff.getRemovedFiles());

        assertPaths(diff.getUnalteredFiles(), FILE_A, FILE_B);
        assertPaths(diff.getAlteredFiles());

        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getRemovedFolders());
        assertPaths(diff.getUnalteredFolders());
    }

    @Test
    public void testFileDiffWithDifferentChecksumV2() {

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

        FileListDiff diff = FileListDiff.diff(a, b);

        assertPaths(diff.getAddedFiles(), FILE_C);
        assertPaths(diff.getRemovedFiles());

        assertPaths(diff.getUnalteredFiles(), FILE_A);
        assertPaths(diff.getAlteredFiles(), FILE_B);

        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getRemovedFolders());
        assertPaths(diff.getUnalteredFolders());
    }

    @Test
    public void addedFoldersV2() throws Exception {

        FileList a = new FileList();
        a.addPath(FOLDER_A, null, true);
        a.addPath(FOLDER_B, null, true);

        FileList b = new FileList();
        b.addPath(FOLDER_A, null, true);
        b.addPath(FOLDER_B, null, true);
        b.addPath(FOLDER_C, null, true);
        b.addPath(FOLDER_D, null, true);

        FileListDiff diff = FileListDiff.diff(a, b);

        assertPaths(diff.getAddedFiles());
        assertPaths(diff.getRemovedFiles());

        assertPaths(diff.getUnalteredFiles());
        assertPaths(diff.getAlteredFiles());

        assertPaths(diff.getAddedFolders(), FOLDER_C, FOLDER_D);
        assertPaths(diff.getRemovedFolders());
        assertPaths(diff.getUnalteredFolders(), FOLDER_A, FOLDER_B);
    }

    @Test
    public void removeFolders() throws Exception {

        FileList a = new FileList();
        a.addPath(FOLDER_A, null, true);
        a.addPath(FOLDER_B, null, true);
        a.addPath(FOLDER_C, null, true);
        a.addPath(FOLDER_D, null, true);

        FileList b = new FileList();
        b.addPath(FOLDER_A, null, true);
        b.addPath(FOLDER_B, null, true);

        FileListDiff diff = FileListDiff.diff(a, b);

        assertPaths(diff.getAddedFiles());
        assertPaths(diff.getRemovedFiles());

        assertPaths(diff.getUnalteredFiles());
        assertPaths(diff.getAlteredFiles());

        assertPaths(diff.getAddedFolders());
        assertPaths(diff.getRemovedFolders(), FOLDER_C, FOLDER_D);
        assertPaths(diff.getUnalteredFolders(), FOLDER_A, FOLDER_B);
    }
}
