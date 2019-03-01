package saros.stf.test.session;

import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;

public class OverlappingSharingTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Before
  public void beforeEveryTest() throws Exception {

    clearWorkspaces();
  }

  @After
  public void afterEveryTest() throws Exception {
    leaveSessionHostFirst(ALICE);
  }

  /**
   * ALICE and BOB have the same project with at least two files ("A" and "B").
   *
   * <p>1. ALICE shares the file A with BOB
   *
   * <p>2. BOB choose the existing project for file A
   *
   * <p>3. BOB adds file B to the session.
   *
   * <p>4. ALICE choose the existing project for file B
   *
   * <p>result: ALICE and BOB have the shared files A and B in the session
   */
  @Test
  @Ignore("Non-Host adding projects is currently deactivated")
  public void testOverlappingSharingInExistingProject() throws Exception {

    ALICE.superBot().internal().createProject("foo");
    ALICE.superBot().internal().createFile("foo", "A.txt", "AAA");
    ALICE.superBot().internal().createFile("foo", "B.txt", "BBB");
    ALICE.superBot().internal().createFile("foo", "C.txt", "BBB");

    BOB.superBot().internal().createProject("foo");
    BOB.superBot().internal().createFile("foo", "A.txt", "AAA");
    BOB.superBot().internal().createFile("foo", "B.txt", "BBB");
    BOB.superBot().internal().createFile("foo", "C.txt", "BBB");

    Util.buildFileSessionConcurrently(
        "foo", new String[] {"A.txt"}, TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/A.txt");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectFile("foo", "B.txt")
        .shareWith()
        .addToSarosSession();

    ALICE.superBot().confirmShellAddProjectUsingExistProject("foo");

    ALICE.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/B.txt");

    assertTrue(ALICE.superBot().views().packageExplorerView().isResourceShared("foo/A.txt"));
  }
}
