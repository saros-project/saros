package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestBuddiesByAlice extends STFTest {
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testAddExistedBuddy() throws RemoteException {
        assertTrue(alice.superBot().views().sarosView().hasBuddy(TEST_JID));
        alice.superBot().views().sarosView().selectBuddies().addBuddy(TEST_JID);
        // alice.superBot().views().sarosView().addANewBuddy(TEST_JID);
        assertTrue(alice.superBot().views().sarosView().hasBuddy(TEST_JID));
    }
}
