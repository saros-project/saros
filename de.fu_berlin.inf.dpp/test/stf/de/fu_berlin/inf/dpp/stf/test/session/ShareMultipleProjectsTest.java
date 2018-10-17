package de.fu_berlin.inf.dpp.stf.test.session;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ADD_PROJECTS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.CANCEL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_ADD_PROJECTS_TO_SESSION;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class ShareMultipleProjectsTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    @Before
    public void beforeEveryTest() throws Exception {

        clearWorkspaces();
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo", "bar", "HelloAlice");

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo1", "bar", "HelloBob");

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo2", "bar", "HelloCarl");
    }

    @After
    public void afterEveryTest() throws Exception {
        leaveSessionHostFirst(ALICE);
    }

    @Test
    public void testShareMultipleWithBobAndCarlSequencetially()
        throws Exception {

        Util.buildSessionConcurrently("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo", "bar", "HelloAlice");

        CARL.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo", "bar", "HelloAlice");

        Util.addProjectToSessionSequentially("foo1",
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo1", "bar", "HelloBob");

        CARL.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo1", "bar", "HelloBob");

        Util.addProjectToSessionSequentially("foo2",
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo2", "bar", "HelloCarl");

        CARL.superBot().views().packageExplorerView()
            .waitUntilClassExists("foo2", "bar", "HelloCarl");

    }

    @Test
    public void testShareSameProjectTwice() throws Exception {

        Util.buildSessionConcurrently("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB, CARL);

        ALICE.remoteBot().activateWorkbench();
        ALICE.remoteBot().menu(MENU_SAROS).menu(ADD_PROJECTS).click();

        IRemoteBotShell shell = ALICE.remoteBot().shell(
            SHELL_ADD_PROJECTS_TO_SESSION);
        shell.activate();
        Thread.sleep(1000);

        List<String> items = shell.bot().tree().getTextOfItems();

        shell.bot().button(CANCEL).click();
        Thread.sleep(1000);
        shell.waitShortUntilIsClosed();

        for (String item : items)
            assertFalse("project foo is still marked as shareable",
                item.equals("foo"));
    }

}
