package de.fu_berlin.inf.dpp.stf.test.stf.view.sarosview.content;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS_ID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import java.rmi.RemoteException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SessionAliceBobTest extends StfTestCase {

  @BeforeClass
  public static void runBeforeClass() throws Exception {
    initTesters(ALICE, BOB);
    setUpWorkbench();
    setUpSaros();
    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);
  }

  @Before
  public void beforeEveryTest() throws Exception {
    Util.reBuildSession(Constants.PROJECT1, ALICE, BOB);
    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared(Constants.PROJECT1);
    Util.grantWriteAccess(ALICE, BOB);
    Util.stopFollowModeSequentially(ALICE, BOB);
  }

  @Test
  public void testSetFocusOnSarosView() throws RemoteException {
    ALICE.remoteBot().view(VIEW_SAROS).show();
    assertTrue(ALICE.remoteBot().view(VIEW_SAROS).isActive());

    ALICE.remoteBot().view(VIEW_SAROS).close();
    assertFalse(ALICE.remoteBot().isViewOpen(VIEW_SAROS));

    ALICE.remoteBot().openViewById(VIEW_SAROS_ID);
    assertTrue(ALICE.remoteBot().isViewOpen(VIEW_SAROS));
  }

  @Test
  public void testRestrictToReadOnlyAccess() throws RemoteException {
    assertTrue(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasWriteAccess());
    assertFalse(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasReadOnlyAccess());
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).restrictToReadOnlyAccess();
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).waitUntilHasReadOnlyAccess();
    assertFalse(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasWriteAccess());
    assertTrue(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasReadOnlyAccess());
  }

  @Test
  public void testGrantWriteAccess() throws RemoteException {
    assertTrue(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasWriteAccess());
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).restrictToReadOnlyAccess();
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).waitUntilHasReadOnlyAccess();
    assertFalse(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasWriteAccess());
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).grantWriteAccess();
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).waitUntilHasWriteAccess();
    assertFalse(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).hasReadOnlyAccess());
  }

  @Test
  public void testFollowMode() throws Exception {
    assertFalse(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).isFollowing());
    assertFalse(BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).isFollowing());

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    // wait for the activity to reach BOB
    Thread.sleep(2000);

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();

    assertTrue(BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).isFollowing());

    BOB.remoteBot().waitUntilEditorOpen(Constants.CLS1_SUFFIX);
    assertTrue(BOB.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).followParticipant();

    assertTrue(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).isFollowing());

    BOB.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA).closeWithSave();
    ALICE.remoteBot().waitUntilEditorClosed(Constants.CLS1_SUFFIX);
    assertFalse(ALICE.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));
  }

  @Test
  public void testStopFollowing() throws Exception {
    assertFalse(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).isFollowing());
    assertFalse(BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).isFollowing());

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    // wait for the activity to reach BOB
    Thread.sleep(2000);

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();
    assertTrue(BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).isFollowing());

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).stopFollowing();
    assertFalse(BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).isFollowing());
  }

  /**
   * FIXME if this test would be performed more than one time on the same saros-instance, you may
   * get the TimeoutException.
   *
   * <p>under mac_os it work well, when the view window isn't too small.
   *
   * @throws RemoteException
   */
  @Test
  public void jumpToSelectedParticipant() throws Exception {

    BOB.remoteBot().closeAllEditors();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);

    ALICE.remoteBot().waitUntilEditorOpen(Constants.CLS2_SUFFIX);

    assertTrue(ALICE.remoteBot().isEditorOpen(Constants.CLS2_SUFFIX));

    assertFalse(BOB.remoteBot().isEditorOpen(Constants.CLS2_SUFFIX));

    BOB.superBot()
        .views()
        .sarosView()
        .selectUser(ALICE.getJID())
        .jumpToPositionOfSelectedParticipant();

    Thread.sleep(500);

    assertTrue(BOB.remoteBot().editor(Constants.CLS2_SUFFIX).isActive());

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).show();

    assertTrue(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());

    assertFalse(BOB.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));

    Thread.sleep(500);

    BOB.superBot()
        .views()
        .sarosView()
        .selectUser(ALICE.getJID())
        .jumpToPositionOfSelectedParticipant();

    assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
  }

  @Test
  public void inconsistencyDetected() throws Exception {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();
    String editorTextOfAlice = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText();
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).restrictToReadOnlyAccess();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();
    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).setTextFromFile(Constants.CP1);
    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).save();
    String editorTextOfBob = BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText();
    assertFalse(editorTextOfAlice.equals(editorTextOfBob));

    Thread.sleep(10000);

    BOB.superBot().views().sarosView().waitUntilIsInconsistencyDetected();
    BOB.superBot().views().sarosView().resolveInconsistency();
    editorTextOfAlice = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText();
    editorTextOfBob = BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText();
    assertTrue(editorTextOfAlice.equals(editorTextOfBob));
  }

  /**
   * ALICE(host) first leave the session then BOB confirm the windonws "Closing the Session".
   *
   * @throws RemoteException
   * @throws InterruptedException
   */
  @Test
  public void leaveSessionProcessDonebyAllUsersWithHostFirstLeave() throws Exception {
    assertTrue(ALICE.superBot().views().sarosView().isInSession());
    assertTrue(BOB.superBot().views().sarosView().isInSession());
    leaveSessionHostFirst(ALICE);
    assertFalse(ALICE.superBot().views().sarosView().isInSession());
    assertFalse(BOB.superBot().views().sarosView().isInSession());
    Thread.sleep(10000);
  }

  /**
   * peer(BOB) first leave the session then host(ALICE) leave.
   *
   * @throws RemoteException
   * @throws InterruptedException
   */
  @Test
  public void leaveSessionProcessDonebyAllUsersWithPeersFirstLeave() throws Exception {
    assertTrue(ALICE.superBot().views().sarosView().isInSession());
    assertTrue(BOB.superBot().views().sarosView().isInSession());
    leaveSessionPeersFirst(ALICE);
    assertFalse(ALICE.superBot().views().sarosView().isInSession());
    assertFalse(BOB.superBot().views().sarosView().isInSession());
  }

  @Test
  public void testIsInSession() throws Exception {
    assertTrue(ALICE.superBot().views().sarosView().isInSession());
    assertTrue(BOB.superBot().views().sarosView().isInSession());
    leaveSessionHostFirst(ALICE);
    assertFalse(ALICE.superBot().views().sarosView().isInSession());
    assertFalse(BOB.superBot().views().sarosView().isInSession());
  }

  @Test
  public void addProjects() throws Exception {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT2);
    ALICE.superBot().views().sarosView().selectSession().addProjects(Constants.PROJECT2);
    BOB.superBot()
        .confirmShellAddProjectUsingWhichProject(
            Constants.PROJECT2, TypeOfCreateProject.NEW_PROJECT);

    // TODO remove this, we have to wait for project arrival
    // or next test cases will fail because of an uncloseable window
    Thread.sleep(10000);
  }
}
