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
  private File remoteWorkDir;
  private byte[] bundle;

  @Before
  public void setUp() throws IOException, IllegalStateException, GitAPIException {
    localWorkDir = tempFolder.newFolder("TempDir1");

    JGitFacadeTest.initNewRepo(localWorkDir);
    JGitFacadeTest.writeCommitToRepo(localWorkDir, 2);

    remoteWorkDir = tempFolder.newFolder("TempDir2");

    JGitFacadeTest.cloneFromRepo(localWorkDir, remoteWorkDir);

    JGitFacadeTest.writeCommitToRepo(localWorkDir, 3);

    String basis = JGitFacade.getSHA1HashByRevisionString(remoteWorkDir, "HEAD");

    bundle = JGitFacade.createBundle(localWorkDir, "HEAD", basis);
  }

  @Test
  public void testFetchFromBundleWithMerge()
      throws IOException, NoFilepatternException, GitAPIException, IllegalArgumentException,
          URISyntaxException {

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDir, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDir, "HEAD"));

    JGitFacade.fetchFromBundle(remoteWorkDir, bundle);
    JGitFacade.ffMerge(remoteWorkDir, "refs/heads/bundle");

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDir, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDir, "refs/heads/bundle"));

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(localWorkDir, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(remoteWorkDir, "HEAD"));
  }
}