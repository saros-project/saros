package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestGroupBuddiesByAlice extends STFTest {
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testConnectWith() throws RemoteException {
        alice.superBot().views().buddiesView().connectWith(TEST_JID, PASSWORD);
        assertTrue(alice.superBot().menuBar().saros().preferences()
            .isAccountActive(TEST_JID));
    }

    @Test
    public void testConnectWithActiveAccount() throws RemoteException {
        alice.superBot().views().buddiesView().connectWithActiveAccount();
        assertTrue(alice.superBot().views().buddiesView().isConnected());
    }

    @Test
    public void testAddExistedBuddy() throws RemoteException {
        assertTrue(alice.superBot().views().buddiesView().hasBuddy(TEST_JID));
        alice.superBot().views().buddiesView().addANewBuddy(TEST_JID);
        assertTrue(alice.superBot().views().buddiesView().hasBuddy(TEST_JID));
    }

}
