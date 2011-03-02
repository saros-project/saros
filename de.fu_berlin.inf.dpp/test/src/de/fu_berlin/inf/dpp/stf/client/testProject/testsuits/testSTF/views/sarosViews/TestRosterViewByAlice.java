package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestRosterViewByAlice extends STFTest {
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @Test
    public void testConnectWith() throws RemoteException {
        alice.sarosBot().views().buddiesView().connectWith(TEST_JID, PASSWORD);
        assertTrue(alice.sarosBot().saros().preferences()
            .isAccountActive(TEST_JID));
    }

    @Test
    public void testConnectWithActiveAccount() throws RemoteException {
        alice.sarosBot().views().buddiesView().connectWithActiveAccount();
        assertTrue(alice.sarosBot().views().buddiesView().isConnected());
    }
}
