package de.fu_berlin.inf.dpp.git;

import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.sql.Ref;
import java.util.Collections;
import java.util.Set;
import javax.swing.ProgressMonitor;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.BundleWriter;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TransportBundleStream;
import org.eclipse.jgit.transport.URIish;

public class JGitFacade {
    private static final Logger LOG = Logger
        .getLogger(XMPPConnectionService.class);

    /**
     * Create a bundle File with all commits from commit at tag to commit at
     * HEAD of the given workDir.
     *
     * @param workDir
     *            The Directory that contains the .git Directory
     * @param tag
     *            assume that the recipient have at least the commit the tag is
     *            pointing to. If tag is an empty String the bundle contains
     *            everything to HEAD and everything to master.
     */
    public static File createBundleByTag(File workDir, String tag)
        throws IOException {
        Git user = Git.open(workDir);
        Repository repo = user.getRepository();
        Ref HEAD = repo.exactRef("HEAD");
        Ref MASTER = repo.exactRef("refs/heads/master");
        BundleWriter bundlewriter = new BundleWriter(repo);
        File bundle = File.createTempFile("file", ".bundle");
        OutputStream fos = new FileOutputStream(bundle);
        ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
        if (HEAD != null)
            bundlewriter.include(HEAD);
        if (MASTER != null)
            bundlewriter.include(MASTER);

        if (tag != "") {
            RevWalk walk = new RevWalk(repo);
            RevCommit tagCommit = walk.parseCommit(repo.resolve(tag));
            bundlewriter.assume(tagCommit);
        }
        bundlewriter.writeBundle(monitor, fos);
        return bundle;
    }

    /**
     * Creating a new Git Directory,add a File to it,create the first commit and
     * the Tag "CheckoutAtInit" that is pointing to the commit
     *
     * @param workDir
     *            The Directory that will contain the .git Directory
     */
    static void initNewRepo(File workDir) {
        Git git;
        try {
            git = Git.init().setDirectory(workDir).call();
            File testFile1 = new File(workDir, "testFile1");
            testFile1.createNewFile();
            git.add().addFilepattern(workDir.getAbsolutePath()).call();
            git.commit().setMessage("Initial commit").call();
            git.tag().setName("CheckoutAtInit").setAnnotated(false)
                .setForceUpdate(true).call();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOG.debug("Error while init repo", e);
        }
    }

    /**
     * Change a existing Git Repo by creating a new File, git add, git commit
     * and create a Tag "CheckoutAtCommit(numberOfCommit)"
     *
     * @param workDir
     *            The Directory that contains the .git Directory
     * @param numberOfCommit
     *            The first used number should be 2 and than incremented by 1
     */
    static void writeCommitToRepo(File workDir, int numberOfCommit) {
        try {
            Git git = Git.open(workDir);
            File testfile = new File(workDir, "testfile" + numberOfCommit);
            git.add().addFilepattern(workDir.getAbsolutePath()).call(); // git
            // add
            // tempfile
            git.commit().setMessage("new file Commit Nr." + numberOfCommit)
                .call();
            git.tag().setName("CheckoutAtCommit" + numberOfCommit)
                .setAnnotated(false).setForceUpdate(true).call();
        } catch (Exception e) {
            LOG.debug("Error while create commit Nr." + numberOfCommit, e);
        }
    }

    /**
     * Fetching from bundle to an git Repo
     *
     * @param bundleFile
     * @param workDir
     *            The Directory that contains the .git Directory
     * @throws IOException
     * @throws GitAPIException
     */
    public static void unbundle(File bundleFile, File workDir)
        throws IOException, GitAPIException {
        Git git = Git.open(workDir);
        URIish bundleURI = new URIish().setPath(bundleFile.getCanonicalPath());
        git.remoteAdd().setUri(bundleURI).setName("bundle").call();
        git.fetch().setRemote("bundle").call();
    }

    static File getMetaDataByGitRepo(File gitDir1) throws IOException {
        Git git = Git.open(gitDir1);
        File gitDir = git.getRepository().getDirectory();
        return gitDir;
    }

    public static String getUrlByGitRepo(File gitRepo) throws IOException {
        Git git = Git.open(gitRepo);
        return git.getRepository().getDirectory().getCanonicalPath();
    }

    public static void close(File gitRepo) throws IOException {
        Git git = Git.open(gitRepo);
        git.close();
    }

    public static void clone(File from, File to) throws IOException,
        InvalidRemoteException, TransportException, GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(getUrlByGitRepo(from));
        cloneCommand.setDirectory(to);
        cloneCommand.call();
    }

    static FetchResult fetchFromBundle(final File workDir,
        final File bundleFile) throws URISyntaxException, IOException {
        final URIish uri = new URIish("in-memory://");
        final FileInputStream fis = new FileInputStream(bundleFile);
        final RefSpec rs = new RefSpec("refs:heads/*:refs/heads/*");
        final Set<RefSpec> refs = Collections.singleton(rs);
        try (Repository workRepo = Git.open(workDir).getRepository();
            TransportBundleStream transport = new TransportBundleStream(
                workRepo, uri, fis)) {
            return transport.fetch(NullProgressMonitor.INSTANCE, refs);
        }
    }
}
