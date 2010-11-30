package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestRosterViewComponent extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestRosterViewComponent.class);

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Car (Observer)</li>
     * <li>Alice share a java project with bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusican() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        carl = InitMusician.newCarl();
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionSequentially(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
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
        carl.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        if (alice.state.hasBuddyNickName(bob.jid)) {
            alice.rosterV.renameBuddy(bob.jid, bob.jid.getBase());
        }
        if (!alice.rosterV.hasBuddy(bob.jid)) {
            alice.addBuddyDone(bob);
        }
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @Test
    public void openCloseRosterView() throws RemoteException {
        alice.rosterV.closeRosterView();
        assertEquals(false, alice.rosterV.isRosterViewOpen());
        alice.rosterV.openRosterView();
        assertEquals(true, alice.rosterV.isRosterViewOpen());
    }

    @Test
    public void setFocusOnRosterView() throws RemoteException {
        alice.rosterV.setFocusOnRosterView();
        assertTrue(alice.rosterV.isRosterViewActive());
        alice.rosterV.closeRosterView();
        assertFalse(alice.rosterV.isRosterViewActive());

        alice.rosterV.openRosterView();
        assertTrue(alice.rosterV.isRosterViewActive());

        alice.sessionV.setFocusOnSessionView();
        assertFalse(alice.rosterV.isRosterViewActive());
    }

    /**
     * Steps:
     * <ol>
     * <li>alice rename bob to "bob_stf".</li>
     * <li>alice rename bob to "new bob".</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice hat contact with bob and bob'name is changed.</li>
     * <li>alice hat contact with bob and bob'name is changed.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void renameBuddyInRosterView() throws RemoteException {
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        alice.rosterV.renameBuddy(bob.jid, bob.getName());
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(alice.state.getBuddyNickName(bob.jid).equals(bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.rosterV.renameBuddy(bob.jid, "new bob");
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(alice.state.getBuddyNickName(bob.jid).equals("new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete bob</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice and bob don't contact each other</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void deleteBuddyInRosterView() throws RemoteException {
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        alice.deleteBuddyDone(bob);
        assertFalse(alice.rosterV.hasBuddy(bob.jid));
        assertFalse(bob.rosterV.hasBuddy(alice.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice invite bob</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob is in the session</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void inviteUserInRosterView() throws RemoteException {
        assertFalse(carl.sessionV.isInSession());
        alice.rosterV.inviteUser(carl.jid);
        carl.pEV.confirmWirzardSessionInvitationWithNewProject(PROJECT1);
        assertTrue(carl.sessionV.isInSession());
    }

}
