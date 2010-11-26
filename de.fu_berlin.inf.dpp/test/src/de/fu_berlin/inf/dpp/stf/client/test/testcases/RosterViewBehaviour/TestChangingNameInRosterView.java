package de.fu_berlin.inf.dpp.stf.client.test.testcases.RosterViewBehaviour;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestChangingNameInRosterView extends STFTest {

    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        // carl = InitMusician.newCarl();
        // alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        // alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
        // bob);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        // carl.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        // alice.rosterV.renameContact(bob.jid.getName(), bob.jid.getBase());
        // bob.workbench.resetWorkbench();
        // carl.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @Test
    public void testReanmeInRosterView() throws RemoteException {
        assertTrue(alice.rosterV.hasContactWith(bob.jid));
        // alice.rosterV.renameContact(bob.jid.getBase(), bob.jid.getName());
        // assertTrue(alice.state.hasContactWith(bob.jid));
        // assertFalse(alice.rosterV.hasContactWith(bob.jid));
        // // assertTrue(alice.bot.hasContactWith(bob.jid.));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid.getBase()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid.getName()));

    }
}
