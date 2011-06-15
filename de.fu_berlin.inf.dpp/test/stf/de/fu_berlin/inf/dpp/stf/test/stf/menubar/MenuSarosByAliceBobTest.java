package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.FINISH;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_ADD_BUDDY;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.NEXT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHARE_PROJECTS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SHARE_PROJECT;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class MenuSarosByAliceBobTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @Override
    @After
    public void tearDown() throws RemoteException {
        announceTestCaseEnd();
        leaveSessionHostFirst(ALICE);
        deleteAllProjectsByActiveTesters();

        // TODO remove this code, because it is a workaround
        // against a bug in Saros Session Management
        for (AbstractTester tester : getCurrentTesters()) {
            tester.remoteBot().sleep(1000);
            if (tester.remoteBot().isShellOpen("Synchronizing")) {
                IRemoteBotShell sync = tester.remoteBot()
                    .shell("Synchronizing");

                sync.confirm("Cancel");
                tester.remoteBot().sleep(1000);
                if (tester.remoteBot().isShellOpen("Problem Occurred")) {
                    IRemoteBotShell problem = tester.remoteBot().shell(
                        "Problem Occurred");
                    problem.confirm("OK");
                }
            }

        }
    }

    @Test
    public void testShareProjectsWithRemoteBot() throws RemoteException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        ALICE.remoteBot().menu(MENU_SAROS).menu(SHARE_PROJECTS).click();
        if (!ALICE.remoteBot().isShellOpen(SHELL_SHARE_PROJECT)) {
            ALICE.remoteBot().waitUntilShellIsOpen(SHELL_SHARE_PROJECT);
        }
        IRemoteBotShell shell = ALICE.remoteBot().shell(SHELL_SHARE_PROJECT);
        shell.activate();
        shell.bot().table().getTableItem(Constants.PROJECT1).check();
        shell.bot().button(NEXT).click();
        shell.bot().tree().selectTreeItem(BOB.getBaseJid()).check();
        shell.bot().button(FINISH).click();
        BOB.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        IRemoteBotShell shell2 = BOB.remoteBot()
            .shell(SHELL_SESSION_INVITATION);
        shell2.activate();
        shell2.bot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);
        BOB.superBot().confirmShellAddProjectUsingWhichProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        BOB.superBot().views().sarosView().waitUntilIsInSession();
    }

    @Test
    public void testShareProjectsWithSuperBot() throws RemoteException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        ALICE.superBot().menuBar().saros()
            .shareProjects(Constants.PROJECT1, BOB.getJID());
        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        BOB.superBot().views().sarosView().waitUntilIsInSession();

    }

    @Test
    public void testAddBuddy() throws RemoteException {
        ALICE.superBot().views().sarosView()
            .connectWith(ALICE.getJID(), ALICE.getPassword());
        ALICE.remoteBot().menu(MENU_SAROS).menu(MENU_ADD_BUDDY).click();
        ALICE.superBot().confirmShellAddBuddy(BOB.getJID());
    }

    @Test
    public void addProjects() throws RemoteException {
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);

        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT2, Constants.PKG1,
                Constants.CLS1);

        ALICE.superBot().menuBar().saros().addProjects(Constants.PROJECT2);
        BOB.superBot().confirmShellAddProjects(Constants.PROJECT2,
            TypeOfCreateProject.NEW_PROJECT);

        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassExists(Constants.PROJECT2, Constants.PKG1,
                Constants.CLS1);
    }

    @Test
    public void stopSession() throws RemoteException {
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);

        ALICE.superBot().views().sarosView().waitUntilIsInSession();
        BOB.superBot().views().sarosView().waitUntilIsInSession();

        ALICE.superBot().views().sarosView().selectBuddies().stopSarosSession();

        ALICE.superBot().views().sarosView().waitUntilIsNotInSession();
        BOB.superBot().views().sarosView().waitUntilIsNotInSession();
    }
}
