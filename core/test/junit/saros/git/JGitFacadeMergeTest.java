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
  private File localWorkDir;
  private JGitFacade localJGitFacade;

  private byte[] bundle;

  private File remoteWorkDir;
  private JGitFacade remoteJGitFacade;

  @Before
  public void setUp() throws IOException, IllegalStateException, GitAPIException {
    localWorkDir = tempFolder.newFolder("TempDir1");

    localJGitFacade = new JGitFacade(localWorkDir);

    JGitFacadeTest.initNewRepo(localWorkDir);
    JGitFacadeTest.writeCommitToRepo(localWorkDir, 2);

    remoteWorkDir = tempFolder.newFolder("TempDir2");

    remoteJGitFacade = new JGitFacade(remoteWorkDir);

    JGitFacadeTest.cloneFromRepo(localWorkDir, remoteWorkDir);

    JGitFacadeTest.writeCommitToRepo(localWorkDir, 3);

    String basis = remoteJGitFacade.getSHA1HashByRevisionString("HEAD");

    bundle = localJGitFacade.createBundle("HEAD", basis);
  }

  @Test
  public void testFetchFromBundleWithMerge()
      throws IOException, NoFilepatternException, GitAPIException, IllegalArgumentException,
          URISyntaxException {

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDir, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDir, "HEAD"));

    remoteJGitFacade.fetchFromBundle(bundle);
    remoteJGitFacade.ffMerge("refs/heads/bundle");

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDir, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDir, "refs/heads/bundle"));

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDir, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDir, "HEAD"));
  }
}