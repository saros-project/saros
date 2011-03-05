package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.contextMenu;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestContextMenuShareWith extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testShareWithMultipleBuddies() throws RemoteException {
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.sarosBot().views().packageExplorerView()
            .selectJavaProject(PROJECT1).shareWith().buddy(bob.getJID());
        bob.sarosBot().confirmShellSessionInvitationAndShellAddProject(PROJECT1,
            TypeOfCreateProject.NEW_PROJECT);
    }
}
