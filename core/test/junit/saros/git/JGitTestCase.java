package saros.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class JGitTestCase {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  // the local user is creating the bundle
  File localWorkDirTree;
  JGitFacade localJGitFacade;

  byte[] bundle;

  // the remote user is receiving the bundle
  File remoteWorkDirTree;
  JGitFacade remoteJGitFacade;

  @Before
  void setUp() throws IOException, IllegalStateException, GitAPIException {
    localWorkDirTree = tempFolder.newFolder("TempDir1");

    initNewRepo(localWorkDirTree);
    writeCommitToRepo(localWorkDirTree, 2);

    remoteWorkDirTree = tempFolder.newFolder("TempDir2");

    cloneFromRepo(localWorkDirTree, remoteWorkDirTree);

    writeCommitToRepo(localWorkDirTree, 3);

    // All the set up before can't be done by the user with Saros.
    localJGitFacade = new JGitFacade(localWorkDirTree);
    remoteJGitFacade = new JGitFacade(remoteWorkDirTree);
  }

  /**
   * Creating a new Git directory,create a file and git add the file,create the first commit and
   * create the Tag "CheckoutAtInit" that is pointing to the commit
   *
   * @param workDirTree The directory that will contain the .git directory
   * @throws GitAPIException
   * @throws IllegalStateException
   * @throws IOException
   */
  private static void initNewRepo(File workDirTree)
      throws IllegalStateException, GitAPIException, IOException {
    Git git = Git.init().setDirectory(workDirTree).call();

    File testFile1 = new File(workDirTree, "testFile1");
    if (!testFile1.createNewFile()) {
      throw new IOException("Could not create file" + testFile1);
    }

    git.add().addFilepattern("testFile1").call();

    git.commit().setMessage("Initial commit").call();

    git.tag().setName("CheckoutAtInit").setAnnotated(false).setForceUpdate(true).call();
  }

  /**
   * Creating a new File, git add, git commit and create a Tag "CheckoutAtCommit(numberOfCommit)"
   *
   * @param workDirTree The directory that contains the .git directory
   * @param numberOfCommit The first used number should be 2 and than incremented by 1
   * @throws IOException
   * @throws GitAPIException
   * @throws NoFilepatternException
   */
  static void writeCommitToRepo(File workDirTree, int numberOfCommit)
      throws IOException, NoFilepatternException, GitAPIException {
    Git git = Git.open(workDirTree);

    File testfile = new File(workDirTree, "testfile" + numberOfCommit);
    if (!testfile.createNewFile()) {
      throw new IOException("Could not create file" + testfile);
    }

    git.add().addFilepattern("testfile" + numberOfCommit).call();

    git.commit().setMessage("new file Commit Nr." + numberOfCommit).call();

    git.tag()
        .setName("CheckoutAtCommit" + numberOfCommit)
        .setAnnotated(false)
        .setForceUpdate(true)
        .call();
  }
  /**
   * @param workDirTree The directory that contains the .git directory
   * @param revString See <a href="https://www.git-scm.com/docs/gitrevisions">gitrevisions</a>
   * @return A SHA-1 abstraction
   * @throws RevisionSyntaxException
   * @throws AmbiguousObjectException
   * @throws IncorrectObjectTypeException
   * @throws IOException
   */
  private static ObjectId getObjectIdByRevisionString(File workDirTree, String revString)
      throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
          IOException {
    return Git.open(workDirTree).getRepository().resolve(revString);
  }

  /**
   * @param from The directory that contains the .git directory of where should be cloned from
   * @param to The directory that contains the .git directory of where should be cloned to
   * @throws IllegalArgumentException parameters can't be resolved. Maybe null
   * @throws IOException failed while cloned
   */
  private static void cloneFromRepo(File from, File to)
      throws IllegalArgumentException, IOException {
    if (from == null)
      throw new IllegalArgumentException(
          "workDirTree of where should be cloned from is null and can't be resolved");
    if (to == null)
      throw new IllegalArgumentException(
          "workDirTree of where should be cloned to is null and can't be resolved");
    try {
      Git.cloneRepository().setURI(getUrl(from)).setDirectory(to).call();
    } catch (InvalidRemoteException e) {
      throw new IllegalArgumentException(
          "workDirTree of where should be cloned to is null and can't be resolved", e);
    } catch (GitAPIException | IOException e) {
      throw new IOException("clone failed", e);
    }
  }

  /**
   * @param workDirTree The directory that contains the .git directory
   * @return The URL to access/address directly the .git directory
   * @throws IllegalArgumentException {@code workDirTree} is null can't be resolved
   * @throws IOException failed while read
   */
  private static String getUrl(File workDirTree) throws IllegalArgumentException, IOException {
    return Git.open(workDirTree).getRepository().getDirectory().getCanonicalPath();
  }
  /**
   * Compare hash of the local and the remote repository. Assert that both revision strings leads to
   * the same hash. The revision string is explained here: <a
   * href="https://www.git-scm.com/docs/gitrevisions">gitrevisions</a>
   *
   * @param localRevString
   * @param remoteRevString
   * @throws RevisionSyntaxException
   * @throws AmbiguousObjectException
   * @throws IncorrectObjectTypeException
   * @throws IOException
   */
  void assertEqualRevisionStrings(String localRevString, String remoteRevString)
      throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
          IOException {
    assertEquals(
        getObjectIdByRevisionString(localWorkDirTree, localRevString),
        getObjectIdByRevisionString(remoteWorkDirTree, remoteRevString));
  }
  /**
   * Compare hash of the local and the remote repository. Assert that both revision strings don't
   * lead to the same hash. The vevision string is explained here: <a
   * href="https://www.git-scm.com/docs/gitrevisions">gitrevisions</a>
   *
   * @param localRevString
   * @param remoteRevString
   * @throws RevisionSyntaxException
   * @throws AmbiguousObjectException
   * @throws IncorrectObjectTypeException
   * @throws IOException
   */
  void assertNotEqualRevisionStrings(String localRevString, String remoteRevString)
      throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
          IOException {
    assertNotEquals(
        getObjectIdByRevisionString(localWorkDirTree, localRevString),
        getObjectIdByRevisionString(remoteWorkDirTree, remoteRevString));
  }
}
