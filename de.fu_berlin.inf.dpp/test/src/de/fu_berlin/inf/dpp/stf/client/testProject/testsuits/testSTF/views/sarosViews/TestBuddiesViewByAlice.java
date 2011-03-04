package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestBuddiesViewByAlice extends STFTest {
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);

        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testConnectWith() throws RemoteException {
        alice.sarosBot().views().buddiesView().connectWith(TEST_JID, PASSWORD);
        assertTrue(alice.sarosBot().menuBar().saros().preferences()
            .isAccountActive(bob.getJID()));
    }

    @Test
    public void testConnectWithActiveAccount() throws RemoteException {
        alice.sarosBot().views().buddiesView().connectWithActiveAccount();
        assertTrue(alice.sarosBot().views().buddiesView().isConnected());
    }

    @Test
    public void testAddExistedBuddy() throws RemoteException {
        assertTrue(alice.sarosBot().views().buddiesView().hasBuddy(TEST_JID));
        alice.sarosBot().views().buddiesView().addANewBuddy(TEST_JID);
        assertTrue(alice.sarosBot().views().buddiesView().hasBuddy(TEST_JID));
    }

}
