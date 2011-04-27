package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestMenuSarosByAliceBobCarl extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @Test
    public void inviteUsersInSession() throws RemoteException,
        InterruptedException {
        assertFalse(carl.superBot().views().sarosView().isInSession());
        inviteBuddies(PROJECT1, TypeOfCreateProject.NEW_PROJECT, alice, carl);
        assertTrue(carl.superBot().views().sarosView().isInSession());
    }
}