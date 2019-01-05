package de.fu_berlin.inf.dpp.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JGitFacadeTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  // the local user is creating the bundle
  private File localWorkDir;
  // the remote user is receiving the bundle
  private File remoteWorkDir;
  // empty directory
  private File emptyDir;
  // testfile
  private File testFile;

  @Before
  public void SetUp() {
    try {
      localWorkDir = tempFolder.newFolder("TempDir1");
      JGitFacade.initNewRepo(localWorkDir);
      JGitFacade.writeCommitToRepo(localWorkDir, 2);
      remoteWorkDir = tempFolder.newFolder("TempDir2");
      JGitFacade.cloneFromRepo(localWorkDir, remoteWorkDir);
      JGitFacade.writeCommitToRepo(localWorkDir, 3);
      JGitFacade.writeCommitToRepo(localWorkDir, 4);
      emptyDir = tempFolder.newFolder("TempDir3");
      testFile = tempFolder.newFile();

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
  public void testWrongBundle() {
    try {
      File bundle = JGitFacade.createBundle(localWorkDir, "refs/heads/master", "WrongTag");
      fail("Tag is not existing");
    } catch (IOException e) {

    }
    try {
      File bundle = JGitFacade.createBundle(localWorkDir, "refs/heads/wrongRef", "CheckoutAtInit");
      fail("Ref is not existing");
    } catch (IOException e) {

    }
    try {
      File bundle = JGitFacade.createBundle(emptyDir, "refs/heads/master", "CheckoutAtInit");
      fail("Dir is empty");
    } catch (IOException e) {

    }
  }

  @Test
  public void testValidBundle() {
    try {
      File bundle = JGitFacade.createBundle(localWorkDir, "refs/heads/master", "");
      assertNotNull(bundle);
    } catch (IOException e) {
      fail("IO");
    }
  }

  @Test
  public void testValidUnbundle() {
    try {

      File bundle = JGitFacade.createBundle(localWorkDir, "refs/heads/master", "CheckoutAtCommit2");
      assertNotEquals(
          JGitFacade.getObjectIDbyRevisionString(localWorkDir, "HEAD"),
          JGitFacade.getObjectIDbyRevisionString(remoteWorkDir, "FETCH_HEAD"));
      JGitFacade.fetchFromBundle(bundle, remoteWorkDir);
      assertEquals(
          JGitFacade.getObjectIDbyRevisionString(localWorkDir, "HEAD"),
          JGitFacade.getObjectIDbyRevisionString(remoteWorkDir, "FETCH_HEAD"));
    } catch (IOException e) {
      fail("IO");
    } catch (GitAPIException e) {
      fail("Git");
    }
  }

  @Test
  public void testWrongUnbundle() {
    //      TO-DO: Analyse endless loop
    //     try {
    //      JGitFacade.fetchFromBundle(testFile, remoteWorkDir);
    //      fail("fetch from empty file");
    //    } catch (IOException | GitAPIException e) {
    //
    //    }
    try {
      File bundle = JGitFacade.createBundle(localWorkDir, "refs/heads/master", "CheckoutAtCommit2");
      JGitFacade.fetchFromBundle(bundle, emptyDir);
      fail("fetch into emptyDir");
    } catch (IOException | GitAPIException e) {

    }
    try {
      File bundle = JGitFacade.createBundle(localWorkDir, "refs/heads/master", "CheckoutAtCommit3");
      JGitFacade.fetchFromBundle(bundle, remoteWorkDir);
      fail("fetch but remote havn't the basis");
    } catch (IOException | GitAPIException e) {

    }
  }

  /* Tests for working with manual created TestDirectorys
   * To run this Test
   *  write "private File selfMadeWorkDir;" as a local variable
   *  write "selfMadeWorkDir = new File("TestWorkDirGit");" in the SetUp()
   *  create a new directory in the root of the core project with the name "TestWorkDirGit"
   *  init git repo with cl
   *  create a empty file helloworld.txt
   *  git add .
   *  git commit -m "initCommit"
   *
  @Test
  public void testValidBundleSelfMade() {
    try {
      File bundle = JGitFacade.createBundle(selfMadeWorkDir,"refs/heads/master", "");
      assertNotNull(bundle);
    } catch (IOException e) {
      fail("IO");
    }
  }

  @Test
  public void preTestValidBundleSelfMade() {
    try {
      Git myRepo = Git.open(selfMadeWorkDir);
    } catch (IOException e) {
      fail("IO");
    }
  }
  */
}
