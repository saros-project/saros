package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestSessionView extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        reBuildSession(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetWriteAccess(alice, bob);
        resetFollowModeSequentially(alice, bob);
    }

    @Test
    public void testSetFocusOnSessionView() throws RemoteException {
        log.trace("alice set focus on session view.");
        alice.bot().view(VIEW_SAROS_SESSION).show();
        assertTrue(alice.bot().view(VIEW_SAROS_SESSION).isActive());
        log.trace("alice close session view.");

        alice.bot().view(VIEW_SAROS_SESSION).close();
        assertFalse(alice.bot().isViewOpen(VIEW_SAROS_SESSION));
        log.trace("alice open session view again");
        alice.bot().openViewById(VIEW_SAROS_SESSION_ID);
        assertTrue(alice.bot().isViewOpen(VIEW_SAROS_SESSION));
        log.trace("alice focus on saros buddies view.");
        alice.bot().view(VIEW_SAROS_BUDDIES).show();
        assertFalse(alice.bot().view(VIEW_SAROS_SESSION).isActive());
        log.trace("testSetFocusOnSessionView is done.");
    }

    @Test
    public void testRestrictToReadOnlyAccess() throws RemoteException {
        assertTrue(alice.sarosBot().sessionView().hasWriteAccessBy(bob.jid));
        assertTrue(bob.sarosBot().sessionView().hasWriteAccess());

        assertFalse(alice.sarosBot().sessionView().hasReadOnlyAccessBy(bob.jid));
        assertFalse(bob.sarosBot().sessionView().hasReadOnlyAccess());

        alice.sarosBot().sessionView().restrictToReadOnlyAccess(bob.jid);

        assertFalse(alice.sarosBot().sessionView().hasWriteAccessBy(bob.jid));
        assertFalse(bob.sarosBot().sessionView().hasWriteAccess());

        assertTrue(alice.sarosBot().sessionView().hasReadOnlyAccessBy(bob.jid));
        assertTrue(bob.sarosBot().sessionView().hasReadOnlyAccess());
    }

    @Test
    public void testGrantWriteAccess() throws RemoteException {
        assertTrue(alice.sarosBot().sessionView().hasWriteAccessBy(bob.jid));
        assertTrue(bob.sarosBot().sessionView().hasWriteAccess());

        alice.sarosBot().sessionView().restrictToReadOnlyAccess(bob.jid);

        assertFalse(bob.sarosBot().sessionView().hasWriteAccess());
        assertFalse(alice.sarosBot().sessionView().hasWriteAccessBy(bob.jid));

        alice.sarosBot().sessionView().grantWriteAccess(bob.jid);

        assertTrue(bob.sarosBot().sessionView().hasWriteAccess());
        assertTrue(alice.sarosBot().sessionView().hasWriteAccessBy(bob.jid));

        assertFalse(alice.sarosBot().sessionView().hasReadOnlyAccessBy(bob.jid));
        assertFalse(bob.sarosBot().sessionView().hasReadOnlyAccess());

    }

    @Test
    public void testFollowMode() throws RemoteException {
        assertFalse(alice.sarosBot().sessionView().isFollowingBuddy(bob.jid));
        assertFalse(bob.sarosBot().sessionView().isFollowingBuddy(alice.jid));

        bob.sarosBot().sessionView().followThisBuddy(alice.jid);
        assertTrue(bob.sarosBot().sessionView().isFollowingBuddy(alice.jid));

        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();

        bob.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        assertTrue(bob.bot().isEditorOpen(CLS1_SUFFIX));

        alice.sarosBot().sessionView().followThisBuddy(bob.jid);
        assertTrue(alice.sarosBot().sessionView().isFollowingBuddy(bob.jid));

        bob.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        alice.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(alice.bot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void testStopFollowing() throws RemoteException {
        assertFalse(alice.sarosBot().sessionView().isFollowingBuddy(bob.jid));
        assertFalse(bob.sarosBot().sessionView().isFollowingBuddy(alice.jid));

        bob.sarosBot().sessionView().followThisBuddy(alice.jid);
        assertTrue(bob.sarosBot().sessionView().isFollowingBuddy(alice.jid));

        bob.sarosBot().sessionView().stopFollowingThisBuddy(alice.jid);
        assertFalse(bob.sarosBot().sessionView().isFollowingBuddy(alice.jid));

    }

    /**
     * FIXME if this test would be performed more than one time on the same
     * saros-instance, you may get the TimeoutException.
     * 
     * under mac_os it work well, when the view window isn't too small.
     * 
     * 
     * @throws RemoteException
     */
    @Test
    public void jumpToSelectedBuddy() throws RemoteException {
        alice.sarosBot().file().newClass(PROJECT1, PKG1, CLS2);
        alice.bot().waitUntilEditorOpen(CLS2_SUFFIX);
        assertTrue(alice.bot().isEditorOpen(CLS2_SUFFIX));
        assertFalse(bob.bot().isEditorOpen(CLS2_SUFFIX));
        bob.bot().captureScreenshot(
            bob.bot().getPathToScreenShot() + "/vor_jump_to_position.png");
        bob.sarosBot().sessionView().jumpToPositionOfSelectedBuddy(alice.jid);
        bob.bot().captureScreenshot(
            bob.bot().getPathToScreenShot() + "/after_jump_to_position.png");
        assertTrue(bob.bot().editor(CLS2_SUFFIX).isActive());

        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        alice.bot().editor(CLS1 + SUFFIX_JAVA).show();
        assertTrue(alice.bot().editor(CLS1_SUFFIX).isActive());
        assertFalse(bob.bot().isEditorOpen(CLS1_SUFFIX));
        bob.sarosBot().sessionView().jumpToPositionOfSelectedBuddy(alice.jid);
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());
    }

    @Test
    @Ignore
    // FIXME dialog with error message "Xuggler not installed"
    public void sharedYourScreenWithSelectedUserGUI() throws RemoteException {
        // alice.mainMenu.setupSettingForScreensharing(1, 0, -1, -1);
        shareYourScreen(alice, bob);
        bob.sarosBot().remoteScreenView().waitUntilRemoteScreenViewIsActive();
        assertTrue(bob.bot().view(VIEW_REMOTE_SCREEN).isActive());
        alice.sarosBot().sessionView().stopSessionWithBuddy(bob.jid);
    }

    @Test
    public void inconsistencyDetectedGUI() throws RemoteException {
        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        String editorTextOfAlice = alice.bot().editor(CLS1_SUFFIX).getText();
        alice.sarosBot().sessionView().restrictToReadOnlyAccess(bob.jid);
        bob.sarosBot().packageExplorerView().selectClass(PROJECT1, PKG1, CLS1)
            .open();
        bob.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String editorTextOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertFalse(editorTextOfAlice.equals(editorTextOfBob));
        bob.sarosBot().sessionView().waitUntilIsInconsistencyDetected();
        bob.sarosBot().sessionView().inconsistencyDetected();
        editorTextOfAlice = alice.bot().editor(CLS1_SUFFIX).getText();
        editorTextOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertTrue(editorTextOfAlice.equals(editorTextOfBob));
    }

    @Test
    public void testRestrictInviteesToReadOnlyAccessGUI()
        throws RemoteException {
        assertTrue(alice.sarosBot().sessionView().isHost());
        assertTrue(alice.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)
            .isEnabled());
        assertFalse(bob.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)
            .isEnabled());

        assertTrue(alice.sarosBot().sessionView().hasWriteAccess());
        assertTrue(bob.sarosBot().sessionView().hasWriteAccess());

        alice.sarosBot().sessionView().restrictInviteesToReadOnlyAccess();
        assertFalse(bob.sarosBot().sessionView().hasWriteAccess());
        assertTrue(alice.sarosBot().sessionView().hasWriteAccess());

    }

    /**
     * alice(host) first leave the session then bob confirm the windonws
     * "Closing the Session".
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    public void leaveSessionProcessDonebyAllUsersWithHostFirstLeave()
        throws RemoteException, InterruptedException {
        assertTrue(alice.sarosBot().sessionView().isInSession());
        assertTrue(bob.sarosBot().sessionView().isInSession());
        leaveSessionHostFirst();
        assertFalse(alice.sarosBot().sessionView().isInSession());
        assertFalse(bob.sarosBot().sessionView().isInSession());
    }

    /**
     * peer(bob) first leave the session then host(alice) leave.
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    public void leaveSessionProcessDonebyAllUsersWithPeersFirstLeave()
        throws RemoteException, InterruptedException {
        assertFalse(alice.sarosBot().sessionView().existsLabelInSessionView());
        assertFalse(bob.sarosBot().sessionView().existsLabelInSessionView());
        assertTrue(alice.sarosBot().sessionView().isInSession());
        assertTrue(bob.sarosBot().sessionView().isInSession());
        leaveSessionPeersFirst();
        assertFalse(alice.sarosBot().sessionView().isInSession());
        assertFalse(bob.sarosBot().sessionView().isInSession());
        assertTrue(alice.sarosBot().sessionView().existsLabelInSessionView());
        assertTrue(bob.sarosBot().sessionView().existsLabelInSessionView());
    }

    @Test
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sarosBot().sessionView().isInSessionNoGUI());
        assertTrue(alice.sarosBot().sessionView().isInSession());
        assertTrue(bob.sarosBot().sessionView().isInSessionNoGUI());
        assertTrue(bob.sarosBot().sessionView().isInSession());
        leaveSessionHostFirst();
        assertFalse(alice.sarosBot().sessionView().isInSessionNoGUI());
        assertFalse(bob.sarosBot().sessionView().isInSessionNoGUI());
        assertFalse(alice.sarosBot().sessionView().isInSession());
        assertFalse(bob.sarosBot().sessionView().isInSession());
    }

    @Test
    public void inviteUsersInSession() throws RemoteException,
        InterruptedException {
        bob.sarosBot().sessionView().leaveTheSessionByPeer();
        bob.noBot().deleteProjectNoGUI(PROJECT1);
        assertFalse(bob.sarosBot().sessionView().isInSession());
        inviteBuddies(PROJECT1, TypeOfCreateProject.NEW_PROJECT, alice, bob,
            carl);
        assertTrue(carl.sarosBot().sessionView().isInSession());
        assertTrue(bob.sarosBot().sessionView().isInSession());
    }

}
