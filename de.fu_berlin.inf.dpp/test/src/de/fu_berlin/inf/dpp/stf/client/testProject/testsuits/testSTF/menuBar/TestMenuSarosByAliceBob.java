package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import static org.junit.Assert.assertFalse;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;

public class TestMenuSarosByAliceBob extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @After
    public void runAfterEveryTest() throws RemoteException,
        InterruptedException {
        leaveSessionHostFirst(alice);
        deleteAllProjectsByActiveTesters();
    }

    @Test
    public void testShareProjectsWithBot() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.remoteBot().menu(MENU_SAROS).menu(SHARE_PROJECTS).click();
        if (!alice.remoteBot().isShellOpen(SHELL_SHARE_PROJECT)) {
            alice.remoteBot().waitUntilShellIsOpen(SHELL_SHARE_PROJECT);
        }
        IRemoteBotShell shell = alice.remoteBot().shell(SHELL_SHARE_PROJECT);
        shell.activate();
        shell.bot().table().getTableItem(PROJECT1).check();
        shell.bot().button(NEXT).click();
        shell.bot().tree().selectTreeItem(bob.getBaseJid()).check();
        shell.bot().button(FINISH).click();
        bob.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        IRemoteBotShell shell2 = bob.remoteBot().shell(SHELL_SESSION_INVITATION);
        shell2.activate();
        shell2.bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        bob.superBot().confirmShellAddProjectUsingWhichProject(PROJECT1,
            TypeOfCreateProject.NEW_PROJECT);
        bob.superBot().views().sarosView().waitUntilIsInSession();
    }

    @Test
    public void testShareProjectsWithSuperBot() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().menuBar().saros()
            .shareProjects(PROJECT1, bob.getJID());
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            PROJECT1, TypeOfCreateProject.NEW_PROJECT);
    }

    @Test
    public void testAddBuddy() throws RemoteException {
        alice.remoteBot().menu(MENU_SAROS).menu(MENU_ADD_BUDDY).click();
        alice.superBot().confirmShellAddBuddy(bob.getJID());
    }

    @Test
    public void addProjects() throws RemoteException, InterruptedException {
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT2);
        alice.superBot().menuBar().saros().addProjects(PROJECT2);
        bob.superBot().confirmShellAddProjects(PROJECT2,
            TypeOfCreateProject.NEW_PROJECT);
    }

    @Test
    public void stopSession() throws RemoteException, InterruptedException {
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
        alice.superBot().views().sarosView().selectBuddies().stopSarosSession();
        assertFalse(alice.superBot().views().sarosView().isInSession());
    }
}
