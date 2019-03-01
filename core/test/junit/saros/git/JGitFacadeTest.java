package saros.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JGitFacadeTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  // the local user is creating the bundle
  private File localworkDirTree;

  @Before
  public void setUp() throws IOException, IllegalStateException, GitAPIException {
    localworkDirTree = tempFolder.newFolder("TempDir1");

    initNewRepo(localworkDirTree);
    writeCommitToRepo(localworkDirTree, 2);
  }
  // lucky paths
  @Test
  public void testCreateBundleHEAD() throws IllegalArgumentException, IOException {
    byte[] bundle = JGitFacade.createBundle(localworkDirTree, "HEAD", "");
    assertNotNull(bundle);
  }

  @Test
  public void testCreateBundleRef() throws IllegalArgumentException, IOException {
    byte[] bundle = JGitFacade.createBundle(localworkDirTree, "refs/heads/master", "");
    assertNotNull(bundle);
  }

  @Test
  public void testFetchFromBundle() throws IllegalArgumentException, Exception {
    File remoteworkDirTree = tempFolder.newFolder("TempDir2");

    cloneFromRepo(localworkDirTree, remoteworkDirTree);

    writeCommitToRepo(localworkDirTree, 3);

    String basis = JGitFacade.getSHA1HashByRevisionString(remoteworkDirTree, "HEAD");

    byte[] bundle = JGitFacade.createBundle(localworkDirTree, "HEAD", basis);

    assertNotEquals(
        getObjectIdByRevisionString(localworkDirTree, "HEAD"),
        getObjectIdByRevisionString(remoteworkDirTree, "FETCH_HEAD"));

    JGitFacade.fetchFromBundle(remoteworkDirTree, bundle);

    assertEquals(
        getObjectIdByRevisionString(localworkDirTree, "HEAD"),
        getObjectIdByRevisionString(remoteworkDirTree, "FETCH_HEAD"));
  }

  // handling exceptions
  @Test(expected = IOException.class)
  public void testCreateBundleEmptyworkDirTree() throws IllegalArgumentException, IOException {
    File emptyDir = tempFolder.newFolder("TempDir3");

    JGitFacade.createBundle(emptyDir, "HEAD", "CheckoutAtInit");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateBundleWrongRef() throws IllegalArgumentException, IOException {
    JGitFacade.createBundle(localworkDirTree, "refs/heads/wrongRef", "CheckoutAtInit");
  }

  @Test(expected = IOException.class)
  public void testCreateBundleWrongBasis() throws IllegalArgumentException, IOException {
    JGitFacade.createBundle(localworkDirTree, "HEAD", "WrongBasis");
  }

  @Test(expected = IOException.class)
  public void testFetchFromBundleEmptyDir()
      throws IllegalArgumentException, IOException, GitAPIException, URISyntaxException {
    File emptyDir = tempFolder.newFolder("TempDir3");

    writeCommitToRepo(localworkDirTree, 3);

    byte[] bundle = JGitFacade.createBundle(localworkDirTree, "HEAD", "CheckoutAtCommit2");

    JGitFacade.fetchFromBundle(emptyDir, bundle);
  }

  @Test(expected = IOException.class)
  public void testFetchFromBundleBasisMissing() throws IllegalArgumentException, Exception {
    File remoteworkDirTree = tempFolder.newFolder("TempDir2");

    cloneFromRepo(localworkDirTree, remoteworkDirTree);

    writeCommitToRepo(localworkDirTree, 3);
    writeCommitToRepo(localworkDirTree, 4);

    byte[] bundle = JGitFacade.createBundle(localworkDirTree, "HEAD", "CheckoutAtCommit3");

    JGitFacade.fetchFromBundle(remoteworkDirTree, bundle);
  }

  /**
   * Creating a new Git directory,create a file and git add the file,create the first commit and
   * create the Tag "CheckoutAtInit" that is pointing to the commit
   *
   * @param workDirTree The directory that will contain the .git directory
   * @throws GitAPIException
   * @throws IllegalStateException
   * @throws IOException
   */
  static void initNewRepo(File workDirTree)
      throws IllegalStateException, GitAPIException, IOException {
    Git git = Git.init().setDirectory(workDirTree).call();

    File testFile1 = new File(workDirTree, "testFile1");
    testFile1.createNewFile();

    git.add().addFilepattern(workDirTree.getAbsolutePath()).call();

    git.commit().setMessage("Initial commit").call();

    git.tag().setName("CheckoutAtInit").setAnnotated(false).setForceUpdate(true).call();
  }

  /**
   * Creating a new File, git add, git commit and create a Tag "CheckoutAtCommit(numberOfCommit)"
   *
   * @param workDirTree The directory that contains the .git directory
   * @param numberOfCommit The first used number should be 2 and than incremented by 1
   * @throws IOException
   * @throws GitAPIException
   * @throws NoFilepatternException
   */
  static void writeCommitToRepo(File workDirTree, int numberOfCommit)
      throws IOException, NoFilepatternException, GitAPIException {
    Git git = Git.open(workDirTree);

    File testfile = new File(workDirTree, "testfile" + numberOfCommit);

    git.add().addFilepattern(workDirTree.getAbsolutePath()).call(); // git add .

    git.commit().setMessage("new file Commit Nr." + numberOfCommit).call();

    git.tag()
        .setName("CheckoutAtCommit" + numberOfCommit)
        .setAnnotated(false)
        .setForceUpdate(true)
        .call();
  }
  /**
   * @param workDirTree The directory that contains the .git directory
   * @param revString See <a href="https://www.git-scm.com/docs/gitrevisions">gitrevisions</a>
   * @return A SHA-1 abstraction
   * @throws RevisionSyntaxException
   * @throws AmbiguousObjectException
   * @throws IncorrectObjectTypeException
   * @throws IOException
   */
  static ObjectId getObjectIdByRevisionString(File workDirTree, String revString)
      throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
          IOException {
    return Git.open(workDirTree).getRepository().resolve(revString);
  }

  /**
   * @param from The directory that contains the .git directory of where should be cloned from
   * @param to The directory that contains the .git directory of where should be cloned to
   * @throws IllegalArgumentException parameters can't be resolved. Maybe null
   * @throws IOException failed while cloned
   */
  static void cloneFromRepo(File from, File to) throws IllegalArgumentException, IOException {
    if (from == null)
      throw new IllegalArgumentException(
          "workDirTree of where should be cloned from is null and can't be resolved");
    if (to == null)
      throw new IllegalArgumentException(
          "workDirTree of where should be cloned to is null and can't be resolved");
    try {
      Git.cloneRepository().setURI(JGitFacade.getUrlByworkDirTree(from)).setDirectory(to).call();
    } catch (InvalidRemoteException e) {
      throw new IllegalArgumentException(
          "workDirTree of where should be cloned to is null and can't be resolved", e);
    } catch (GitAPIException | IOException e) {
      throw new IOException("clone failed", e);
    }
  }
}