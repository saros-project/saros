package de.fu_berlin.inf.dpp.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FacadeJGitTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  // the local user is creating the bundle
  private File localWorkDir;
  // the remote user is receiving the bundle
  private File remoteWorkDir;

  @Before
  public void SetUp() {
    try {
      localWorkDir = tempFolder.newFolder("TempDir1");
      JGitFacade.initNewRepo(localWorkDir);
      JGitFacade.writeCommitToRepo(localWorkDir, 2);
      remoteWorkDir = tempFolder.newFolder("TempDir2");
      JGitFacade.clone(localWorkDir, remoteWorkDir);
      JGitFacade.writeCommitToRepo(localWorkDir, 3);

    } catch (IOException e) {
      fail("IO");
    } catch (InvalidRemoteException e) {
      fail("GitRepo1");
    } catch (TransportException e) {
      fail("Transport");
    } catch (GitAPIException e) {
      fail("Other");
    }
  }

  @Test
  public void testNullBundle() {}

  @Test
  public void testValidBundle() {
    try {
      File bundle = JGitFacade.createBundleByTag(localWorkDir, "CheckoutAtInit");
      assertNotNull(bundle);
    } catch (IOException e) {
      fail("IO");
    }
  }

  @Test
  public void testValidUnbundle() {
    try {
      File bundle = JGitFacade.createBundleByTag(localWorkDir, "CheckoutAt2");
      JGitFacade.unbundle(bundle, remoteWorkDir);

      assertEquals(
          Git.open(localWorkDir).getRepository().resolve("HEAD"),
          Git.open(remoteWorkDir).getRepository().resolve("FETCH_HEAD"));
    } catch (IOException e) {
      fail("IO");
    } catch (InvalidRemoteException e) {
      fail("GitRepo1");
    } catch (TransportException e) {
      fail("Transport");
    } catch (GitAPIException e) {
      fail("Other");
    }
  }

  @Test
  public void testEmptyfileUnbundle() {}
}
