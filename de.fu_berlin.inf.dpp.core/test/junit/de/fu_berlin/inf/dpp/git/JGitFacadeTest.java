package de.fu_berlin.inf.dpp.git;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;<<<<<<<HEAD=======
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;import org.junit.Before;>>>>>>>split production and test code,removed not needed methods,refactoring methods signature,undo.gitignore,Add Testsuite
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JGitFacadeTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    // the local user is creating the bundle
    private File localWorkDir;

    @Before
    public void setUp() {
        try {
            localWorkDir = tempFolder.newFolder("TempDir1");
            initNewRepo(localWorkDir);
            writeCommitToRepo(localWorkDir, 2);
        } catch (IOException e) {
            fail("IO");
        } catch (GitAPIException e) {
            fail("Other");
        }
    }

    @Test
    public void testWrongBundle() {
        try {
            JGitFacade.createBundle(localWorkDir, "refs/heads/master",
                "WrongBasis");
            fail("Basis isn't existing");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Expected IllegalArgumentException but thrown" + e);
        }
        try {
            JGitFacade.createBundle(localWorkDir, "refs/heads/wrongRef",
                "CheckoutAtInit");
            fail("Ref isn't existing");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Expected IllegalArgumentException but thrown" + e);
        }
        try {
            File emptyDir = tempFolder.newFolder("TempDir3");
            JGitFacade.createBundle(emptyDir, "refs/heads/master",
                "CheckoutAtInit");
            fail("Dir is empty");
        } catch (IOException e) {

        }
    }

  @Test
  public void testValidBundle() {
    try {
      File tempSubfolder = tempFolder.newFolder("tempFolder" + iD);
      File tempFile = tempSubfolder.createTempFile("tempFile", ".txt");
      JGitFacade.initNewRepo(tempFile);
      for (int i = 2; i <= amountCommits; i++) {
        JGitFacade.writeCommitToRepo(tempFile, i);
      }
      return tempFile;
    } catch (IOException e) {

    @Test
    public void testValidUnbundle() {
        try {

            File remoteWorkDir = tempFolder.newFolder("TempDir2");
            JGitFacade.cloneFromRepo(localWorkDir, remoteWorkDir);
            writeCommitToRepo(localWorkDir, 3);
            File bundle = JGitFacade.createBundle(localWorkDir,
                "refs/heads/master", "CheckoutAtCommit2");
            assertNotEquals(getObjectIdByRevisionString(localWorkDir, "HEAD"),
                getObjectIdByRevisionString(remoteWorkDir, "FETCH_HEAD"));
            JGitFacade.fetchFromBundle(bundle, remoteWorkDir);
            assertEquals(getObjectIdByRevisionString(localWorkDir, "HEAD"),
                getObjectIdByRevisionString(remoteWorkDir, "FETCH_HEAD"));
        } catch (IOException e) {
            fail("IO");
        } catch (GitAPIException e) {
            fail("Git");
        }
        return null;
    }

    @Test
    public void testCreateBundle() throws IOException {

        File firstRepoTempFile = createTestFile(2, 1);
        File bundle = JGitFacade.createBundleByTag(firstRepoTempFile,
            "CheckoutAtInit");
        assertNotNull(bundle);
    }

  @Test
  public void testCreateBundleRaisesNullPointerExeption() throws IOException {
    try {
      File bundle2 = JGitFacade.createBundleByTag(null, "CheckoutAtInit");
      fail("created Bundle with null File");
    } catch (NullPointerException e) {
    }

    public void testWrongUnbundle() {
        // TO-DO: Analyse endless loop
        // try {
        // testFile = tempFolder.newFile();
        // JGitFacade.fetchFromBundle(testFile, remoteWorkDir);
        // fail("fetch from empty file");
        // } catch (IOException | GitAPIException e) {
        //
        // }
        try {
            File emptyDir = tempFolder.newFolder("TempDir3");
            writeCommitToRepo(localWorkDir, 3);
            File bundle = JGitFacade.createBundle(localWorkDir,
                "refs/heads/master", "CheckoutAtCommit2");
            JGitFacade.fetchFromBundle(bundle, emptyDir);
            fail("fetch into emptyDir");
        } catch (IOException | GitAPIException e) {

        }
        try {
            File remoteWorkDir = tempFolder.newFolder("TempDir2");
            JGitFacade.cloneFromRepo(localWorkDir, remoteWorkDir);
            writeCommitToRepo(localWorkDir, 3);
            writeCommitToRepo(localWorkDir, 4);
            File bundle = JGitFacade.createBundle(localWorkDir,
                "refs/heads/master", "CheckoutAtCommit3");
            JGitFacade.fetchFromBundle(bundle, remoteWorkDir);
            fail("fetch but remote havn't the basis");
        } catch (IOException | GitAPIException e) {

        }
    }

    /**
     * Creating a new Git directory,create a file and git add the file,create
     * the first commit and create the Tag "CheckoutAtInit" that is pointing to
     * the commit
     *
     * @param workDir
     *            The directory that will contain the .git directory
     * @throws GitAPIException
     * @throws IllegalStateException
     * @throws IOException
     */
    private static void initNewRepo(File workDir)
        throws IllegalStateException, GitAPIException, IOException {
        Git git;
        git = Git.init().setDirectory(workDir).call();
        File testFile1 = new File(workDir, "testFile1");
        testFile1.createNewFile();
        git.add().addFilepattern(workDir.getAbsolutePath()).call();
        git.commit().setMessage("Initial commit").call();
        git.tag().setName("CheckoutAtInit").setAnnotated(false)
            .setForceUpdate(true).call();
    }

    /**
     * Creating a new File, git add, git commit and create a Tag
     * "CheckoutAtCommit(numberOfCommit)"
     *
     * @param workDir
     *            The directory that contains the .git directory
     * @param numberOfCommit
     *            The first used number should be 2 and than incremented by 1
     * @throws IOException
     * @throws GitAPIException
     * @throws NoFilepatternException
     */
    private static void writeCommitToRepo(File workDir, int numberOfCommit)
        throws IOException, NoFilepatternException, GitAPIException {

        Git git = Git.open(workDir);
        File testfile = new File(workDir, "testfile" + numberOfCommit);
        git.add().addFilepattern(workDir.getAbsolutePath()).call(); // git add .
        git.commit().setMessage("new file Commit Nr." + numberOfCommit).call();
        git.tag().setName("CheckoutAtCommit" + numberOfCommit)
            .setAnnotated(false).setForceUpdate(true).call();
    }

    private static ObjectId getObjectIdByRevisionString(File workDir,
        String rev) throws RevisionSyntaxException, AmbiguousObjectException,
        IncorrectObjectTypeException, IOException {
        return Git.open(workDir).getRepository().resolve(rev);
    }
}
