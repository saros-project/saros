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

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Alice share a java project with bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. <br/>
     * make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        alice.rosterV.renameBuddy(bob.jid, bob.jid.getBase());
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @Test
    public void testRenameInRosterView() throws RemoteException {
        assertTrue(alice.rosterV.hasBuddyWith(bob.jid));
        alice.rosterV.renameBuddy(bob.jid, bob.getName());
        assertTrue(alice.rosterV.hasBuddyWith(bob.jid));
        assertTrue(alice.state.getBuddyNickName(bob.jid).equals(bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.rosterV.renameBuddy(bob.jid, "new bob");
        assertTrue(alice.rosterV.hasBuddyWith(bob.jid));
        assertTrue(alice.state.getBuddyNickName(bob.jid).equals("new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }
}
