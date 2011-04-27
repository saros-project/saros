package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.RosterViewBehaviour;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestChangingNameInRosterView extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Alice share a java project with bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        // resetBuddiesName();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        // if (alice.sarosBot().views().buddiesView()
        // .hasBuddyNickNameNoGUI(bob.jid)) {
        // alice.sarosBot().views().buddiesView()
        // .renameBuddy(bob.jid, bob.jid.getBase());
        // }
        resetBuddiesName();
        if (!alice.superBot().views().sarosView().hasBuddy(bob.getJID())) {
            addBuddies(alice, bob);
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
        assertTrue(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .rename(bob.getName());
        assertTrue(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        assertTrue(alice.superBot().views().sarosView()
            .getNickName(bob.getJID()).equals(bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.getJID()));
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .rename("new bob");
        assertTrue(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        assertTrue(alice.superBot().views().sarosView()
            .getNickName(bob.getJID()).equals("new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.getJID()));
    }

}
