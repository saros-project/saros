package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

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

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestSessionViewComponent extends STFTest {

    private static final Logger log = Logger
        .getLogger(TestSessionViewComponent.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(tester.ALICE, tester.BOB, tester.CARL);
        setUpWorkbenchs();
        setUpSaros();
        setUpSession(alice, bob);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        resetSaros(alice, bob);
        resetWorkbenches();
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        resetWorkbenches();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetWorkbenches();
        resetDriverRole(alice, bob);
        resetFollowModel(alice, bob);
    }

    @Test
    public void testSetFocusOnSessionView() throws RemoteException {
        log.trace("alice set focus on session view.");
        alice.sessionV.setFocusOnSessionView();
        assertTrue(alice.sessionV.isSessionViewActive());
        log.trace("alice close session view.");
        alice.sessionV.closeSessionView();
        assertFalse(alice.sessionV.isSessionViewActive());
        log.trace("alice open session view again");
        alice.sessionV.openSessionView();
        assertTrue(alice.sessionV.isSessionViewActive());
        log.trace("alice focus on roster view.");
        alice.rosterV.setFocusOnRosterView();
        assertFalse(alice.sessionV.isSessionViewActive());
        log.trace("testSetFocusOnSessionView is done.");
    }

    @Test
    public void testGiveDriverRole() throws RemoteException {
        alice.sessionV.getContactStatusInSessionView(alice.jid).equals(
            OWN_CONTACT_NAME + ROLE_NAME);
        alice.sessionV.getContactStatusInSessionView(bob.jid).equals(
            bob.getBaseJid());
        bob.sessionV.getContactStatusInSessionView(bob.jid).equals(
            OWN_CONTACT_NAME);
        bob.sessionV.getContactStatusInSessionView(alice.jid).equals(
            alice.getBaseJid() + ROLE_NAME);

        alice.sessionV.giveDriverRoleGUI(bob.sessionV);
        assertTrue(alice.sessionV.isDriver());
        assertTrue(alice.sessionV.isDriver(bob.jid));
        assertTrue(bob.sessionV.isDriver());

        alice.sessionV.getContactStatusInSessionView(alice.jid).equals(
            OWN_CONTACT_NAME + ROLE_NAME);
        alice.sessionV.getContactStatusInSessionView(bob.jid).equals(
            bob.getBaseJid() + ROLE_NAME);
        bob.sessionV.getContactStatusInSessionView(bob.jid).equals(
            OWN_CONTACT_NAME + ROLE_NAME);
        bob.sessionV.getContactStatusInSessionView(alice.jid).equals(
            alice.getBaseJid() + ROLE_NAME);
    }

    @Test
    public void testGiveExclusiveDriverRole() throws RemoteException {
        assertTrue(alice.sessionV.getContactStatusInSessionView(alice.jid)
            .equals(OWN_CONTACT_NAME + ROLE_NAME));
        assertTrue(alice.sessionV.getContactStatusInSessionView(bob.jid)
            .equals(bob.getBaseJid()));
        assertTrue(bob.sessionV.getContactStatusInSessionView(bob.jid).equals(
            OWN_CONTACT_NAME));
        assertTrue(bob.sessionV.getContactStatusInSessionView(alice.jid)
            .equals(alice.getBaseJid() + ROLE_NAME));

        log.trace("alice give bob exclusive driver role.");
        alice.sessionV.giveExclusiveDriverRoleGUI(bob.sessionV);
        assertFalse(alice.sessionV.isDriver());
        assertTrue(bob.sessionV.isDriver());

        assertTrue(alice.sessionV.getContactStatusInSessionView(alice.jid)
            .equals(OWN_CONTACT_NAME));
        assertTrue(alice.sessionV.getContactStatusInSessionView(bob.jid)
            .equals(bob.getBaseJid() + ROLE_NAME));
        assertTrue(bob.sessionV.getContactStatusInSessionView(bob.jid).equals(
            OWN_CONTACT_NAME + ROLE_NAME));
        assertTrue(bob.sessionV.getContactStatusInSessionView(alice.jid)
            .equals(alice.getBaseJid()));
    }

    @Test
    public void removeDriverRole() throws RemoteException {
        alice.sessionV.giveDriverRoleGUI(bob.sessionV);
        assertTrue(alice.sessionV.isDriver());
        assertTrue(bob.sessionV.isDriver());
        alice.sessionV.removeDriverRoleGUI(bob.sessionV);
        assertTrue(alice.sessionV.isDriver());
        assertFalse(bob.sessionV.isDriver());
        assertTrue(alice.sessionV.getContactStatusInSessionView(alice.jid)
            .equals(OWN_CONTACT_NAME + ROLE_NAME));
        assertFalse(alice.sessionV.getContactStatusInSessionView(bob.jid)
            .equals(bob.getBaseJid() + ROLE_NAME));
        assertFalse(bob.sessionV.getContactStatusInSessionView(bob.jid).equals(
            OWN_CONTACT_NAME + ROLE_NAME));
        assertTrue(bob.sessionV.getContactStatusInSessionView(alice.jid)
            .equals(alice.getBaseJid() + ROLE_NAME));
    }

    @Test
    public void testIsInFollowMode() throws RemoteException {
        assertFalse(alice.sessionV.isInFollowMode());
        assertFalse(bob.sessionV.isInFollowMode());
        bob.sessionV.followThisUserGUI(alice.jid);
        assertTrue(bob.sessionV.isInFollowMode());
        alice.sessionV.followThisUserGUI(bob.jid);
        assertTrue(alice.sessionV.isInFollowMode());
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
        alice.pEV.newClass(PROJECT1, PKG1, CLS2);
        alice.editor.waitUntilJavaEditorOpen(CLS2);
        assertTrue(alice.editor.isJavaEditorOpen(CLS2));
        assertFalse(bob.editor.isJavaEditorOpen(CLS2));
        bob.basic.captureScreenshot(bob.basic.getPathToScreenShot()
            + "/vor_jump_to_position.png");
        bob.sessionV.jumpToPositionOfSelectedUserGUI(alice.jid);
        bob.basic.captureScreenshot(bob.basic.getPathToScreenShot()
            + "/after_jump_to_position.png");
        bob.editor.waitUntilJavaEditorActive(CLS2);
        assertTrue(bob.editor.isJavaEditorActive(CLS2));

        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.activateJavaEditor(CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        assertFalse(bob.editor.isJavaEditorOpen(CLS1));
        bob.sessionV.jumpToPositionOfSelectedUserGUI(alice.jid);
        bob.editor.waitUntilJavaEditorOpen(CLS1);
        assertTrue(bob.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void sharedYourScreenWithSelectedUserGUI() throws RemoteException {
        // alice.mainMenu.setupSettingForScreensharing(1, 0, -1, -1);
        alice.shareYourScreenWithSelectedUserDone(bob);
        bob.rSV.waitUntilRemoteScreenViewIsActive();
        assertTrue(bob.rSV.isRemoteScreenViewActive());
        alice.sessionV.stopSessionWithUserGUI(bob.jid);
    }

    @Test
    public void inconsistencyDetectedGUI() throws RemoteException {
        String editorTextOfAlice = alice.editor.getTextOfJavaEditor(PROJECT1,
            PKG1, CLS1);
        bob.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        String editorTextOfBob = bob.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        assertFalse(editorTextOfAlice.equals(editorTextOfBob));
        bob.sessionV.waitUntilInconsistencyDetected();
        bob.sessionV.inconsistencyDetectedGUI();
        editorTextOfAlice = alice.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        editorTextOfBob = bob.editor.getTextOfJavaEditor(PROJECT1, PKG1, CLS1);
        assertTrue(editorTextOfAlice.equals(editorTextOfBob));
    }

    @Test
    public void removeAllDriverGUI() throws RemoteException {
        assertTrue(alice.sessionV.isHost());
        assertTrue(alice.sessionV.isRemoveAllRiverEnabled());
        assertFalse(bob.sessionV.isRemoveAllRiverEnabled());
        assertTrue(alice.sessionV.isDriver());
        assertFalse(bob.sessionV.isDriver());
        alice.sessionV.giveDriverRoleGUI(bob.sessionV);
        assertTrue(bob.sessionV.isDriver());
        alice.sessionV.removeAllRriverRolesGUI();
        assertFalse(alice.sessionV.isDriver());
        bob.sessionV.waitUntilIsNoDriver();
        assertFalse(bob.sessionV.isDriver());
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
        assertTrue(alice.sessionV.isInSession());
        assertTrue(bob.sessionV.isInSession());
        alice.leaveSessionHostFirstDone(bob);
        assertFalse(alice.sessionV.isInSession());
        assertFalse(bob.sessionV.isInSession());
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
        assertFalse(alice.sessionV.existsLabelTextInSessionView());
        assertFalse(bob.sessionV.existsLabelTextInSessionView());
        assertTrue(alice.sessionV.isInSession());
        assertTrue(bob.sessionV.isInSession());
        alice.leaveSessionPeersFirstDone(bob);
        assertFalse(alice.sessionV.isInSession());
        assertFalse(bob.sessionV.isInSession());
        assertTrue(alice.sessionV.existsLabelTextInSessionView());
        assertTrue(bob.sessionV.existsLabelTextInSessionView());
    }

    @Test
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sessionV.isInSession());
        assertTrue(alice.sessionV.isInSessionGUI());
        assertTrue(bob.sessionV.isInSession());
        assertTrue(bob.sessionV.isInSessionGUI());
        alice.leaveSessionHostFirstDone(bob);
        assertFalse(alice.sessionV.isInSession());
        assertFalse(bob.sessionV.isInSession());
        assertFalse(alice.sessionV.isInSessionGUI());
        assertFalse(bob.sessionV.isInSessionGUI());
    }

    @Test
    public void inviteUsersInYourSession() throws RemoteException,
        InterruptedException {
        bob.sessionV.leaveTheSessionByPeer();
        assertFalse(bob.sessionV.isInSession());
        alice.inviteUsersInYourSessionDone(PROJECT1, bob, carl);
        assertTrue(carl.sessionV.isInSession());
        assertTrue(bob.sessionV.isInSession());
    }

}
