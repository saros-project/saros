package de.fu_berlin.inf.dpp.stf.client.test.testcases.RosterViewBehaviour;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
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
            alice.rosterV.renameBuddyGUI(bob.jid, bob.jid.getBase());
        }
        if (!alice.rosterV.hasBuddy(bob.jid)) {
            alice.addBuddyGUIDone(bob);
        }
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

}
