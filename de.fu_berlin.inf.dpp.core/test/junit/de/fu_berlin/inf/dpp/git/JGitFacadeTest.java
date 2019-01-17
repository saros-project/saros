package de.fu_berlin.inf.dpp.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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
  private File localWorkDir;

  @Before
  public void setUp() throws IOException, IllegalStateException, GitAPIException {
    localWorkDir = tempFolder.newFolder("TempDir1");

    initNewRepo(localWorkDir);
    writeCommitToRepo(localWorkDir, 2);
  }
  // lucky paths
  @Test
  public void testCreateBundleHEAD() throws IllegalArgumentException, IOException {
    File bundle = JGitFacade.createBundle(localWorkDir, "HEAD", "");
    assertNotNull(bundle);
  }

  @Test
  public void testCreateBundleRef() throws IllegalArgumentException, IOException {
    File bundle = JGitFacade.createBundle(localWorkDir, "refs/heads/master", "");
    assertNotNull(bundle);
  }

  @Test
  public void testFetchFromBundle() throws IllegalArgumentException, Exception {
    File remoteWorkDir = tempFolder.newFolder("TempDir2");

    JGitFacade.cloneFromRepo(localWorkDir, remoteWorkDir);

    writeCommitToRepo(localWorkDir, 3);

    String basis = JGitFacade.getSHA1HashByRevisionString(remoteWorkDir, "HEAD");

    File bundle = JGitFacade.createBundle(localWorkDir, "HEAD", basis);

    assertNotEquals(
        getObjectIdByRevisionString(localWorkDir, "HEAD"),
        getObjectIdByRevisionString(remoteWorkDir, "FETCH_HEAD"));

    JGitFacade.fetchFromBundle(remoteWorkDir, bundle);

    assertEquals(
        getObjectIdByRevisionString(localWorkDir, "HEAD"),
        getObjectIdByRevisionString(remoteWorkDir, "FETCH_HEAD"));
  }
  // handling exceptions
  @Test(expected = IllegalArgumentException.class)
  public void testCreateBundleEmptyWorkDir() throws IllegalArgumentException, IOException {
    File emptyDir = tempFolder.newFolder("TempDir3");

    JGitFacade.createBundle(emptyDir, "HEAD", "CheckoutAtInit");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateBundleWrongRef() throws IllegalArgumentException, IOException {
    JGitFacade.createBundle(localWorkDir, "refs/heads/wrongRef", "CheckoutAtInit");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateBundleWrongBasis() throws IllegalArgumentException, IOException {
    JGitFacade.createBundle(localWorkDir, "HEAD", "WrongBasis");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFetchFromBundleEmptyDir() throws IllegalArgumentException, Exception {
    File emptyDir = tempFolder.newFolder("TempDir3");

    writeCommitToRepo(localWorkDir, 3);

    File bundle = JGitFacade.createBundle(localWorkDir, "HEAD", "CheckoutAtCommit2");

    JGitFacade.fetchFromBundle(emptyDir, bundle);
  }

  @Test(expected = Exception.class)
  public void testFetchFromBundleBasisMissing() throws IllegalArgumentException, Exception {
    File remoteWorkDir = tempFolder.newFolder("TempDir2");

    JGitFacade.cloneFromRepo(localWorkDir, remoteWorkDir);

    writeCommitToRepo(localWorkDir, 3);
    writeCommitToRepo(localWorkDir, 4);

    File bundle = JGitFacade.createBundle(localWorkDir, "HEAD", "CheckoutAtCommit3");

    JGitFacade.fetchFromBundle(remoteWorkDir, bundle);
  }

  /**
   * Creating a new Git directory,create a file and git add the file,create the first commit and
   * create the Tag "CheckoutAtInit" that is pointing to the commit
   *
   * @param workDir The directory that will contain the .git directory
   * @throws GitAPIException
   * @throws IllegalStateException
   * @throws IOException
   */
  private static void initNewRepo(File workDir)
      throws IllegalStateException, GitAPIException, IOException {
    Git git = Git.init().setDirectory(workDir).call();

    File testFile1 = new File(workDir, "testFile1");
    testFile1.createNewFile();

    git.add().addFilepattern(workDir.getAbsolutePath()).call();

    git.commit().setMessage("Initial commit").call();

    git.tag().setName("CheckoutAtInit").setAnnotated(false).setForceUpdate(true).call();
  }

  /**
   * Creating a new File, git add, git commit and create a Tag "CheckoutAtCommit(numberOfCommit)"
   *
   * @param workDir The directory that contains the .git directory
   * @param numberOfCommit The first used number should be 2 and than incremented by 1
   * @throws IOException
   * @throws GitAPIException
   * @throws NoFilepatternException
   */
  private static void writeCommitToRepo(File workDir, int numberOfCommit)
      throws IOException, NoFilepatternException, GitAPIException {
    Git git = Git.open(workDir);

    File testfile = new File(workDir, "testfile" + numberOfCommit);

    git.add().addFilepattern(workDir.getAbsolutePath()).call(); // git add .

    git.commit().setMessage("new file Commit Nr." + numberOfCommit).call();

    git.tag()
        .setName("CheckoutAtCommit" + numberOfCommit)
        .setAnnotated(false)
        .setForceUpdate(true)
        .call();
  }

  private static ObjectId getObjectIdByRevisionString(File workDir, String rev)
      throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
          IOException {
    return Git.open(workDir).getRepository().resolve(rev);
  }
}
