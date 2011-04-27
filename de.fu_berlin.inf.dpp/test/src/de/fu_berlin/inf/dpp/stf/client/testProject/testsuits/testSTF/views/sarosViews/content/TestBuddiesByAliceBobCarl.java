package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestBuddiesByAliceBobCarl extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetBuddies();
        resetBuddiesName();
    }

    @Test
    public void testPreCondition() {
        //
    }

    /**
     * Steps:
     * 
     * 1. Alice share session with Bob.
     * 
     * 2. Alice invite Carl.
     * 
     * Result:
     * <ol>
     * <li>Carl is in the session</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    public void inviteBuddy() throws RemoteException, InterruptedException {
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
        assertFalse(carl.superBot().views().sarosView().isInSession());
        alice.superBot().views().sarosView().selectBuddy(carl.getJID())
            .addToSarosSession();
        carl.superBot().confirmShellSessionInvitationAndShellAddProject(
            PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        carl.superBot().views().sarosView().waitUntilIsInSession();
        assertTrue(carl.superBot().views().sarosView().isInSession());

    }
}
