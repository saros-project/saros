package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AwarenessInformationVisibleAfterInvitationTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Before
  public void setUp() throws Exception {
    clearWorkspaces();
  }

  @After
  public void leaveSession() throws Exception {
    leaveSessionHostFirst(ALICE);
  }

  /**
   * Tests that an invited client can see the awareness information after the projectNegotiation
   * finished for new Sessions
   *
   * <p>Steps:
   *
   * <p>1. Alice creates local Project with file1 and opens file1
   *
   * <p>2. Alice invites Bob
   *
   * <p>3. Bob checks if he can see which project Alice has opened
   */
  @Test
  public void awarenessInformationVisibleNewSessionTest() throws Exception {
    ALICE.superBot().internal().createJavaProject(Constants.PROJECT1);
    ALICE.superBot().internal().createFile(Constants.PROJECT1, Constants.FILE1, "");

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile(Constants.PROJECT1, Constants.FILE1)
        .open();
    Util.buildSessionConcurrently(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared(Constants.PROJECT1 + "/" + Constants.FILE1);

    // Wait for awareness information to arrive
    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    String expectedOpenProject = Constants.PROJECT1 + ": " + Constants.FILE1;

    String projectOpenAtAlice =
        BOB.remoteBot()
            .view("Saros")
            .bot()
            .tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(ALICE.getName()) + ".*")
            .getNode(0)
            .getText();

    assertEquals(
        "Bob sees the wrong file as open by Alice or none at all.",
        expectedOpenProject,
        projectOpenAtAlice);
  }

  /**
   * Tests that a client invited to a running session can see the awareness information after the
   * projectNegotiation has finished
   *
   * <p>Steps:
   *
   * <p>1. Alice creates local Project with file1 and file2 and opens file1
   *
   * <p>2. Alice invites Bob
   *
   * <p>3. Bob opens file2
   *
   * <p>4. Alice adds Carl to the session
   *
   * <p>5. Carl checks if he can see which files Alice and Bob have opened
   */
  @Test
  public void awarenessInformationVisableAddToSessionTest() throws Exception {

    ALICE.superBot().internal().createJavaProject(Constants.PROJECT1);
    ALICE.superBot().internal().createFile(Constants.PROJECT1, Constants.FILE1, "");
    ALICE.superBot().internal().createFile(Constants.PROJECT1, Constants.FILE2, "");

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile(Constants.PROJECT1, Constants.FILE1)
        .open();
    Util.buildSessionConcurrently(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared(Constants.PROJECT1 + "/" + Constants.FILE2);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectFile(Constants.PROJECT1, Constants.FILE2)
        .open();

    ALICE.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    CARL.superBot().confirmShellAddProjectWithNewProject(Constants.PROJECT1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared(Constants.PROJECT1 + "/" + Constants.FILE2);

    // Wait for awareness information from both participants to arrive
    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    String expectedOpenProjectAlice = Constants.PROJECT1 + ": " + Constants.FILE1;
    String expectedOpenProjectBob = Constants.PROJECT1 + ": " + Constants.FILE2;

    String projectOpenAtAlice =
        CARL.remoteBot()
            .view("Saros")
            .bot()
            .tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(ALICE.getName()) + ".*")
            .getNode(0)
            .getText();
    String projectOpenAtBob =
        CARL.remoteBot()
            .view("Saros")
            .bot()
            .tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(BOB.getName()) + ".*")
            .getNode(0)
            .getText();

    assertEquals(
        "Carl sees the wrong file as open by Alice or none at all.",
        expectedOpenProjectAlice,
        projectOpenAtAlice);

    assertEquals(
        "Carl sees the wrong file as open by Bob or none at all.",
        expectedOpenProjectBob,
        projectOpenAtBob);
  }
}
