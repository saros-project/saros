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

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestSessionViewComponent extends STFTest {

    private static final Logger log = Logger
        .getLogger(TestSessionViewComponent.class);

    @BeforeClass
    public static void initMusican() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        log.trace("alice create a new proejct and a new class.");
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        log.trace("alice share session with bob.");
        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @Before
    public void startUp() throws RemoteException {
        if (!alice.state.isInSession()) {
            bob.typeOfSharingProject = USE_EXISTING_PROJECT;
            alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
                bob);
        }
        bob.workbench.openSarosViews();
        alice.workbench.openSarosViews();
    }

    @After
    public void cleanUp() throws RemoteException {

        if (bob.state.isInSession() && bob.state.isDriver()) {
            alice.sessionV.removeDriverRole(bob.state);
        }
        if (alice.state.isInSession() && !alice.state.isDriver()) {
            alice.sessionV.giveDriverRole(alice.state);
        }
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
        if (bob.state.isInSession() && bob.state.isInFollowMode()) {
            bob.sessionV.stopFollowing();
        }
        if (alice.state.isInSession() && alice.state.isInFollowMode()) {
            alice.sessionV.stopFollowing();
        }
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

        alice.sessionV.giveDriverRole(bob.state);
        assertTrue(alice.state.isDriver());
        assertTrue(alice.state.isDriver(bob.jid));
        assertTrue(bob.state.isDriver());

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
        alice.sessionV.getContactStatusInSessionView(alice.jid).equals(
            OWN_CONTACT_NAME + ROLE_NAME);
        alice.sessionV.getContactStatusInSessionView(bob.jid).equals(
            bob.getBaseJid());
        bob.sessionV.getContactStatusInSessionView(bob.jid).equals(
            OWN_CONTACT_NAME);
        bob.sessionV.getContactStatusInSessionView(alice.jid).equals(
            alice.getBaseJid() + ROLE_NAME);

        log.trace("alice give bob exclusive driver role.");
        alice.sessionV.giveExclusiveDriverRole(bob.state);
        assertFalse(alice.state.isDriver());
        assertTrue(bob.state.isDriver());

        alice.sessionV.getContactStatusInSessionView(alice.jid).equals(
            OWN_CONTACT_NAME);
        alice.sessionV.getContactStatusInSessionView(bob.jid).equals(
            bob.getBaseJid() + ROLE_NAME);
        bob.sessionV.getContactStatusInSessionView(bob.jid).equals(
            OWN_CONTACT_NAME + ROLE_NAME);
        bob.sessionV.getContactStatusInSessionView(alice.jid).equals(
            alice.getBaseJid());
    }

    @Test
    public void testIsInFollowMode() throws RemoteException {
        assertFalse(alice.state.isInFollowMode());
        assertFalse(bob.state.isInFollowMode());
        bob.sessionV.followThisUser(alice.state);
        assertTrue(bob.state.isInFollowMode());
        alice.sessionV.followThisUser(bob.state);
        assertTrue(alice.state.isInFollowMode());
    }

    /**
     * TODO there are some exception.
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testShareYourScreenWithSelectedUser() throws RemoteException {
        alice.shareYourScreenWithSelectedUserDone(bob);
        bob.rSV.waitUntilRemoteScreenViewIsActive();
        assertTrue(bob.rSV.isRemoteScreenViewActive());
        alice.sessionV.stopSessionWithUser(bob.state);
    }

    @Test
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sessionV.isInSession());
        assertTrue(bob.sessionV.isInSession());
        alice.leaveSessionFirst(bob);
        assertFalse(alice.sessionV.isInSession());
        assertFalse(bob.sessionV.isInSession());
    }

}
