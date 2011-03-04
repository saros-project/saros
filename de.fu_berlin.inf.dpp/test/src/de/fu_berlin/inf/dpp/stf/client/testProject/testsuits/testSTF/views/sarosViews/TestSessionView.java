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
        assertTrue(alice.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).hasWriteAccess());
        assertTrue(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());

        assertFalse(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasReadOnlyAccess());
        assertFalse(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasReadOnlyAccess());

        alice.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .restrictToReadOnlyAccess();
        bob.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .waitUntilHasReadOnlyAccess();
        assertFalse(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());
        assertFalse(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());

        assertTrue(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasReadOnlyAccess());
        assertTrue(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasReadOnlyAccess());
    }

    @Test
    public void testGrantWriteAccess() throws RemoteException {
        assertTrue(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());
        assertTrue(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());

        alice.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .restrictToReadOnlyAccess();
        bob.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .waitUntilHasReadOnlyAccess();
        assertFalse(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());
        assertFalse(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());

        alice.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .grantWriteAccess();
        bob.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .waitUntilHasWriteAccess();
        assertTrue(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());
        assertTrue(alice.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).hasWriteAccess());

        assertFalse(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasReadOnlyAccess());
        assertFalse(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasReadOnlyAccess());

    }

    @Test
    public void testFollowMode() throws RemoteException {
        assertFalse(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).isFollowingThisBuddy());
        assertFalse(bob.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).isFollowingThisBuddy());

        bob.sarosBot().views().sessionView().selectParticipant(alice.getJID())
            .followThisBuddy();
        assertTrue(bob.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).isFollowingThisBuddy());

        alice.sarosBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();

        bob.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        assertTrue(bob.bot().isEditorOpen(CLS1_SUFFIX));

        alice.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .followThisBuddy();
        assertTrue(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).isFollowingThisBuddy());

        bob.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        alice.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(alice.bot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void testStopFollowing() throws RemoteException {
        assertFalse(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).isFollowingThisBuddy());
        assertFalse(bob.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).isFollowingThisBuddy());

        bob.sarosBot().views().sessionView().selectParticipant(alice.getJID())
            .followThisBuddy();
        assertTrue(bob.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).isFollowingThisBuddy());

        bob.sarosBot().views().sessionView().selectParticipant(alice.getJID())
            .stopFollowingThisBuddy();
        assertFalse(bob.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).isFollowingThisBuddy());

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
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS2);
        alice.bot().waitUntilEditorOpen(CLS2_SUFFIX);
        assertTrue(alice.bot().isEditorOpen(CLS2_SUFFIX));
        assertFalse(bob.bot().isEditorOpen(CLS2_SUFFIX));
        bob.bot().captureScreenshot(
            bob.bot().getPathToScreenShot() + "/vor_jump_to_position.png");
        bob.sarosBot().views().sessionView().selectParticipant(alice.getJID())
            .jumpToPositionOfSelectedBuddy();
        bob.bot().captureScreenshot(
            bob.bot().getPathToScreenShot() + "/after_jump_to_position.png");
        assertTrue(bob.bot().editor(CLS2_SUFFIX).isActive());

        alice.sarosBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        alice.bot().editor(CLS1 + SUFFIX_JAVA).show();
        assertTrue(alice.bot().editor(CLS1_SUFFIX).isActive());
        assertFalse(bob.bot().isEditorOpen(CLS1_SUFFIX));
        bob.sarosBot().views().sessionView().selectParticipant(alice.getJID())
            .jumpToPositionOfSelectedBuddy();
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());
    }

    @Test
    @Ignore
    // FIXME dialog with error message "Xuggler not installed"
    public void sharedYourScreenWithSelectedUserGUI() throws RemoteException {
        // alice.mainMenu.setupSettingForScreensharing(1, 0, -1, -1);
        shareYourScreen(alice, bob);
        bob.sarosBot().views().remoteScreenView()
            .waitUntilRemoteScreenViewIsActive();
        assertTrue(bob.bot().view(VIEW_REMOTE_SCREEN).isActive());
        alice.sarosBot().views().sessionView()
            .stopSessionWithBuddy(bob.getJID());
    }

    @Test
    public void inconsistencyDetectedGUI() throws RemoteException {
        alice.sarosBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        String editorTextOfAlice = alice.bot().editor(CLS1_SUFFIX).getText();
        alice.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .restrictToReadOnlyAccess();
        bob.sarosBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        bob.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String editorTextOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertFalse(editorTextOfAlice.equals(editorTextOfBob));
        bob.sarosBot().views().sessionView().waitUntilIsInconsistencyDetected();
        bob.sarosBot().views().sessionView().inconsistencyDetected();
        editorTextOfAlice = alice.bot().editor(CLS1_SUFFIX).getText();
        editorTextOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertTrue(editorTextOfAlice.equals(editorTextOfBob));
    }

    @Test
    public void testRestrictInviteesToReadOnlyAccessGUI()
        throws RemoteException {
        assertTrue(alice.sarosBot().views().sessionView().isHost());
        assertTrue(alice.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)
            .isEnabled());
        assertFalse(bob.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)
            .isEnabled());

        assertTrue(alice.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).hasWriteAccess());
        assertTrue(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());

        alice.sarosBot().views().sessionView()
            .restrictInviteesToReadOnlyAccess();
        assertFalse(bob.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).hasWriteAccess());
        assertTrue(alice.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).hasWriteAccess());

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
        assertTrue(alice.sarosBot().views().sessionView().isInSession());
        assertTrue(bob.sarosBot().views().sessionView().isInSession());
        leaveSessionHostFirst(alice);
        assertFalse(alice.sarosBot().views().sessionView().isInSession());
        assertFalse(bob.sarosBot().views().sessionView().isInSession());
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
        assertFalse(alice.sarosBot().views().sessionView()
            .existsLabelInSessionView());
        assertFalse(bob.sarosBot().views().sessionView()
            .existsLabelInSessionView());
        assertTrue(alice.sarosBot().views().sessionView().isInSession());
        assertTrue(bob.sarosBot().views().sessionView().isInSession());
        leaveSessionPeersFirst();
        assertFalse(alice.sarosBot().views().sessionView().isInSession());
        assertFalse(bob.sarosBot().views().sessionView().isInSession());
        assertTrue(alice.sarosBot().views().sessionView()
            .existsLabelInSessionView());
        assertTrue(bob.sarosBot().views().sessionView()
            .existsLabelInSessionView());
    }

    @Test
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sarosBot().views().sessionView().isInSession());
        assertTrue(bob.sarosBot().views().sessionView().isInSession());
        leaveSessionHostFirst(alice);
        assertFalse(alice.sarosBot().views().sessionView().isInSession());
        assertFalse(bob.sarosBot().views().sessionView().isInSession());
    }

    @Test
    public void inviteUsersInSession() throws RemoteException,
        InterruptedException {
        bob.sarosBot().views().sessionView().leaveSession();
        bob.noBot().deleteProjectNoGUI(PROJECT1);
        assertFalse(bob.sarosBot().views().sessionView().isInSession());
        inviteBuddies(PROJECT1, TypeOfCreateProject.NEW_PROJECT, alice, bob,
            carl);
        assertTrue(carl.sarosBot().views().sessionView().isInSession());
        assertTrue(bob.sarosBot().views().sessionView().isInSession());
    }

}
