package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestShare3UsersSequentially extends STFTest {

    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        carl = InitMusician.newCarl();
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        carl.workbench.resetSaros();
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        carl.workbench.resetWorkbench();
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @Test
    public void testShareProject3UsersSequentially() throws RemoteException,
        InterruptedException {

        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, carl,
            bob);

        assertTrue(carl.state.isParticipant(carl.jid));
        assertTrue(carl.state.isObserver(carl.jid));
        assertTrue(carl.state.isParticipant(bob.jid));
        assertTrue(carl.state.isObserver(bob.jid));
        assertTrue(carl.state.isParticipant(alice.jid));
        assertTrue(carl.state.isDriver(alice.jid));

        assertTrue(bob.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
        assertTrue(bob.state.isParticipant(carl.jid));
        assertTrue(bob.state.isObserver(carl.jid));
        assertTrue(bob.state.isParticipant(alice.jid));
        assertTrue(bob.state.isDriver(alice.jid));

        assertTrue(alice.state.isParticipant(alice.jid));
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(carl.jid));
        assertTrue(alice.state.isObserver(carl.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(alice.state.isObserver(bob.jid));

        alice.leaveSessionFirstByPeers(carl, bob);
        assertFalse(carl.state.isParticipant(carl.jid));
        assertFalse(bob.state.isParticipant(bob.jid));
        assertFalse(alice.state.isParticipant(alice.jid));

    }
}
