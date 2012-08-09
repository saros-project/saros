package de.fu_berlin.inf.dpp.stf.test.partialsharing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class ShareFilesFromOneProjectToMultipleRemoteProjectsTest extends
    StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Test
    public void testShareFilesFromOneProjectToMultipleRemoteProjects()
        throws Exception {

        ALICE.superBot().internal().createProject("A");
        ALICE.superBot().internal().createFile("A", "a/a.txt", "");
        ALICE.superBot().internal().createFile("A", "b/b.txt", "");

        Util.buildFileSessionConcurrently("A", new String[] { "a/a.txt" },
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("A/a/a.txt");

        ALICE.superBot().menuBar().saros()
            .addProject("A", new String[] { "b/b.txt" });

        BOB.superBot().confirmShellAddProjectUsingWhichProject("B",
            TypeOfCreateProject.NEW_PROJECT);

        // BUG: project A is now unshared

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("B/b/b.txt");

        BOB.superBot().views().packageExplorerView()
            .selectFile("B", new String[] { "b", "b.txt" }).open();

        BOB.remoteBot().editor("b.txt").waitUntilIsActive();
        BOB.remoteBot().editor("b.txt").typeText("Triple BBB");

        BOB.superBot().views().packageExplorerView()
            .selectFile("A", new String[] { "a", "a.txt" }).open();

        BOB.remoteBot().editor("a.txt").waitUntilIsActive();
        BOB.remoteBot().editor("a.txt").typeText("Triple AAA");

        /*
         * TODO some STF waitXXX methods should fail silently instead of
         * throwing timeout exceptions to distinguish between errors and
         * failures
         */
        Thread.sleep(2000);

        ALICE.superBot().views().packageExplorerView()
            .selectFile("A", new String[] { "b", "b.txt" }).open();
        ALICE.remoteBot().editor("b.txt").waitUntilIsActive();

        ALICE.superBot().views().packageExplorerView()
            .selectFile("A", new String[] { "a", "a.txt" }).open();
        ALICE.remoteBot().editor("a.txt").waitUntilIsActive();

        assertEquals(BOB.remoteBot().editor("a.txt").getText(), ALICE
            .remoteBot().editor("a.txt").getText());

        assertEquals(BOB.remoteBot().editor("b.txt").getText(), ALICE
            .remoteBot().editor("b.txt").getText());
    }
}
