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
  private File localWorkDirTree;
  private JGitFacade localJGitFacade;

  private byte[] bundle;

  // the remote user is receiving the bundle
  private File remoteWorkDirTree;
  private JGitFacade remoteJGitFacade;

  @Before
  public void setUp() throws IOException, IllegalStateException, GitAPIException {
    localWorkDirTree = tempFolder.newFolder("TempDir1");

    initNewRepo(localWorkDirTree);
    writeCommitToRepo(localWorkDirTree, 2);

    remoteWorkDirTree = tempFolder.newFolder("TempDir2");

    cloneFromRepo(localWorkDirTree, remoteWorkDirTree);

    writeCommitToRepo(localWorkDirTree, 3);

    // All the set up before can't be done by the user with Saros.
    localJGitFacade = new JGitFacade(localWorkDirTree);
    remoteJGitFacade = new JGitFacade(remoteWorkDirTree);
  }
  // lucky paths
  @Test
  public void testCreateBundleHEAD() throws IllegalArgumentException, IOException {
    bundle = localJGitFacade.createBundle("HEAD", "");
    assertNotNull(bundle);
  }

  @Test
  public void testCreateBundleRef() throws IllegalArgumentException, IOException {
    bundle = localJGitFacade.createBundle("refs/heads/master", "");
    assertNotNull(bundle);
  }

  @Test
  public void testFetchFromBundle() throws IllegalArgumentException, Exception {
    String basis = remoteJGitFacade.getSHA1HashByRevisionString("HEAD");

    bundle = localJGitFacade.createBundle("HEAD", basis);

    assertNotEquals(
        getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        getObjectIdByRevisionString(remoteWorkDirTree, "FETCH_HEAD"));

    remoteJGitFacade.fetchFromBundle(bundle);

    assertEquals(
        getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        getObjectIdByRevisionString(remoteWorkDirTree, "FETCH_HEAD"));
  }

  // handling exceptions
  @Test(expected = IOException.class)
  public void testCreateBundleEmptyworkDirTree() throws IllegalArgumentException, IOException {
    File emptyDir = tempFolder.newFolder("TempDir3");

    JGitFacade emptyDirJGitFacade = new JGitFacade(emptyDir);

    emptyDirJGitFacade.createBundle("HEAD", "CheckoutAtInit");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateBundleWrongRef() throws IllegalArgumentException, IOException {
    localJGitFacade.createBundle("refs/heads/wrongRef", "CheckoutAtInit");
  }

  @Test(expected = IOException.class)
  public void testCreateBundleWrongBasis() throws IllegalArgumentException, IOException {
    localJGitFacade.createBundle("HEAD", "WrongBasis");
  }

  @Test(expected = IOException.class)
  public void testFetchFromBundleEmptyDir()
      throws IllegalArgumentException, IOException, GitAPIException, URISyntaxException {
    File emptyDir = tempFolder.newFolder("TempDir3");

    JGitFacade emptyDirJGitFacade = new JGitFacade(emptyDir);

    writeCommitToRepo(localWorkDirTree, 3);

    bundle = localJGitFacade.createBundle("HEAD", "CheckoutAtCommit2");

    emptyDirJGitFacade.fetchFromBundle(bundle);
  }

  @Test(expected = IOException.class)
  public void testFetchFromBundleBasisMissing() throws IllegalArgumentException, Exception {
    writeCommitToRepo(localWorkDirTree, 4);

    bundle = localJGitFacade.createBundle("HEAD", "CheckoutAtCommit3");

    remoteJGitFacade.fetchFromBundle(bundle);
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
      Git.cloneRepository().setURI(getUrl(from)).setDirectory(to).call();
    } catch (InvalidRemoteException e) {
      throw new IllegalArgumentException(
          "workDirTree of where should be cloned to is null and can't be resolved", e);
    } catch (GitAPIException | IOException e) {
      throw new IOException("clone failed", e);
    }
  }

  /**
   * @param workDirTree The directory that contains the .git directory
   * @return The URL to access/address directly the .git directory
   * @throws IllegalArgumentException {@code workDirTree} is null can't be resolved
   * @throws IOException failed while read
   */
  private static String getUrl(File workDirTree) throws IllegalArgumentException, IOException {
    return Git.open(workDirTree).getRepository().getDirectory().getCanonicalPath();
  }
}
