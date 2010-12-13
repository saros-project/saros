package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewComponent;

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
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbenchs();
        setUpSaros();
        setUpSession(alice, bob);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        alice.leaveSessionHostFirstDone(bob);
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        if (alice.rosterV.hasBuddyNickName(bob.jid)) {
            alice.rosterV.renameBuddy(bob.jid, bob.jid.getBase());
        }
        if (!alice.rosterV.hasBuddy(bob.jid)) {
            alice.addBuddyGUIDone(bob);
        }
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
    public void renameBuddyWithGUI() throws RemoteException {
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        alice.rosterV.renameBuddyGUI(bob.jid, bob.getName());
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(alice.rosterV.getBuddyNickName(bob.jid)
            .equals(bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.rosterV.renameBuddyGUI(bob.jid, "new bob");
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(alice.rosterV.getBuddyNickName(bob.jid).equals("new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }

    @Test
    public void renameBuddyWithoutGUI() throws RemoteException {
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        alice.rosterV.renameBuddy(bob.jid, bob.getName());
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(alice.rosterV.getBuddyNickName(bob.jid)
            .equals(bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.rosterV.renameBuddy(bob.jid, "new bob");
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(alice.rosterV.getBuddyNickName(bob.jid).equals("new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }

    @Test
    public void addBuddyWithGUI() throws RemoteException {
        alice.deleteBuddyGUIDone(bob);
        assertFalse(alice.rosterV.hasBuddy(bob.jid));
        assertFalse(bob.rosterV.hasBuddy(alice.jid));
        alice.addBuddyGUIDone(bob);
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(bob.rosterV.hasBuddy(alice.jid));
    }

    /**
     * FIXME the method
     * {@link RosterViewComponent#addANewContact(de.fu_berlin.inf.dpp.net.JID)}
     * 
     * @throws RemoteException
     * @throws XMPPException
     */
    @Test
    @Ignore
    public void addBuddyWithoutGUI() throws RemoteException, XMPPException {
        alice.deleteBuddyGUIDone(bob);
        assertFalse(alice.rosterV.hasBuddy(bob.jid));
        assertFalse(bob.rosterV.hasBuddy(alice.jid));
        alice.addBuddyDone(bob);
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(bob.rosterV.hasBuddy(alice.jid));
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
    public void deleteBuddyWithGUI() throws RemoteException {
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        alice.deleteBuddyGUIDone(bob);
        assertFalse(alice.rosterV.hasBuddy(bob.jid));
        assertFalse(bob.rosterV.hasBuddy(alice.jid));
    }

    @Test
    public void deleteBuddyWithoutGUI() throws RemoteException, XMPPException {
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
    public void inviteUserWithGUI() throws RemoteException {
        assertFalse(carl.sessionV.isInSessionGUI());
        alice.rosterV.inviteUserGUI(carl.jid);
        carl.pEV.confirmWirzardSessionInvitationWithNewProject(PROJECT1);
        carl.basic.sleep(1000);

        assertTrue(carl.sessionV.isInSessionGUI());
    }

}
