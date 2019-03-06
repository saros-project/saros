package saros.git;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;

public class JGitFacadeTest extends JGitTestCase {

  @Override
  @Before
  public void setUp() throws IOException, IllegalStateException, GitAPIException {
    super.setUp();
  }
  // happy paths
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

    assertNotEqualRevisionStrings("HEAD", "FETCH_HEAD");

    remoteJGitFacade.fetchFromBundle(bundle);

    assertEqualRevisionStrings("HEAD", "FETCH_HEAD");
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
}
