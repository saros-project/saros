package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotShell;

public class TestMenuSaros extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
    }

    // @After
    // public void runAfterEveryTest() throws RemoteException {
    // deleteAllProjectsByActiveTesters();
    // }

    @Test
    public void testShareProjectsWithBot() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.bot().menu(MENU_SAROS).menu(MENU_SHARE_PROJECTS).click();
        if (!alice.bot().isShellOpen(SHELL_SHARE_PROJECT)) {
            alice.bot().waitUntilShellIsOpen(SHELL_SHARE_PROJECT);
        }
        IRemoteBotShell shell = alice.bot().shell(SHELL_SHARE_PROJECT);
        shell.activate();
        shell.bot().table().getTableItem(PROJECT1).check();
        shell.bot().button(NEXT).click();
        shell.bot().tree().selectTreeItem(bob.getBaseJid()).check();
        shell.bot().button(FINISH).click();
        bob.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        IRemoteBotShell shell2 = bob.bot().shell(SHELL_SESSION_INVITATION);
        shell2.activate();
        shell2.bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        bob.superBot().confirmShellAddProjectUsingWhichProject(PROJECT1,
            TypeOfCreateProject.NEW_PROJECT);
        bob.superBot().views().sessionView().waitUntilIsInSession();
    }

    @Test
    public void testShareProjectsWithSuperBot() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().menuBar().saros()
            .shareProjects(PROJECT1, bob.getJID());
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(PROJECT1,
            TypeOfCreateProject.NEW_PROJECT);
    }

    @Test
    public void testAddBuddy() throws RemoteException {
        alice.bot().menu(MENU_SAROS).menu(MENU_ADD_BUDDY).click();
        alice.superBot().confirmShellAddBuddy(bob.getJID());
    }
}
