package de.fu_berlin.inf.dpp.stf.test.editing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.test.util.TestThread;

public class EditDifferentFilesTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Before
    public void beforeEveryTest() throws Exception {
        clearWorkspaces();
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses("foo", "bar", "HelloWorld");

        Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB);

    }

    // alice starts editing HelloWorld class
    // in the meantime bob adds a new class file HelloGermany and start editing
    // it

    @Test
    public void testEditingOnDifferentFiles() throws Exception {
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/src/bar/HelloWorld.java");

        TestThread.Runnable aliceEditTask = new TestThread.Runnable() {
            public void run() throws Exception {
                String textToType = "This is a long, long, long and\n long working test that bla bla bla";

                for (char c : textToType.toCharArray()) {
                    ALICE.remoteBot().editor("HelloWorld.java")
                        .waitUntilIsActive();
                    ALICE.remoteBot().editor("HelloWorld.java")
                        .typeText(String.valueOf(c));
                    ALICE.remoteBot().sleep(100);
                }
            }
        };

        TestThread.Runnable bobEditTask = new TestThread.Runnable() {
            public void run() throws Exception {
                String textToType = "Dieses ist ein sehr, sehr, sehr,\n langer bla bla bla";

                BOB.superBot().views().packageExplorerView()
                    .selectProject("foo").newC().cls("HelloGermany");

                for (char c : textToType.toCharArray()) {
                    BOB.remoteBot().editor("HelloGermany.java")
                        .waitUntilIsActive();
                    BOB.remoteBot().editor("HelloGermany.java")
                        .typeText(String.valueOf(c));
                    BOB.remoteBot().sleep(100);
                }
            }
        };

        TestThread alice = new TestThread(aliceEditTask);
        TestThread bob = new TestThread(bobEditTask);

        alice.start();
        bob.start();
        alice.join();
        bob.join();
        alice.verify();
        bob.verify();

        ALICE.remoteBot().saveAllEditors();
        BOB.remoteBot().saveAllEditors();

        ALICE.remoteBot().sleep(1000);
        String contentAliceHelloWorld = ALICE.superBot().views()
            .packageExplorerView()
            .getFileContent("foo/src/bar/HelloWorld.java");
        String contentBobHelloWorld = BOB.superBot().views()
            .packageExplorerView()
            .getFileContent("foo/src/bar/HelloWorld.java");

        String contentAliceHelloGermany = ALICE.superBot().views()
            .packageExplorerView().getFileContent("foo/src/HelloGermany.java");
        String contentBobHelloWorldGermany = BOB.superBot().views()
            .packageExplorerView().getFileContent("foo/src/HelloGermany.java");

        assertEquals(contentAliceHelloWorld, contentBobHelloWorld);
        assertEquals(contentAliceHelloGermany, contentBobHelloWorldGermany);
    }
}
