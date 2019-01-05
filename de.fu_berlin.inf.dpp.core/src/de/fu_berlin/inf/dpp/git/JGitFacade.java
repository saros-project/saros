package de.fu_berlin.inf.dpp.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.BundleWriter;
import org.eclipse.jgit.transport.URIish;

public class JGitFacade {
  private static final Logger LOG = Logger.getLogger(JGitFacade.class);

  /**
   * Create a bundle file with all commits from basis to actual of the given workDir.
   *
   * @param workDir The directory that contains the .git directory
   * @param actual the name of the ref to lookup. Must not be a short-handform; e.g., "master" is
   *     not automatically expanded to"refs/heads/master". Include the object the ref is point at in
   *     the bundle and (if the basis is an empty string) everything reachable from it.
   * @param basis assume that the recipient have at least the commit the basis is pointing to. In
   *     order to fetch from a bundle the recipient must have the commit the basis is pointing to.
   * @throws IOException if the workDir can't be accessed, actual can't be exact or the basis isn't
   *     empty but can't be resolved
   */
  public static File createBundle(File workDir, String actual, String basis)
      throws IOException, NullPointerException {
    Git user = Git.open(workDir);
    Repository repo = user.getRepository();
    Ref actualRef = repo.exactRef(actual);
    if (actualRef == null) throw new IOException();
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
        throw new IOException();
      }
    }
    bundlewriter.writeBundle(monitor, fos);
    return bundle;
  }

  /**
   * Fetching from a bundle file to an git Repo
   *
   * @param bundleFile
   * @param workDir The directory that contains the .git directory
   * @throws IOException
   * @throws GitAPIException
   */
  public static void fetchFromBundle(File bundleFile, File workDir)
      throws IOException, GitAPIException {
    Git git = Git.open(workDir);
    URIish bundleURI = new URIish().setPath(bundleFile.getCanonicalPath());
    git.remoteAdd().setUri(bundleURI).setName("bundle").call();
    git.fetch().setRemote("bundle").call();
  }

  public static void cloneFromRepo(File from, File to)
      throws IOException, InvalidRemoteException, TransportException, GitAPIException {
    CloneCommand cloneCommand = Git.cloneRepository();
    cloneCommand.setURI(getUrlByGitRepo(from));
    cloneCommand.setDirectory(to);
    cloneCommand.call();
  }

  /**
   * Creating a new Git directory,create a file and git add the file,create the first commit and
   * create the Tag "CheckoutAtInit" that is pointing to the commit
   *
   * @param workDir The directory that will contain the .git directory
   */
  static void initNewRepo(File workDir) {
    Git git;
    try {
      git = Git.init().setDirectory(workDir).call();
      File testFile1 = new File(workDir, "testFile1");
      testFile1.createNewFile();
      git.add().addFilepattern(workDir.getAbsolutePath()).call();
      git.commit().setMessage("Initial commit").call();
      git.tag().setName("CheckoutAtInit").setAnnotated(false).setForceUpdate(true).call();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      LOG.debug("Error while init repo", e);
    }
  }

  /**
   * Creating a new File, git add, git commit and create a Tag "CheckoutAtCommit(numberOfCommit)"
   *
   * @param workDir The directory that contains the .git directory
   * @param numberOfCommit The first used number should be 2 and than incremented by 1
   */
  static void writeCommitToRepo(File workDir, int numberOfCommit) {
    try {
      Git git = Git.open(workDir);
      File testfile = new File(workDir, "testfile" + numberOfCommit);
      git.add().addFilepattern(workDir.getAbsolutePath()).call(); // git add .
      git.commit().setMessage("new file Commit Nr." + numberOfCommit).call();
      git.tag()
          .setName("CheckoutAtCommit" + numberOfCommit)
          .setAnnotated(false)
          .setForceUpdate(true)
          .call();
    } catch (Exception e) {
      LOG.debug("Error while create commit Nr." + numberOfCommit, e);
    }
  }

  static File getMetaDataByGitRepo(File gitDir1) throws IOException {
    Git git = Git.open(gitDir1);
    File gitDir = git.getRepository().getDirectory();
    return gitDir;
  }

  static String getUrlByGitRepo(File gitRepo) throws IOException {
    Git git = Git.open(gitRepo);
    return git.getRepository().getDirectory().getCanonicalPath();
  }

  static void close(File gitRepo) throws IOException {
    Git git = Git.open(gitRepo);
    git.close();
  }

  static ObjectId getObjectIDbyRevisionString(File workDir, String rev)
      throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
          IOException {
    return Git.open(workDir).getRepository().resolve(rev);
  }
}
