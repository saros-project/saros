package de.fu_berlin.inf.dpp.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class jgittest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  File GitDir1;
  File GitDir2;

  @Before
  public void setUP() {
    try {
      tempFolder.delete();
      File remote = tempFolder.newFolder("Gitdir1");
      File local = tempFolder.newFolder("Gitdir2");
      JGitFacade.initNewRepo(remote);
      CloneCommand cloneCommand = Git.cloneRepository();
      cloneCommand.setDirectory(local);
      cloneCommand.setURI(JGitFacade.getUrlByGitRepo(remote));
      cloneCommand.call();
      JGitFacade.writeCommitToRepo(remote, 2);

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
  public void testFileNoBundle() {
    try {
      File emptyFile = new File("");
      emptyFile.createTempFile("emptyFile", "");
      JGitFacade.unbundle(emptyFile, GitDir2);
      fail("Wrong File accepted");
    } catch (Exception e) {

    }
  }

  @Test
  public void testFilebundle() {

    File bundle;
    try {
      bundle = JGitFacade.createBundleByTag(GitDir1, "CheckoutAtInit");
      ObjectId oldId = Git.open(GitDir2).getRepository().resolve("HEAD");
      assertEquals(oldId, Git.open(GitDir2).getRepository().resolve("HEAD"));
      JGitFacade.unbundle(bundle, GitDir2);
      assertNotEquals(oldId, Git.open(GitDir2).getRepository().resolve("HEAD"));
    } catch (IOException e) {
      fail("IO");
    } catch (Exception e) {
      fail("Not IO");
    }
  }
}
