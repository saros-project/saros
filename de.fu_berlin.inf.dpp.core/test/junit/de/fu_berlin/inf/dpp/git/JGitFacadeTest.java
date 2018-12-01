package de.fu_berlin.inf.dpp.git;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JGitFacadeTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Method to create file,folder and .git Directory with a given number of
     * commits. Commits can be accessed by Tag (The first is named
     * CheckoutAtInit, the following CheckoutAtCommit2,CheckoutAtCommit3,...)
     * 
     * @param iD
     *            Test should never use the same iD twice
     * @param amountCommits
     *            At least 1 will be created. If >= 2 more than that.
     * 
     * @return The file that will be used to identify the folder with the Git
     *         Directory
     */
    File createTestFile(int amountCommits, int iD) {

        File tempSubfolder = tempFolder.newFolder("tempFolder" + iD);
        try {
            File tempFile = tempSubfolder.createTempFile("tempFile", ".txt");
            JGitFacade.initNewRepo(tempFile);
            for (int i = 2; i <= amountCommits; i++) {
                JGitFacade.writeCommitToRepo(tempFile, i);

            }
            return tempFile;
        } catch (IOException e) {

        }
        return null;
    }

    @Test
    public void testCreateBundle() throws IOException {

        File firstRepoTempFile = createTestFile(2, 1);
        File bundle = JGitFacade.createBundleByTag(firstRepoTempFile,
            "CheckoutAtInit");
        assertNotNull(bundle);

    }

    @Test
    public void testCreateBundleRaisesNullPointerExeption() throws IOException {
        try {
            File bundle2 = JGitFacade.createBundleByTag(null, "CheckoutAtInit");
            fail("created Bundle with null File");
        } catch (NullPointerException e) {
        }
    }

}
