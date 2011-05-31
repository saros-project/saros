package de.fu_berlin.inf.dpp.stf.test.stf.view.sarosview.content;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;

public class BuddiesByAliceTest extends StfTestCase {
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testAddExistedBuddy() throws RemoteException {
        assertTrue(ALICE.superBot().views().sarosView()
            .hasBuddy(Constants.TEST_JID));
        ALICE.superBot().views().sarosView().selectBuddies()
            .addBuddy(Constants.TEST_JID);
        // ALICE.superBot().views().sarosView().addANewBuddy(TEST_JID);
        assertTrue(ALICE.superBot().views().sarosView()
            .hasBuddy(Constants.TEST_JID));
    }
}
