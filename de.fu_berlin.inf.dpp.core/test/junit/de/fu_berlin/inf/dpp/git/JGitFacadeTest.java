package de.fu_berlin.inf.dpp.git;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.fu_berlin.inf.dpp.versioning.JGitService;

public class JGitFacadeTest {

    @Before
    public void setUp() {

    }

    public static Git createTestRepo(int amountCommits)
        throws IllegalStateException, GitAPIException, IOException {

        @Rule
        TemporaryFolder tempFolder = new TemporaryFolder();

        File tempSubfolder = tempFolder.newFolder("tempFolder" + amountCommits);
        File tempFile = tempSubfolder.createTempFile("tempFile", ".txt");
        Git git = Git.init().setDirectory(tempFile.getParentFile()).call();
        git.add().addFilepattern(tempFile.getPath()).call();
        git.commit().setMessage("Initial commit").call();
        git.tag().setName("CheckoutAtInit").setAnnotated(false)
            .setForceUpdate(true).call();

        for (int i = 2; i <= amountCommits; i++) {
            FileUtils.writeStringToFile(tempFile,
                System.getProperty("line.separator") + "Commit Nr." + i);
            git.add().addFilepattern(tempFile.getPath()).call(); // git add
                                                                 // tempfile
            git.commit()
                .setMessage("changed tempfile.New Text: Commit Nr." + i).call();
            git.tag().setName("CheckoutAtCommit" + i).setAnnotated(false)
                .setForceUpdate(true).call();
        }
        return git;
    }

    @Test
    public void testCreateBundle() throws IOException, IllegalStateException,
        GitAPIException {

        Git gitCommitAHead = createTestRepo(2);
        File bundle = JGitService.getBundleByTag(gitCommitAHead,
            "CheckoutAtInit");
        assertNotNull(bundle);

    }

}
