package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestSessionAliceBobCarl extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        reBuildSession(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetWriteAccess(alice, bob);
        resetFollowModeSequentially(alice, bob);
    }

    @Test
    public void inviteUsersInSession() throws RemoteException,
        InterruptedException {
        bob.superBot().views().sarosView().leaveSession();
        bob.superBot().views().packageExplorerView().selectProject(PROJECT1)
            .delete();
        assertFalse(bob.superBot().views().sarosView().isInSession());
        inviteBuddies(PROJECT1, TypeOfCreateProject.NEW_PROJECT, alice, bob,
            carl);
        assertTrue(carl.superBot().views().sarosView().isInSession());
        assertTrue(bob.superBot().views().sarosView().isInSession());
    }

}
