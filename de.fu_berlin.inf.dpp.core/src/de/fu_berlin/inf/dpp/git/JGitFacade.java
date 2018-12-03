package de.fu_berlin.inf.dpp.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.BundleWriter;

import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;

public class JGitFacade {
    private static final Logger LOG = Logger
        .getLogger(XMPPConnectionService.class);

    /**
     * 
     * Create a bundle File with all commits from commit at tag to commit at
     * 
     * HEAD of user.
     * 
     * 
     * 
     * @param user
     * 
     *            the one which have the commits for the bundle
     * 
     * @param tag
     * 
     *            assume that the recipient have at least the commit the tag is
     * 
     *            pointing to
     */

    public static File createBundleByTag(File fileChangable, String tag)
        throws IOException {

        Git user = Git.open(fileChangable.getParentFile());

        Repository repo = user.getRepository();

        Ref HEAD = repo.getRef("HEAD");

        Ref MASTER = repo.getRef("master");

        BundleWriter bundlewriter = new BundleWriter(repo);

        File bundle = File.createTempFile("file", ".bundle");

        OutputStream fos = new FileOutputStream(bundle);

        ProgressMonitor monitor = NullProgressMonitor.INSTANCE;

        bundlewriter.include(HEAD);

        bundlewriter.include(MASTER);

        RevWalk walk = new RevWalk(repo);

        RevCommit tagCommit = walk.parseCommit(repo.resolve(tag));

        bundlewriter.assume(tagCommit);

        bundlewriter.writeBundle(monitor, fos);

        return bundle;

    }

    /**
     * Creating a new Git Directory,add the fileChangable to it,create the first
     * commit and create a Tag "CheckoutAtInit"
     * 
     * 
     * @param fileChangable
     *            This file have to be in the same folder as the Git Directory
     */
    static void initNewRepo(File fileChangable) {
        Git git;
        try {
            git = Git.init().setDirectory(fileChangable.getParentFile()).call();
            git.add().addFilepattern(fileChangable.getPath()).call();
            git.commit().setMessage("Initial commit").call();
            git.tag().setName("CheckoutAtInit").setAnnotated(false)
                .setForceUpdate(true).call();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOG.debug("Error while init repo", e);
        }

    }

    /**
     * Change a existing Git Repo by add text to fileChangable, git add
     * fileChangable, git commit and create a Tag "CheckoutAt(numberOfCommit)"
     * 
     * 
     * 
     * @param fileChangable
     *            This file have to be in the same folder as the Git Directory
     * @param numberOfCommit
     *            The first used number should be 2 and than incremented by 1
     */
    static void writeCommitToRepo(File fileChangable, int numberOfCommit) {
        try {
            Git git = Git.open(fileChangable.getParentFile());
            FileUtils.writeStringToFile(fileChangable,
                System.getProperty("line.separator") + "Commit Nr."
                    + numberOfCommit);
            git.add().addFilepattern(fileChangable.getPath()).call(); // git
                                                                      // add
            // tempfile
            git.commit()
                .setMessage(
                    "changed tempfile.New Text: Commit Nr." + numberOfCommit)
                .call();
            git.tag().setName("CheckoutAtCommit" + numberOfCommit)
                .setAnnotated(false).setForceUpdate(true).call();
        } catch (Exception e) {
            LOG.debug("Error while create commit Nr." + numberOfCommit, e);
        }

    }
}
