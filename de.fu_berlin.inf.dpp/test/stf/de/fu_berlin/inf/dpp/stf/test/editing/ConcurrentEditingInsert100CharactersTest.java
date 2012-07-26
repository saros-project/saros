package de.fu_berlin.inf.dpp.stf.test.editing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.test.util.TestThread;

/**
 * - ALICE, BOB and CARL edit readme.txt
 * 
 * - Every Buddy has 100 key strokes.
 * 
 * - the test waits 3 seconds
 * 
 * - the texts of the buddies will be compared
 */

public class ConcurrentEditingInsert100CharactersTest extends StfTestCase {
    private TestThread aliceEditTaskThread;
    public volatile static int A_ALICE = 0;

    private TestThread bobEditTaskThread;
    public volatile static int A_BOB = 0;

    private TestThread carlEditTaskThread;
    public volatile static int A_CARL = 0;

    /*
     * Set the limit of keystrokes that every buddy has to make during this
     * test.
     */
    private final static int LIMIT = 100;

    @BeforeClass
    public static void selectTesters() throws Exception {
        resetDefaultAccount();
        select(ALICE, BOB, CARL);
    }

    @Before
    public void runBeforeEveryTest() throws Exception {
        clearWorkspaces();
        resetDefaultAccount();
    }

    @After
    public void runAfterEveryTest() throws Exception {
        if (aliceEditTaskThread != null && aliceEditTaskThread.isAlive()) {
            aliceEditTaskThread.interrupt();
            aliceEditTaskThread.join(10000);
        }
        if (bobEditTaskThread != null && bobEditTaskThread.isAlive()) {
            bobEditTaskThread.interrupt();
            bobEditTaskThread.join(10000);
        }
        if (carlEditTaskThread != null && carlEditTaskThread.isAlive()) {
            carlEditTaskThread.interrupt();
            carlEditTaskThread.join(10000);
        }

        leaveSessionHostFirst(ALICE);
        ALICE.remoteBot().sleep(1000);
        BOB.remoteBot().sleep(1000);
        CARL.remoteBot().sleep(1000);
    }

    /**
     * Three Buddies (each one in a dedicated thread) insert text in the same
     * file on the same line (Line 1).
     * 
     * @throws Exception
     */
    @Test
    public void ThreeInsert() throws Exception {

        ALICE.superBot().internal().createProject("foo");
        ALICE
            .superBot()
            .internal()
            .createFile("foo", "readme.txt",
                "\nVerbesserung des algorithmischen Kerns, Gleichzeitiges Editieren\n");

        ALICE.superBot().menuBar().saros()
            .shareProjects("foo", Util.getJID(BOB, CARL));

        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();

        joinSessionTasks.add(new Callable<Void>() {
            public Void call() throws Exception {
                BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
                    "foo", TypeOfCreateProject.NEW_PROJECT);
                return null;
            }
        });

        joinSessionTasks.add(new Callable<Void>() {
            public Void call() throws Exception {
                CARL.remoteBot().sleep(1000);
                CARL.superBot()
                    .confirmShellSessionInvitationAndShellAddProject("foo",
                        TypeOfCreateProject.NEW_PROJECT);
                return null;
            }
        });

        Util.workAll(joinSessionTasks);

        A_ALICE = 0;
        A_BOB = 0;
        A_CARL = 0;
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();

        BOB.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

        /*
         * The Alice-Thread insert the lower case characters
         */
        TestThread.Runnable aliceEditTask = new TestThread.Runnable() {
            public void run() throws Exception {
                int i = 97;
                while ((!Thread.currentThread().isInterrupted())
                    && (A_ALICE < LIMIT)) {
                    if (i >= 123) {
                        ALICE.remoteBot().editor("readme.txt").typeText("\n");
                        i = 96;
                    } else {
                        ALICE.remoteBot().editor("readme.txt")
                            .typeText("" + ((char) i) + "");
                        ALICE.remoteBot().editor("readme.txt").navigateTo(1, 1);

                    }
                    ++A_ALICE;
                    i++;
                }
            }
        };

        /*
         * The Carl-Thread insert numbers
         */
        TestThread.Runnable carlEditTask = new TestThread.Runnable() {
            public void run() throws Exception {
                int i = 48;
                while ((!Thread.currentThread().isInterrupted())
                    && (A_CARL < LIMIT)) {
                    if (i >= 58) {
                        CARL.remoteBot().editor("readme.txt").typeText("\n");
                        i = 47;
                    } else {
                        CARL.remoteBot().editor("readme.txt")
                            .typeText("" + ((char) i) + "");
                        CARL.remoteBot().editor("readme.txt").navigateTo(1, 1);

                    }
                    ++A_CARL;
                    i++;
                }
            }
        };

        /*
         * The Bob-Thread insert the upper case characters
         */
        TestThread.Runnable bobEditTask = new TestThread.Runnable() {
            public void run() throws Exception {
                int i = 65;
                while ((!Thread.currentThread().isInterrupted())
                    && (A_BOB < LIMIT)) {
                    if (i >= 91) {
                        BOB.remoteBot().editor("readme.txt").typeText("\n");
                        i = 64;
                    } else {
                        BOB.remoteBot().editor("readme.txt")
                            .typeText("" + ((char) i) + "");
                        BOB.remoteBot().editor("readme.txt").navigateTo(1, 1);
                    }
                    ++A_BOB;
                    i++;
                }
            }
        };

        aliceEditTaskThread = new TestThread(aliceEditTask);
        aliceEditTaskThread.start();

        bobEditTaskThread = new TestThread(bobEditTask);
        bobEditTaskThread.start();

        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");

        CARL.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        CARL.remoteBot().editor("readme.txt").waitUntilIsActive();

        carlEditTaskThread = new TestThread(carlEditTask);
        carlEditTaskThread.start();

        /*
         * Here, it's important, that the test waits until every user makes the
         * limit count of keystrokes.
         */
        while ((A_ALICE < LIMIT) || (A_BOB < LIMIT) || (A_CARL < LIMIT))
            aliceEditTaskThread.join(100);

        aliceEditTaskThread.interrupt();
        aliceEditTaskThread.join(10000);
        aliceEditTaskThread.verify();

        bobEditTaskThread.interrupt();
        bobEditTaskThread.join(10000);
        bobEditTaskThread.verify();

        carlEditTaskThread.interrupt();
        carlEditTaskThread.join(10000);
        carlEditTaskThread.verify();

        ALICE.remoteBot().sleep(1000);
        BOB.remoteBot().sleep(1000);
        CARL.remoteBot().sleep(1000);

        String aliceText = ALICE.remoteBot().editor("readme.txt").getText();
        String bobText = BOB.remoteBot().editor("readme.txt").getText();
        String carlText = CARL.remoteBot().editor("readme.txt").getText();

        ALICE.remoteBot().editor("readme.txt").closeWithoutSave();
        ALICE.remoteBot().waitUntilEditorClosed("readme.txt");

        BOB.remoteBot().editor("readme.txt").closeWithoutSave();
        BOB.remoteBot().waitUntilEditorClosed("readme.txt");

        CARL.remoteBot().editor("readme.txt").closeWithoutSave();
        CARL.remoteBot().waitUntilEditorClosed("readme.txt");

        assertEquals(aliceText.length(), bobText.length());
        assertEquals(carlText.length(), bobText.length());
        assertEquals(bobText, carlText);
        assertEquals(aliceText, bobText);
    }
}
