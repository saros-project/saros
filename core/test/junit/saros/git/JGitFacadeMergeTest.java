package saros.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
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

  @Test
  public void testFetchFromBundleWithMerge()
      throws IOException, NoFilepatternException, GitAPIException, IllegalArgumentException,
          URISyntaxException {

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.ffMerge("refs/heads/bundle");

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "refs/heads/bundle"));

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDirTree, "HEAD"));
  }
}
