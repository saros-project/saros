package saros.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JGitFacadeMergeTest {

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

    JGitFacadeTest.initNewRepo(localWorkDirTree);
    JGitFacadeTest.writeCommitToRepo(localWorkDirTree, 2);

    remoteWorkDirTree = tempFolder.newFolder("TempDir2");

    JGitFacadeTest.cloneFromRepo(localWorkDirTree, remoteWorkDirTree);

    JGitFacadeTest.writeCommitToRepo(localWorkDirTree, 3);

    // All the set up before can't be done by the user with Saros.
    localJGitFacade = new JGitFacade(localWorkDirTree);
    remoteJGitFacade = new JGitFacade(remoteWorkDirTree);

    String basis = remoteJGitFacade.getSHA1HashByRevisionString("HEAD");

    bundle = localJGitFacade.createBundle("HEAD", basis);
  }

  /**
   * @throws IOException
   * @throws GitAPIException
   * @throws NoWorkTreeException
   * @throws URISyntaxException
   * @throws IllegalArgumentException
   */
  @Test
  public void testPullAllChangesFromSessionUntrackedFiles()
      throws IOException, NoWorkTreeException, GitAPIException, IllegalArgumentException,
          URISyntaxException {
    File testfile3 = new File(remoteWorkDirTree, "testfile3");
    if (!testfile3.createNewFile()) {
      throw new IOException("Could not create file" + testfile3);
    }

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));

    Git localGit = Git.open(localWorkDirTree);
    assert (localGit.status().call().isClean());

    Git remoteGit = Git.open(remoteWorkDirTree);
    assert (!remoteGit.status().call().isClean());

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.ffMerge("refs/heads/bundle");

    assert (localGit.status().call().isClean());

    assert (remoteGit.status().call().isClean());

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "refs/heads/bundle"));

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));
  }

  /**
   * @throws IOException
   * @throws GitAPIException
   * @throws NoWorkTreeException
   * @throws URISyntaxException
   * @throws IllegalArgumentException
   */
  @Test
  public void testPullAllChangesFromSessionAddedFiles()
      throws IOException, NoWorkTreeException, GitAPIException, IllegalArgumentException,
          URISyntaxException {
    File testfile3 = new File(remoteWorkDirTree, "testfile3");
    if (!testfile3.createNewFile()) {
      throw new IOException("Could not create file" + testfile3);
    }

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));

    Git localGit = Git.open(localWorkDirTree);
    assert (localGit.status().call().isClean());

    Git remoteGit = Git.open(remoteWorkDirTree);

    remoteGit.add().addFilepattern("testfile3").call();

    assert (!remoteGit.status().call().isClean());

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.ffMerge("refs/heads/bundle");

    assert (localGit.status().call().isClean());

    assert (remoteGit.status().call().isClean());

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "refs/heads/bundle"));

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));
  }

  /**
   * @throws IOException
   * @throws GitAPIException
   * @throws NoWorkTreeException
   * @throws URISyntaxException
   * @throws IllegalArgumentException
   */
  @Test
  public void testPullChangesNotFromSession()
      throws IOException, NoWorkTreeException, GitAPIException, IllegalArgumentException,
          URISyntaxException {
    File testfile3 = new File(remoteWorkDirTree, "testfile3");
    if (!testfile3.createNewFile()) {
      throw new IOException("Could not create file" + testfile3);
    }

    Git remoteGit = Git.open(remoteWorkDirTree);
    remoteGit.add().addFilepattern("testfile3").call();

    if (!testfile3.delete()) {
      throw new IOException("Could not delete file" + testfile3);
    }

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));

    Git localGit = Git.open(localWorkDirTree);
    assert (localGit.status().call().isClean());

    assert (!remoteGit.status().call().isClean());

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.fastForwardMerge("refs/heads/bundle");

    assert (localGit.status().call().isClean());

    assert (!remoteGit.status().call().isClean());

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "refs/heads/bundle"));

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));
  }

  /**
   * @throws IOException
   * @throws NoFilepatternException
   * @throws GitAPIException
   * @throws IllegalArgumentException
   * @throws URISyntaxException
   */
  @Test
  public void testPullNoChanges()
      throws IOException, NoFilepatternException, GitAPIException, IllegalArgumentException,
          URISyntaxException {
    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));

    Git localGit = Git.open(localWorkDirTree);
    assert (localGit.status().call().isClean());

    Git remoteGit = Git.open(remoteWorkDirTree);
    assert (remoteGit.status().call().isClean());

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.ffMerge("refs/heads/bundle");

    assert (localGit.status().call().isClean());

    assert (remoteGit.status().call().isClean());

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "refs/heads/bundle"));

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));
  }
}
