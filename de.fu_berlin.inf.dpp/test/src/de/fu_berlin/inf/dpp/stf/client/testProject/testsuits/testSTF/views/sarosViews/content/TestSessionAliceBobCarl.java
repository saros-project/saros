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
    public void inviteUsersInSession() throws RemoteException {
        bob.superBot().views().sarosView().leaveSession();
        bob.superBot().views().packageExplorerView().selectProject(PROJECT1)
            .delete();
        assertFalse(bob.superBot().views().sarosView().isInSession());
        alice.superBot().views().sarosView().selectSession()
            .addBuddies(bob.getBaseJid(), carl.getBaseJid());
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        carl.superBot().confirmShellSessionInvitationAndShellAddProject(
            PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        assertTrue(carl.superBot().views().sarosView().isInSession());
        assertTrue(bob.superBot().views().sarosView().isInSession());
    }

}
