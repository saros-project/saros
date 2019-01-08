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
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
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
   * Create a bundle file with all commits from basis to actual of the given workDir.
   *
   * @param workDir The directory that contains the .git directory
   * @param actual the name of the ref to lookup. Must not be a short-handform; e.g., "master" is
   *     not automatically expanded to"refs/heads/master". Include the object the ref is point at in
   *     the bundle and (if the basis is an empty string) everything reachable from it.
   * @param basis assume that the recipient have at least the commit the basis is pointing to. In
   *     order to fetch from a bundle the recipient must have the commit the basis is pointing to.
   * @throws IOException if the workDir can't be accessed
   * @throws IllegalArgumentException if actual or the basis can't be resolved
   */
  public static File createBundle(File workDir, String actual, String basis)
      throws IOException, IllegalArgumentException, NullPointerException {
    Git user = Git.open(workDir);
    Repository repo = user.getRepository();
    Ref actualRef = repo.exactRef(actual);
    if (actualRef == null) throw new IllegalArgumentException("actual can't be resolved");
    BundleWriter bundlewriter = new BundleWriter(repo);
    File bundle = File.createTempFile("file", ".bundle");
    OutputStream fos = new FileOutputStream(bundle);
    ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
    bundlewriter.include(actualRef);

    if (basis != "") {
      RevWalk walk = new RevWalk(repo);
      try {
        RevCommit basisCommit = walk.parseCommit(repo.resolve(basis));
        bundlewriter.assume(basisCommit);
      } catch (Exception e) {
        throw new IllegalArgumentException("basis can't be resolved");
      }
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

    public static void cloneFromRepo(File from, File to) throws IOException,
        InvalidRemoteException, TransportException, GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(getUrlByWorkDir(from));
        cloneCommand.setDirectory(to);
        cloneCommand.call();
    }

    static String getUrlByWorkDir(File gitRepo) throws IOException {
        Git git = Git.open(gitRepo);
        return git.getRepository().getDirectory().getCanonicalPath();
    }
}
