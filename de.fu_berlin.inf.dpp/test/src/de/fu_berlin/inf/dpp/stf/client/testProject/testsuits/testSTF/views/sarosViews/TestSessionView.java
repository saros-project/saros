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
        assertTrue(alice.sarosBot().state().hasWriteAccessBy(bob.jid));
        assertTrue(bob.sarosBot().state().hasWriteAccess());

        assertFalse(alice.sarosBot().state().hasReadOnlyAccessBy(bob.jid));
        assertFalse(bob.sarosBot().state().hasReadOnlyAccess());

        alice.sarosBot().sessionView().selectBuddy(bob.jid)
            .restrictToReadOnlyAccess();
        bob.sarosBot().condition().waitUntilHasReadOnlyAccess();
        assertFalse(alice.sarosBot().state().hasWriteAccessBy(bob.jid));
        assertFalse(bob.sarosBot().state().hasWriteAccess());

        assertTrue(alice.sarosBot().state().hasReadOnlyAccessBy(bob.jid));
        assertTrue(bob.sarosBot().state().hasReadOnlyAccess());
    }

    @Test
    public void testGrantWriteAccess() throws RemoteException {
        assertTrue(alice.sarosBot().state().hasWriteAccessBy(bob.jid));
        assertTrue(bob.sarosBot().state().hasWriteAccess());

        alice.sarosBot().sessionView().selectBuddy(bob.jid)
            .restrictToReadOnlyAccess();
        bob.sarosBot().condition().waitUntilHasReadOnlyAccess();
        assertFalse(bob.sarosBot().state().hasWriteAccess());
        assertFalse(alice.sarosBot().state().hasWriteAccessBy(bob.jid));

        alice.sarosBot().sessionView().selectBuddy(bob.jid).grantWriteAccess();
        bob.sarosBot().condition().waitUntilHasWriteAccess();
        assertTrue(bob.sarosBot().state().hasWriteAccess());
        assertTrue(alice.sarosBot().state().hasWriteAccessBy(bob.jid));

        assertFalse(alice.sarosBot().state().hasReadOnlyAccessBy(bob.jid));
        assertFalse(bob.sarosBot().state().hasReadOnlyAccess());

    }

    @Test
    public void testFollowMode() throws RemoteException {
        assertFalse(alice.sarosBot().state().isFollowingBuddy(bob.jid));
        assertFalse(bob.sarosBot().state().isFollowingBuddy(alice.jid));

        bob.sarosBot().sessionView().selectBuddy(alice.jid).followThisBuddy();
        assertTrue(bob.sarosBot().state().isFollowingBuddy(alice.jid));

        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();

        bob.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        assertTrue(bob.bot().isEditorOpen(CLS1_SUFFIX));

        alice.sarosBot().sessionView().selectBuddy(bob.jid).followThisBuddy();
        assertTrue(alice.sarosBot().state().isFollowingBuddy(bob.jid));

        bob.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        alice.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(alice.bot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void testStopFollowing() throws RemoteException {
        assertFalse(alice.sarosBot().state().isFollowingBuddy(bob.jid));
        assertFalse(bob.sarosBot().state().isFollowingBuddy(alice.jid));

        bob.sarosBot().sessionView().selectBuddy(alice.jid).followThisBuddy();
        assertTrue(bob.sarosBot().state().isFollowingBuddy(alice.jid));

        bob.sarosBot().sessionView().selectBuddy(alice.jid)
            .stopFollowingThisBuddy();
        assertFalse(bob.sarosBot().state().isFollowingBuddy(alice.jid));

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
        alice.sarosBot().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS2);
        alice.bot().waitUntilEditorOpen(CLS2_SUFFIX);
        assertTrue(alice.bot().isEditorOpen(CLS2_SUFFIX));
        assertFalse(bob.bot().isEditorOpen(CLS2_SUFFIX));
        bob.bot().captureScreenshot(
            bob.bot().getPathToScreenShot() + "/vor_jump_to_position.png");
        bob.sarosBot().sessionView().selectBuddy(alice.jid)
            .jumpToPositionOfSelectedBuddy();
        bob.bot().captureScreenshot(
            bob.bot().getPathToScreenShot() + "/after_jump_to_position.png");
        assertTrue(bob.bot().editor(CLS2_SUFFIX).isActive());

        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        alice.bot().editor(CLS1 + SUFFIX_JAVA).show();
        assertTrue(alice.bot().editor(CLS1_SUFFIX).isActive());
        assertFalse(bob.bot().isEditorOpen(CLS1_SUFFIX));
        bob.sarosBot().sessionView().selectBuddy(alice.jid)
            .jumpToPositionOfSelectedBuddy();
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
        alice.sarosBot().sessionView().selectBuddy(bob.jid)
            .restrictToReadOnlyAccess();
        bob.sarosBot().packageExplorerView().selectClass(PROJECT1, PKG1, CLS1)
            .open();
        bob.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String editorTextOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertFalse(editorTextOfAlice.equals(editorTextOfBob));
        bob.sarosBot().condition().waitUntilIsInconsistencyDetected();
        bob.sarosBot().sessionView().inconsistencyDetected();
        editorTextOfAlice = alice.bot().editor(CLS1_SUFFIX).getText();
        editorTextOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertTrue(editorTextOfAlice.equals(editorTextOfBob));
    }

    @Test
    public void testRestrictInviteesToReadOnlyAccessGUI()
        throws RemoteException {
        assertTrue(alice.sarosBot().state().isHost());
        assertTrue(alice.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)
            .isEnabled());
        assertFalse(bob.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)
            .isEnabled());

        assertTrue(alice.sarosBot().state().hasWriteAccess());
        assertTrue(bob.sarosBot().state().hasWriteAccess());

        alice.sarosBot().sessionView().restrictInviteesToReadOnlyAccess();
        assertFalse(bob.sarosBot().state().hasWriteAccess());
        assertTrue(alice.sarosBot().state().hasWriteAccess());

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
        assertTrue(alice.sarosBot().state().isInSession());
        assertTrue(bob.sarosBot().state().isInSession());
        leaveSessionHostFirst();
        assertFalse(alice.sarosBot().state().isInSession());
        assertFalse(bob.sarosBot().state().isInSession());
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
        assertFalse(alice.sarosBot().state().existsLabelInSessionView());
        assertFalse(bob.sarosBot().state().existsLabelInSessionView());
        assertTrue(alice.sarosBot().state().isInSession());
        assertTrue(bob.sarosBot().state().isInSession());
        leaveSessionPeersFirst();
        assertFalse(alice.sarosBot().state().isInSession());
        assertFalse(bob.sarosBot().state().isInSession());
        assertTrue(alice.sarosBot().state().existsLabelInSessionView());
        assertTrue(bob.sarosBot().state().existsLabelInSessionView());
    }

    @Test
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sarosBot().state().isInSessionNoGUI());
        assertTrue(alice.sarosBot().state().isInSession());
        assertTrue(bob.sarosBot().state().isInSessionNoGUI());
        assertTrue(bob.sarosBot().state().isInSession());
        leaveSessionHostFirst();
        assertFalse(alice.sarosBot().state().isInSessionNoGUI());
        assertFalse(bob.sarosBot().state().isInSessionNoGUI());
        assertFalse(alice.sarosBot().state().isInSession());
        assertFalse(bob.sarosBot().state().isInSession());
    }

    @Test
    public void inviteUsersInSession() throws RemoteException,
        InterruptedException {
        bob.sarosBot().sessionView().leaveTheSessionByPeer();
        bob.noBot().deleteProjectNoGUI(PROJECT1);
        assertFalse(bob.sarosBot().state().isInSession());
        inviteBuddies(PROJECT1, TypeOfCreateProject.NEW_PROJECT, alice, bob,
            carl);
        assertTrue(carl.sarosBot().state().isInSession());
        assertTrue(bob.sarosBot().state().isInSession());
    }

}
