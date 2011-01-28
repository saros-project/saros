package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
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

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        // alice.leaveSessionHostFirstDone(bob, carl);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        reBuildSession(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetWriteAccess(alice, bob);
        resetFollowModel(alice, bob);
    }

    @Test
    public void testSetFocusOnSessionView() throws RemoteException {
        log.trace("alice set focus on session view.");
        alice.sarosSessionV.setFocusOnSessionView();
        assertTrue(alice.sarosSessionV.isSessionViewActive());
        log.trace("alice close session view.");
        alice.sarosSessionV.closeSessionView();
        assertFalse(alice.sarosSessionV.isSessionViewActive());
        log.trace("alice open session view again");
        alice.sarosSessionV.openSessionView();
        assertTrue(alice.sarosSessionV.isSessionViewActive());
        log.trace("alice focus on roster view.");
        alice.sarosBuddiesV.setFocusOnRosterView();
        assertFalse(alice.sarosSessionV.isSessionViewActive());
        log.trace("testSetFocusOnSessionView is done.");
    }

    @Test
    public void testRestrictToReadOnlyAccess() throws RemoteException {
        alice.sarosSessionV.getParticipantLabel(alice.jid).equals(
            OWN_CONTACT_NAME);
        alice.sarosSessionV.getParticipantLabel(bob.jid).equals(
            bob.getBaseJid());
        bob.sarosSessionV.getParticipantLabel(bob.jid).equals(OWN_CONTACT_NAME);
        bob.sarosSessionV.getParticipantLabel(alice.jid).equals(
            alice.getBaseJid());

        assertTrue(alice.sarosSessionV.hasWriteAccess());
        assertTrue(bob.sarosSessionV.hasWriteAccess());

        alice.sarosSessionV.restrictToReadOnlyAccessGUI(bob.sarosSessionV);

        assertTrue(alice.sarosSessionV.hasWriteAccess());
        assertFalse(alice.sarosSessionV.hasWriteAccess(bob.jid));
        assertTrue(bob.sarosSessionV.hasReadOnlyAccess());

        alice.sarosSessionV.getParticipantLabel(alice.jid).equals(
            OWN_CONTACT_NAME);
        alice.sarosSessionV.getParticipantLabel(bob.jid).equals(
            bob.getBaseJid() + PERMISSION_NAME);
        bob.sarosSessionV.getParticipantLabel(bob.jid).equals(
            OWN_CONTACT_NAME + PERMISSION_NAME);
        bob.sarosSessionV.getParticipantLabel(alice.jid).equals(
            alice.getBaseJid());
    }

    @Test
    public void testGrantWriteAccess() throws RemoteException {

        assertTrue(alice.sarosSessionV.hasWriteAccess());
        assertTrue(bob.sarosSessionV.hasWriteAccess());
        alice.sarosSessionV.restrictToReadOnlyAccessGUI(bob.sarosSessionV);
        assertTrue(alice.sarosSessionV.hasWriteAccess());
        assertFalse(bob.sarosSessionV.hasWriteAccess());

        alice.sarosSessionV.grantWriteAccessGUI(bob.sarosSessionV);
        assertTrue(bob.sarosSessionV.hasWriteAccess());

        assertTrue(alice.sarosSessionV.getParticipantLabel(alice.jid).equals(
            OWN_CONTACT_NAME));
        assertTrue(alice.sarosSessionV.getParticipantLabel(bob.jid).equals(
            bob.getBaseJid()));
        assertTrue(bob.sarosSessionV.getParticipantLabel(bob.jid).equals(
            OWN_CONTACT_NAME));
        assertTrue(bob.sarosSessionV.getParticipantLabel(alice.jid).equals(
            alice.getBaseJid()));
    }

    @Test
    public void testIsInFollowMode() throws RemoteException {
        assertFalse(alice.sarosSessionV.isInFollowMode());
        assertFalse(bob.sarosSessionV.isInFollowMode());
        bob.sarosSessionV.followThisBuddyGUI(alice.jid);
        assertTrue(bob.sarosSessionV.isInFollowMode());
        alice.sarosSessionV.followThisBuddyGUI(bob.jid);
        assertTrue(alice.sarosSessionV.isInFollowMode());
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
    @Ignore
    public void jumpToSelectedUserGUI() throws RemoteException {
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        alice.editor.waitUntilJavaEditorOpen(CLS2);
        assertTrue(alice.editor.isJavaEditorOpen(CLS2));
        assertFalse(bob.editor.isJavaEditorOpen(CLS2));
        bob.workbench.captureScreenshot(bob.workbench.getPathToScreenShot()
            + "/vor_jump_to_position.png");
        bob.sarosSessionV.jumpToPositionOfSelectedBuddyGUI(alice.jid);
        bob.workbench.captureScreenshot(bob.workbench.getPathToScreenShot()
            + "/after_jump_to_position.png");
        bob.editor.waitUntilJavaEditorActive(CLS2);
        assertTrue(bob.editor.isJavaEditorActive(CLS2));

        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.activateJavaEditor(CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        assertFalse(bob.editor.isJavaEditorOpen(CLS1));
        bob.sarosSessionV.jumpToPositionOfSelectedBuddyGUI(alice.jid);
        bob.editor.waitUntilJavaEditorOpen(CLS1);
        assertTrue(bob.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    @Ignore
    // FIXME dialog with error message "Xuggler not installed"
    public void sharedYourScreenWithSelectedUserGUI() throws RemoteException {
        // alice.mainMenu.setupSettingForScreensharing(1, 0, -1, -1);
        alice.shareYourScreenWithSelectedUserDone(bob);
        bob.rSV.waitUntilRemoteScreenViewIsActive();
        assertTrue(bob.rSV.isRemoteScreenViewActive());
        alice.sarosSessionV.stopSessionWithUserGUI(bob.jid);
    }

    @Test
    public void inconsistencyDetectedGUI() throws RemoteException {
        String editorTextOfAlice = alice.editor.getTextOfJavaEditor(PROJECT1,
            PKG1, CLS1);
        alice.sarosSessionV.restrictToReadOnlyAccessGUI(bob.sarosSessionV);
        bob.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        String editorTextOfBob = bob.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        assertFalse(editorTextOfAlice.equals(editorTextOfBob));
        bob.sarosSessionV.waitUntilInconsistencyDetected();
        bob.sarosSessionV.inconsistencyDetectedGUI();
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

        alice.sarosSessionV.restrictInviteesToReadOnlyAccessGUI();
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
        assertTrue(alice.sarosSessionV.isInSession());
        assertTrue(alice.sarosSessionV.isInSessionGUI());
        assertTrue(bob.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSessionGUI());
        alice.leaveSessionHostFirstDone(bob);
        assertFalse(alice.sarosSessionV.isInSession());
        assertFalse(bob.sarosSessionV.isInSession());
        assertFalse(alice.sarosSessionV.isInSessionGUI());
        assertFalse(bob.sarosSessionV.isInSessionGUI());
    }

    @Test
    public void inviteUsersInSession() throws RemoteException,
        InterruptedException {
        bob.sarosSessionV.leaveTheSessionByPeer();
        bob.editM.deleteProjectNoGUI(PROJECT1);
        assertFalse(bob.sarosSessionV.isInSession());
        alice.inviteUsersInSessionDone(PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, bob, carl);
        assertTrue(carl.sarosSessionV.isInSession());
        assertTrue(bob.sarosSessionV.isInSession());
    }

}
