package de.fu_berlin.inf.dpp.git;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FacadeJGitTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  // the local user is creating the bundle
  private File localWorkDir;

  @Before
  public void SetUp() {
    try {
      localWorkDir = tempFolder.newFolder("TempDir1");
      JGitFacade.initNewRepo(localWorkDir);
      JGitFacade.writeCommitToRepo(localWorkDir, 2);
    } catch (IOException e) {
      fail("IO");
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
  public void testValidUnbundle() {}

  @Test
  public void testEmptyfileUnbundle() {}
}
