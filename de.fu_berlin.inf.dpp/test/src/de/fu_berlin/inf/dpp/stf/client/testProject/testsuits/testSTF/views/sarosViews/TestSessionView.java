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
        alice.bot().view(VIEW_SAROS_SESSION).setFocus();
        assertTrue(alice.bot().view(VIEW_SAROS_SESSION).isActive());
        log.trace("alice close session view.");
        alice.bot().view(VIEW_SAROS_SESSION).close();
        assertFalse(alice.bot().view(VIEW_SAROS_SESSION).isActive());
        log.trace("alice open session view again");
        alice.bot().openViewById(VIEW_SAROS_SESSION_ID);
        assertTrue(alice.bot().isViewOpen(VIEW_SAROS_SESSION));
        log.trace("alice focus on saros buddies view.");
        alice.bot().view(VIEW_SAROS_BUDDIES).setFocus();
        assertFalse(alice.bot().view(VIEW_SAROS_SESSION).isActive());
        log.trace("testSetFocusOnSessionView is done.");
    }

    @Test
    public void testRestrictToReadOnlyAccess() throws RemoteException {
        assertTrue(alice.sarosSessionV.hasWriteAccessBy(bob.jid));
        assertTrue(bob.sarosSessionV.hasWriteAccess());

        assertFalse(alice.sarosSessionV.hasReadOnlyAccessBy(bob.jid));
        assertFalse(bob.sarosSessionV.hasReadOnlyAccess());

        alice.sarosSessionV.restrictToReadOnlyAccess(bob.jid);

        assertFalse(alice.sarosSessionV.hasWriteAccessBy(bob.jid));
        assertFalse(bob.sarosSessionV.hasWriteAccess());

        assertTrue(alice.sarosSessionV.hasReadOnlyAccessBy(bob.jid));
        assertTrue(bob.sarosSessionV.hasReadOnlyAccess());
    }

    @Test
    public void testGrantWriteAccess() throws RemoteException {
        assertTrue(alice.sarosSessionV.hasWriteAccessBy(bob.jid));
        assertTrue(bob.sarosSessionV.hasWriteAccess());

        alice.sarosSessionV.restrictToReadOnlyAccess(bob.jid);

        assertFalse(bob.sarosSessionV.hasWriteAccess());
        assertFalse(alice.sarosSessionV.hasWriteAccessBy(bob.jid));

        alice.sarosSessionV.grantWriteAccess(bob.jid);

        assertTrue(bob.sarosSessionV.hasWriteAccess());
        assertTrue(alice.sarosSessionV.hasWriteAccessBy(bob.jid));

        assertFalse(alice.sarosSessionV.hasReadOnlyAccessBy(bob.jid));
        assertFalse(bob.sarosSessionV.hasReadOnlyAccess());
    }

    @Test
    public void testFollowMode() throws RemoteException {
        assertFalse(alice.sarosSessionV.isFollowingBuddy(bob.jid));
        assertFalse(bob.sarosSessionV.isFollowingBuddy(alice.jid));

        bob.sarosSessionV.followThisBuddy(alice.jid);
        assertTrue(bob.sarosSessionV.isFollowingBuddy(alice.jid));

        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        bob.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        assertTrue(bob.bot().isEditorOpen(CLS1_SUFFIX));

        alice.sarosSessionV.followThisBuddy(bob.jid);
        assertTrue(alice.sarosSessionV.isFollowingBuddy(bob.jid));

        bob.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        alice.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(alice.bot().isEditorOpen(CLS1_SUFFIX));
    }

    @Test
    public void testStopFollowing() throws RemoteException {
        assertFalse(alice.sarosSessionV.isFollowingBuddy(bob.jid));
        assertFalse(bob.sarosSessionV.isFollowingBuddy(alice.jid));

        bob.sarosSessionV.followThisBuddy(alice.jid);
        assertTrue(bob.sarosSessionV.isFollowingBuddy(alice.jid));

        bob.sarosSessionV.stopFollowingThisBuddy(alice.jid);
        assertFalse(bob.sarosSessionV.isFollowingBuddy(alice.jid));

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
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        alice.bot().waitUntilEditorOpen(CLS2_SUFFIX);
        assertTrue(alice.bot().isEditorOpen(CLS2_SUFFIX));
        assertFalse(bob.bot().isEditorOpen(CLS2_SUFFIX));
        bob.workbench.captureScreenshot(bob.workbench.getPathToScreenShot()
            + "/vor_jump_to_position.png");
        bob.sarosSessionV.jumpToPositionOfSelectedBuddy(alice.jid);
        bob.workbench.captureScreenshot(bob.workbench.getPathToScreenShot()
            + "/after_jump_to_position.png");
        assertTrue(bob.bot().editor(CLS2_SUFFIX).isActive());

        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1 + SUFFIX_JAVA).activate();
        assertTrue(alice.bot().editor(CLS1_SUFFIX).isActive());
        assertFalse(bob.bot().isEditorOpen(CLS1_SUFFIX));
        bob.sarosSessionV.jumpToPositionOfSelectedBuddy(alice.jid);
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());
    }

    @Test
    @Ignore
    // FIXME dialog with error message "Xuggler not installed"
    public void sharedYourScreenWithSelectedUserGUI() throws RemoteException {
        // alice.mainMenu.setupSettingForScreensharing(1, 0, -1, -1);
        shareYourScreen(alice, bob);
        bob.rSV.waitUntilRemoteScreenViewIsActive();
        assertTrue(bob.bot().view(VIEW_REMOTE_SCREEN).isActive());
        alice.sarosSessionV.stopSessionWithBuddy(bob.jid);
    }

    @Test
    public void inconsistencyDetectedGUI() throws RemoteException {
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        String editorTextOfAlice = alice.bot().editor(CLS1_SUFFIX).getText();
        alice.sarosSessionV.restrictToReadOnlyAccess(bob.jid);
        bob.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        bob.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String editorTextOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertFalse(editorTextOfAlice.equals(editorTextOfBob));
        bob.sarosSessionV.waitUntilIsInconsistencyDetected();
        bob.sarosSessionV.inconsistencyDetected();
        editorTextOfAlice = alice.bot().editor(CLS1_SUFFIX).getText();
        editorTextOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertTrue(editorTextOfAlice.equals(editorTextOfBob));
    }

    @Test
    public void testRestrictInviteesToReadOnlyAccessGUI()
        throws RemoteException {
        assertTrue(alice.sarosSessionV.isHost());
        assertTrue(alice.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)
            .isEnabled());
        assertFalse(bob.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)
            .isEnabled());

        assertTrue(alice.sarosSessionV.hasWriteAccess());
        assertTrue(bob.sarosSessionV.hasWriteAccess());

        alice.sarosSessionV.restrictInviteesToReadOnlyAccess();
        assertFalse(bob.sarosSessionV.hasWriteAccess());
        assertTrue(alice.sarosSessionV.hasWriteAccess());

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
        assertTrue(alice.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSession());
        leaveSessionHostFirst();
        assertFalse(alice.sarosSessionV.isInSession());
        assertFalse(bob.sarosSessionV.isInSession());
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
        assertFalse(alice.sarosSessionV.existsLabelInSessionView());
        assertFalse(bob.sarosSessionV.existsLabelInSessionView());
        assertTrue(alice.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSession());
        leaveSessionPeersFirst();
        assertFalse(alice.sarosSessionV.isInSession());
        assertFalse(bob.sarosSessionV.isInSession());
        assertTrue(alice.sarosSessionV.existsLabelInSessionView());
        assertTrue(bob.sarosSessionV.existsLabelInSessionView());
    }

    @Test
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sarosSessionV.isInSessionNoGUI());
        assertTrue(alice.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSessionNoGUI());
        assertTrue(bob.sarosSessionV.isInSession());
        leaveSessionHostFirst();
        assertFalse(alice.sarosSessionV.isInSessionNoGUI());
        assertFalse(bob.sarosSessionV.isInSessionNoGUI());
        assertFalse(alice.sarosSessionV.isInSession());
        assertFalse(bob.sarosSessionV.isInSession());
    }

    @Test
    public void inviteUsersInSession() throws RemoteException,
        InterruptedException {
        bob.sarosSessionV.leaveTheSessionByPeer();
        bob.editM.deleteProjectNoGUI(PROJECT1);
        assertFalse(bob.sarosSessionV.isInSession());
        inviteBuddies(PROJECT1, TypeOfCreateProject.NEW_PROJECT, alice, bob,
            carl);
        assertTrue(carl.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSession());
    }

}
