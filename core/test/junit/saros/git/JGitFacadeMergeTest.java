package saros.git;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.junit.Before;
import org.junit.Test;

public class JGitFacadeMergeTest extends JGitTestCase {

  @Override
  @Before
  public void setUp() throws IOException, IllegalStateException, GitAPIException {
    super.setUp();

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

    assertNotEqualRevisionStrings("HEAD", "HEAD");

    Git localGit = Git.open(localWorkDirTree);
    assert (localGit.status().call().isClean());

    Git remoteGit = Git.open(remoteWorkDirTree);
    assert (!remoteGit.status().call().isClean());

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.ffMerge("refs/heads/bundle");

    assert (localGit.status().call().isClean());

    assert (remoteGit.status().call().isClean());

    assertEqualRevisionStrings("HEAD", "refs/heads/bundle");

    assertEqualRevisionStrings("HEAD", "HEAD");
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

    assertNotEqualRevisionStrings("HEAD", "HEAD");

    Git localGit = Git.open(localWorkDirTree);
    assert (localGit.status().call().isClean());

    Git remoteGit = Git.open(remoteWorkDirTree);

    remoteGit.add().addFilepattern("testfile3").call();

    assert (!remoteGit.status().call().isClean());

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.ffMerge("refs/heads/bundle");

    assert (localGit.status().call().isClean());

    assert (remoteGit.status().call().isClean());

    assertEqualRevisionStrings("HEAD", "refs/heads/bundle");

    assertEqualRevisionStrings("HEAD", "HEAD");
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

    assertNotEqualRevisionStrings("HEAD", "HEAD");

    Git localGit = Git.open(localWorkDirTree);
    assert (localGit.status().call().isClean());

    assert (!remoteGit.status().call().isClean());

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.fastForwardMerge("refs/heads/bundle");

    assert (localGit.status().call().isClean());

    assert (!remoteGit.status().call().isClean());

    assertEqualRevisionStrings("HEAD", "refs/heads/bundle");

    assertNotEqualRevisionStrings("HEAD", "HEAD");
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
    assertNotEqualRevisionStrings("HEAD", "HEAD");

    Git localGit = Git.open(localWorkDirTree);
    assert (localGit.status().call().isClean());

    Git remoteGit = Git.open(remoteWorkDirTree);
    assert (remoteGit.status().call().isClean());

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.ffMerge("refs/heads/bundle");

    assert (localGit.status().call().isClean());

    assert (remoteGit.status().call().isClean());

    assertEqualRevisionStrings("HEAD", "refs/heads/bundle");

    assertNotEqualRevisionStrings("HEAD", "HEAD");
  }
}
