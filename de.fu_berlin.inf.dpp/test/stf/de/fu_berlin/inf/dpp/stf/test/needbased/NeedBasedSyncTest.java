package de.fu_berlin.inf.dpp.stf.test.needbased;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.shared.Constants;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class NeedBasedSyncTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {

        select(ALICE, BOB, CARL);
        ALICE.superBot().menuBar().saros().preferences()
            .setNeedBasedActivated(true);

    }

    @Before
    public void enableNeedBased() throws Exception {
        closeAllShells();
        closeAllEditors();
        clearWorkspaces();
    }

    @After
    public void afterEveryTest() throws Exception {
        leaveSessionPeersFirst(ALICE);
    }

    @Test
    public void testNeedBasedSyncInSession() throws Exception {

        Util.createProjectWithEmptyFile("foo", "file1", ALICE);
        ALICE.superBot().internal().createFile("foo", "file2", "bla123");
        ALICE.superBot().internal().createFile("foo", "file3", "bla345bla");

        Util.buildFileSessionConcurrently("foo", new String[] { "file2" },
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file2");

        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file2");

        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();

        CARL.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();

        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .waitUntilIsFollowing();

        CARL.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .waitUntilIsFollowing();

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file2").open();

        ALICE.remoteBot().editor("file2")
            .setTextFromFile("test/resources/stf/lorem.txt");

        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        assertTrue(CARL.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        String contentAliceFile2 = ALICE.superBot().views()
            .packageExplorerView().getFileContent("foo/file2");

        String contentBobFile2 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");

        String contentCarlFile2 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");

        assertEquals(contentAliceFile2, contentBobFile2);
        assertEquals(contentAliceFile2, contentCarlFile2);

        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file1"));

        assertFalse(CARL.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file1"));

        ALICE.remoteBot().editor("file2").save();

        BOB.remoteBot().editor("file2").save();

        CARL.remoteBot().editor("file2").save();

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file1").open();

        ALICE.remoteBot().editor("file1").typeText("123456789");

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file1");

        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file1");

        ALICE.remoteBot().editor("file1").save();

        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file1"));

        assertTrue(CARL.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file1"));

        String contentAliceFile1 = ALICE.superBot().views()
            .packageExplorerView().getFileContent("foo/file1");

        ALICE.remoteBot().sleep(1000);

        String contentBobFile1 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file1");

        String contentCarlFile1 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file1");

        assertEquals(contentAliceFile1, contentBobFile1);
        assertEquals(contentAliceFile1, contentCarlFile1);

        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file3"));

        assertFalse(CARL.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file3"));
    }

    @Test
    public void testNeedBasedSyncUI() throws Exception {
        ALICE.superBot().menuBar().saros().preferences().restoreDefaults();

        Util.createProjectWithEmptyFile("foo", "file1", ALICE);
        ALICE.superBot().internal().createFile("foo", "file2", "bla123");
        ALICE.superBot().internal().createFile("foo", "file3", "bla345bla");
        ALICE.superBot().internal().createFile("foo", "file4", "hallihallo");
        ALICE.superBot().internal().createFile("foo", "file5", "main();");

        Util.buildFileSessionConcurrently("foo", new String[] { "file1" },
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file1");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file2").open();

        ALICE.remoteBot().editor("file2").typeText("1");
        ALICE.superBot().confirmShellNeedBased(Constants.NO, false);

        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        ALICE.remoteBot().editor("file2").typeText("a");
        ALICE.superBot().confirmShellNeedBased(Constants.NO, false);

        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        ALICE.remoteBot().editor("file2").typeText("b");
        ALICE.superBot().confirmShellNeedBased(Constants.NO, true);

        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        ALICE.remoteBot().editor("file2").typeText("h");

        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        ALICE.superBot().menuBar().saros().preferences().restoreDefaults();

        ALICE.remoteBot().editor("file2").typeText("n");
        ALICE.superBot().confirmShellNeedBased(Constants.YES, false);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file2");

        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        String contentBF2 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");

        String contentAF2 = ALICE.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");

        assertEquals(contentAF2, contentBF2);

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file3").open();

        ALICE.remoteBot().editor("file3").typeText("n");
        ALICE.superBot().confirmShellNeedBased(Constants.YES, true);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file3");

        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file3"));

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file4").open();

        ALICE.remoteBot().editor("file4").typeText("new try nr 3");

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file4");

        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file4"));

        leaveSessionPeersFirst(ALICE);

        Util.buildFileSessionConcurrently("foo", new String[] { "file1" },
            TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file1");

        ALICE.remoteBot().editor("file2").typeText("123456789");

        String contentBobFile2Before = BOB.superBot().views()
            .packageExplorerView().getFileContent("foo/file2");

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file2");

        BOB.superBot().confirmShellNewSharedFile(Constants.BACKUP);

        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2_BACKUP"));

        String contentBobFile2After = BOB.superBot().views()
            .packageExplorerView().getFileContent("foo/file2_BACKUP");

        assertEquals(contentBobFile2Before, contentBobFile2After);

        BOB.superBot().internal().createFile("foo", "file5", "main();");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file5").open();

        ALICE.remoteBot().editor("file5").typeText("void ");

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file5");

        String contentBobF5 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file5");

        String contentAliceF5 = ALICE.superBot().views().packageExplorerView()
            .getFileContent("foo/file5");

        assertEquals(contentAliceF5, contentBobF5);
    }

    @Test
    public void testNeedBasedSyncInSessionWithBobCarl() throws Exception {

        Util.createProjectWithEmptyFile("foo", "file1", ALICE);
        ALICE.superBot().internal().createFile("foo", "file2", "123");
        ALICE.superBot().internal().createFile("foo", "file3", "321");

        Util.createProjectWithEmptyFile("foo", "file2", BOB);

        Util.createProjectWithEmptyFile("foo", "file3", CARL);

        Util.buildFileSessionConcurrently("foo", new String[] { "file1" },
            TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file1");

        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file1");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file1").open();

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file2").open();

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file3").open();

        BOB.superBot().views().packageExplorerView().selectFile("foo", "file2")
            .open();

        CARL.superBot().views().packageExplorerView()
            .selectFile("foo", "file3").open();

        ALICE.remoteBot().editor("file1").typeText("123456789");

        BOB.remoteBot().editor("file2").typeText("12345");

        CARL.remoteBot().editor("file3").typeText("blabla");

        ALICE.remoteBot().saveAllEditors();
        BOB.remoteBot().saveAllEditors();
        CARL.remoteBot().saveAllEditors();

        String contentAliceFile1 = ALICE.superBot().views()
            .packageExplorerView().getFileContent("foo/file1");

        String contentAliceFile2 = ALICE.superBot().views()
            .packageExplorerView().getFileContent("foo/file2");

        String contentAliceFile3 = ALICE.superBot().views()
            .packageExplorerView().getFileContent("foo/file3");

        ALICE.remoteBot().sleep(1000);

        String contentBobFile1 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file1");

        String contentBobFile2 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");

        String contentCarlFile1 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file1");

        String contentCarlFile3 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file3");

        assertEquals(contentAliceFile1, contentBobFile1);
        assertEquals(contentAliceFile1, contentCarlFile1);
        assertFalse(contentAliceFile2.equals(contentBobFile2));
        assertFalse(contentAliceFile3.equals(contentCarlFile3));

        BOB.remoteBot().editor("file2").closeWithSave();
        CARL.remoteBot().editor("file3").closeWithSave();

        ALICE.remoteBot().editor("file2").typeText("new Text ");
        ALICE.remoteBot().editor("file3").typeText("some new Text ");
        ALICE.remoteBot().sleep(1000);

        BOB.superBot().confirmShellNewSharedFile(Constants.OVERWRITE);
        CARL.superBot().confirmShellNewSharedFile(Constants.OVERWRITE);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file3");
        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file2");

        String contentA3 = ALICE.superBot().views().packageExplorerView()
            .getFileContent("foo/file3");
        String contentB3 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file3");
        String contentC3 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file3");

        assertEquals(contentA3, contentB3);
        assertEquals(contentA3, contentC3);

        BOB.superBot().views().packageExplorerView().selectFile("foo", "file3")
            .open();

        CARL.superBot().views().packageExplorerView()
            .selectFile("foo", "file2").open();

        BOB.remoteBot().editor("file3").typeText("hallo");
        CARL.remoteBot().editor("file2").typeText("du da");

        String contentAlice1 = ALICE.superBot().views().packageExplorerView()
            .getFileContent("foo/file1");
        String contentAlice2 = ALICE.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");
        String contentAlice3 = ALICE.superBot().views().packageExplorerView()
            .getFileContent("foo/file3");

        String contentBob1 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file1");
        String contentBob2 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");
        String contentBob3 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file3");

        String contentCarl1 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file1");
        String contentCarl2 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");
        String contentCarl3 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file3");

        assertEquals(contentAlice1, contentBob1);
        assertEquals(contentAlice2, contentBob2);
        assertEquals(contentAlice3, contentBob3);
        assertEquals(contentAlice1, contentCarl1);
        assertEquals(contentAlice2, contentCarl2);
        assertEquals(contentAlice3, contentCarl3);
    }

    @Test
    public void testNeedBasedSyncDuringSynchronization() throws Exception {

        String[] filesToShare = new String[11];

        filesToShare[0] = "file1";

        Util.createProjectWithEmptyFile("foo", "file1", ALICE);
        ALICE.superBot().internal().createFile("foo", "file2", "bla123");
        ALICE.superBot().internal().createFile("foo", "file3", "bla345bla");

        for (int i = 0; i < 10; i++) {
            ALICE.superBot().internal()
                .createFile("foo", "fileX" + i, 512 * 512, false);
            filesToShare[i + 1] = "fileX" + i;
        }

        Util.buildFileSessionConcurrently("foo", filesToShare,
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilFolderExists("foo");
        CARL.superBot().views().packageExplorerView()
            .waitUntilFolderExists("foo");

        List<String> shellNamesAlice = ALICE.remoteBot().getOpenShellNames();
        for (String string : shellNamesAlice) {
            if (string.startsWith("Sharing project")) {
                IRemoteBotShell shellAlice = ALICE.remoteBot().shell(string);
                shellAlice.activate();
                shellAlice.bot().button(Constants.RUN_IN_BACKGROUND).click();
            }
        }

        IRemoteBotShell shellCarl = CARL.remoteBot().shell(
            Constants.SHELL_MONITOR_PROJECT_SYNCHRONIZATION);
        shellCarl.activate();
        shellCarl.bot().button(Constants.RUN_IN_BACKGROUND).click();

        BOB.remoteBot().activateWorkbench();
        IRemoteBotShell shellBob = BOB.remoteBot().shell(
            Constants.SHELL_MONITOR_PROJECT_SYNCHRONIZATION);
        shellBob.activate();
        shellBob.bot().button(Constants.RUN_IN_BACKGROUND).click();

        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();

        CARL.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();

        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .waitUntilIsFollowing();

        CARL.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .waitUntilIsFollowing();

        assertTrue(BOB.superBot().views().sarosView().isFollowing());
        assertTrue(CARL.superBot().views().sarosView().isFollowing());

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file2").open();

        ALICE.remoteBot().editor("file2").typeText("Hallo ihr Beiden! ");
        CARL.remoteBot().editor("file2").typeText("Hallo ich bin Carl ");
        BOB.remoteBot().editor("file2").typeText("und ich bin Bob");

        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file1"));
        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file3"));
        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        assertFalse(CARL.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file1"));
        assertFalse(CARL.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file3"));
        assertTrue(CARL.superBot().views().packageExplorerView()
            .selectProject("foo").exists("file2"));

        String contentA2 = ALICE.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");
        String contentB2 = BOB.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");
        String contentC2 = CARL.superBot().views().packageExplorerView()
            .getFileContent("foo/file2");

        assertEquals(contentB2, contentA2);
        assertEquals(contentC2, contentA2);

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "file3").open();

        ALICE.remoteBot().editor("file3").typeText("Hallo ihr Beiden! ");
        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file3");
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/file3");

        CARL.remoteBot().editor("file3").typeText("Hallo ich bin Carl ");
        BOB.remoteBot().editor("file3").typeText("und ich bin Bob");
    }
}
