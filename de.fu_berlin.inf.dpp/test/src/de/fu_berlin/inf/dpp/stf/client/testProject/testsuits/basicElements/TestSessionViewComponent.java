package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestSessionViewComponent extends STFTest {

    private static final Logger log = Logger
        .getLogger(TestSessionViewComponent.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbenchs();
        setUpSaros();
        setUpSessionByDefault(alice, bob);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        reBuildSession(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetWriteAccess(alice, bob);
        resetFollowMode(alice, bob);
    }

    @Test
    public void testSetFocusOnSessionView() throws RemoteException {
        log.trace("alice set focus on session view.");
        alice.view.setFocusOnViewByTitle(VIEW_SAROS_SESSION);
        assertTrue(alice.view.isViewActive(VIEW_SAROS_SESSION));
        log.trace("alice close session view.");
        alice.view.closeViewById(VIEW_SAROS_SESSION_ID);
        assertFalse(alice.view.isViewActive(VIEW_SAROS_SESSION));
        log.trace("alice open session view again");
        alice.view.openViewById(VIEW_SAROS_SESSION_ID);
        assertTrue(alice.view.isViewActive(VIEW_SAROS_SESSION));
        log.trace("alice focus on roster view.");
        alice.sarosBuddiesV.setFocusOnRosterView();
        assertFalse(alice.view.isViewActive(VIEW_SAROS_SESSION));
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

        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        bob.editor.waitUntilJavaEditorOpen(CLS1);
        assertTrue(bob.editor.isJavaEditorOpen(CLS1));

        alice.sarosSessionV.followThisBuddy(bob.jid);
        assertTrue(alice.sarosSessionV.isFollowingBuddy(bob.jid));

        bob.editor.closeJavaEditorWithSave(CLS1);
        alice.editor.waitUntilJavaEditorClosed(CLS1);
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
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
        alice.editor.waitUntilJavaEditorOpen(CLS2);
        assertTrue(alice.editor.isJavaEditorOpen(CLS2));
        assertFalse(bob.editor.isJavaEditorOpen(CLS2));
        bob.workbench.captureScreenshot(bob.workbench.getPathToScreenShot()
            + "/vor_jump_to_position.png");
        bob.sarosSessionV.jumpToPositionOfSelectedBuddy(alice.jid);
        bob.workbench.captureScreenshot(bob.workbench.getPathToScreenShot()
            + "/after_jump_to_position.png");
        assertTrue(bob.editor.isJavaEditorActive(CLS2));

        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.activateJavaEditor(CLS1);
        assertTrue(alice.editor.isJavaEditorActive(CLS1));
        assertFalse(bob.editor.isJavaEditorOpen(CLS1));
        bob.sarosSessionV.jumpToPositionOfSelectedBuddy(alice.jid);
        assertTrue(bob.editor.isJavaEditorActive(CLS1));
    }

    @Test
    @Ignore
    // FIXME dialog with error message "Xuggler not installed"
    public void sharedYourScreenWithSelectedUserGUI() throws RemoteException {
        // alice.mainMenu.setupSettingForScreensharing(1, 0, -1, -1);
        alice.shareYourScreenWithSelectedUserDone(bob);
        bob.rSV.waitUntilRemoteScreenViewIsActive();
        assertTrue(bob.rSV.isRemoteScreenViewActive());
        alice.sarosSessionV.stopSessionWithUser(bob.jid);
    }

    @Test
    public void inconsistencyDetectedGUI() throws RemoteException {
        String editorTextOfAlice = alice.editor.getTextOfJavaEditor(PROJECT1,
            PKG1, CLS1);
        alice.sarosSessionV.restrictToReadOnlyAccess(bob.jid);
        bob.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        String editorTextOfBob = bob.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        assertFalse(editorTextOfAlice.equals(editorTextOfBob));
        bob.sarosSessionV.waitUntilInconsistencyDetected();
        bob.sarosSessionV.inconsistencyDetected();
        editorTextOfAlice = alice.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        editorTextOfBob = bob.editor.getTextOfJavaEditor(PROJECT1, PKG1, CLS1);
        assertTrue(editorTextOfAlice.equals(editorTextOfBob));
    }

    @Test
    public void testRestrictInviteesToReadOnlyAccessGUI()
        throws RemoteException {
        assertTrue(alice.sarosSessionV.isHost());
        assertTrue(alice.sarosSessionV
            .isRestrictInviteesToReadOnlyAccessEnabled());
        assertFalse(bob.sarosSessionV
            .isRestrictInviteesToReadOnlyAccessEnabled());

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
        alice.leaveSessionHostFirstDone(bob);
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
        assertFalse(alice.sarosSessionV.existsLabelTextInSessionView());
        assertFalse(bob.sarosSessionV.existsLabelTextInSessionView());
        assertTrue(alice.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSession());
        alice.leaveSessionPeersFirstDone(bob);
        assertFalse(alice.sarosSessionV.isInSession());
        assertFalse(bob.sarosSessionV.isInSession());
        assertTrue(alice.sarosSessionV.existsLabelTextInSessionView());
        assertTrue(bob.sarosSessionV.existsLabelTextInSessionView());
    }

    @Test
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sarosSessionV.isInSessionNoGUI());
        assertTrue(alice.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSessionNoGUI());
        assertTrue(bob.sarosSessionV.isInSession());
        alice.leaveSessionHostFirstDone(bob);
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
        alice.inviteBuddiesInSessionDone(PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, bob, carl);
        assertTrue(carl.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSession());
    }

}
