package de.fu_berlin.inf.dpp.versioning;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JGitServiceTest {

    public enum RepositoryOwner {
        Host, Client
    }

    @Before
    public void setUp() {
    }

    public static Git getSettedUpRepo(RepositoryOwner owner)
        throws IllegalStateException, GitAPIException, IOException {

        @Rule
        TemporaryFolder tempFolder = new TemporaryFolder();

        File tempSubfolder = tempFolder.newFolder("tempFolder"
            + owner.toString());
        File tempFile = tempSubfolder.createTempFile("tempFile", ".txt");
        Git git = Git.init().setDirectory(tempFile.getParentFile()).call();
        git.add().addFilepattern(tempFile.getPath()).call();
        git.commit().setMessage("Initial commit").call();
        git.tag().setName("ClientCheckout").setAnnotated(false)
            .setForceUpdate(true).call();

        // the Host will be 1 commit forward
        if (owner == RepositoryOwner.Host) {
            FileUtils.writeStringToFile(tempFile, "hello world");
            git.add().addFilepattern(tempSubfolder.getPath()).call(); // git
                                                                      // add
                                                                      // **
            Status status = git.status().call();
            git.commit().setMessage("added: " + status.getAdded()).call();
            git.tag().setName("HostCheckout").setAnnotated(false)
                .setForceUpdate(true).call();
        }
        return git;
    }

    @Test
    public void testWrite() throws IOException, IllegalStateException,
        GitAPIException {

        Git gitClient = getSettedUpRepo(RepositoryOwner.Client);

        Git gitHost = getSettedUpRepo(RepositoryOwner.Host);

        File bundle = JGitService.getBundleByTag(gitHost, "ClientCheckout");
        assertNotNull(bundle);

    }

}
