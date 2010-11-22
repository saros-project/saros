package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestSessionViewObject extends STFTest {

    private static final Logger log = Logger
        .getLogger(TestSessionViewObject.class);
    private static Musician alice;
    private static Musician bob;

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
        bob.workbench.openSarosViews();
        alice.workbench.openSarosViews();
        if (!alice.state.isInSession()) {
            bob.typeOfSharingProject = USE_EXISTING_PROJECT;
            alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
                bob);
        }
        if (bob.state.isDriver()) {
            alice.sessionV.removeDriverRole(bob.state);
        }
        if (!alice.state.isDriver()) {
            alice.sessionV.giveDriverRole(alice.state);
        }

    }

    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
        // bob.sessionV.stopFollowing();
        // alice.sessionV.stopFollowing();
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
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sessionV.isInSession());
        assertTrue(alice.state.isInSession());
        assertTrue(bob.sessionV.isInSession());
        assertTrue(bob.state.isInSession());
        alice.leaveSessionFirst(bob);
        assertFalse(alice.sessionV.isInSession());
        assertFalse(alice.state.isInSession());
        assertFalse(bob.sessionV.isInSession());
        assertFalse(bob.state.isInSession());
    }

    @Test
    public void testGiveDriverRole() throws RemoteException {
        assertTrue(alice.sessionV.isContactInSessionView(OWN_CONTACT_NAME
            + ROLE_NAME));
        assertTrue(alice.sessionV.isContactInSessionView(bob.getBaseJid()));
        assertTrue(bob.sessionV.isContactInSessionView(OWN_CONTACT_NAME));
        assertTrue(bob.sessionV.isContactInSessionView(alice.getBaseJid()
            + ROLE_NAME));

        log.trace("alice give bob driver role.");
        alice.sessionV.giveDriverRole(bob.state);
        assertTrue(alice.state.isDriver());
        assertTrue(bob.state.isDriver());

        assertTrue(alice.sessionV.isContactInSessionView(OWN_CONTACT_NAME
            + ROLE_NAME));
        assertTrue(alice.sessionV.isContactInSessionView(bob.getBaseJid()
            + ROLE_NAME));
        assertTrue(bob.sessionV.isContactInSessionView(OWN_CONTACT_NAME
            + ROLE_NAME));
        assertTrue(bob.sessionV.isContactInSessionView(alice.getBaseJid()
            + ROLE_NAME));

    }

    @Test
    public void testGiveExclusiveDriverRole() throws RemoteException {
        assertTrue(alice.sessionV.isContactInSessionView(OWN_CONTACT_NAME
            + ROLE_NAME));
        assertTrue(alice.sessionV.isContactInSessionView(bob.getBaseJid()));
        assertTrue(bob.sessionV.isContactInSessionView(OWN_CONTACT_NAME));
        assertTrue(bob.sessionV.isContactInSessionView(alice.getBaseJid()
            + ROLE_NAME));

        log.trace("alice give bob exclusive driver role.");
        alice.sessionV.giveExclusiveDriverRole(bob.state);
        assertFalse(alice.state.isDriver());
        assertTrue(bob.state.isDriver());

        assertTrue(alice.sessionV.isContactInSessionView(OWN_CONTACT_NAME));
        assertTrue(alice.sessionV.isContactInSessionView(bob.getBaseJid()
            + ROLE_NAME));
        assertTrue(bob.sessionV.isContactInSessionView(OWN_CONTACT_NAME
            + ROLE_NAME));
        assertTrue(bob.sessionV.isContactInSessionView(alice.getBaseJid()));
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
    public void testShareYourScreenWithSelectedUser() throws RemoteException {
        alice.shareYourScreenWithSelectedUserDone(bob);
        bob.rSV.waitUntilRemoteScreenViewIsActive();
        assertTrue(bob.rSV.isRemoteScreenViewActive());
        alice.sessionV.stopSessionWithUser(bob.state);
    }

}
