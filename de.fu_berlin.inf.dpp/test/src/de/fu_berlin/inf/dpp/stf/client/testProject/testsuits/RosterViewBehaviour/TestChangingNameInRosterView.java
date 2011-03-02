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
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        if (alice.sarosBot().views().buddiesView()
            .hasBuddyNickNameNoGUI(bob.jid)) {
            alice.sarosBot().views().buddiesView()
                .renameBuddy(bob.jid, bob.jid.getBase());
        }
        if (!alice.sarosBot().views().buddiesView().hasBuddyNoGUI(bob.jid)) {
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
        assertTrue(alice.sarosBot().views().buddiesView()
            .hasBuddyNoGUI(bob.jid));
        alice.sarosBot().views().buddiesView()
            .renameBuddy(bob.jid, bob.getName());
        assertTrue(alice.sarosBot().views().buddiesView()
            .hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBot().views().buddiesView()
            .getBuddyNickNameNoGUI(bob.jid).equals(bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.sarosBot().views().buddiesView().renameBuddy(bob.jid, "new bob");
        assertTrue(alice.sarosBot().views().buddiesView()
            .hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBot().views().buddiesView()
            .getBuddyNickNameNoGUI(bob.jid).equals("new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }

}
