package de.fu_berlin.inf.dpp.stf.test.consistency;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.tester.SarosTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import de.fu_berlin.inf.dpp.test.util.TestThread;

public class EditDuringInvitationStressTest extends StfTestCase {

    private TestThread bobIsWriting;
    private final String[] CLASS_NAMES = { "ClassA", "ClassB", "ClassC",
        "ClassD", "ClassE" };

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    @Before
    public void beforeEveryTest() throws Exception {
        tidyUp();
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                CLASS_NAMES);

        Util.buildSessionSequentially(Constants.PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);
    }

    private void tidyUp() throws Exception {
        closeAllShells();
        closeAllEditors();
        clearWorkspaces();
    }

    @After
    public void runAfterEveryTest() throws Exception {
        if (bobIsWriting != null)
            killThread(bobIsWriting);

        leaveSessionHostFirst(ALICE);
    }

    private void killThread(Thread thread) throws InterruptedException {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread.join(10000);
        }
    }

    @Test
    public void testEditMultipleClassesDuringInvitation() throws Exception {

        BOB.superBot().views().packageExplorerView()
            .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, "ClassA");

        openTestClasses(BOB);

        ALICE.superBot().views().sarosView().selectBuddy(CARL.getJID())
            .addToSarosSession();

        CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

        bobIsWriting = new TestThread(new TestThread.Runnable() {

            private final Random random = new Random();

            @Override
            public void run() throws Exception {
                for (int i = 0; i < 100; i++) {
                    String nextClass = CLASS_NAMES[i % 5] + ".java";
                    BOB.remoteBot().editor(nextClass)
                        .typeText(String.valueOf(generateCharacter()));
                }
            }

            // ([A-Z] union [a-z])
            private char generateCharacter() {
                return (char) (((random.nextInt() & 0x7FFFFFFF) % 26) | 0x41 | ((random
                    .nextInt() & 1) << 5));
            }
        });

        bobIsWriting.start();

        CARL.superBot()
            .confirmShellAddProjectWithNewProject(Constants.PROJECT1);

        bobIsWriting.join();
        bobIsWriting.verify();

        CARL.superBot().views().packageExplorerView()
            .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, "ClassA");

        openTestClasses(ALICE);
        openTestClasses(CARL);

        compareFilesOfTesters(BOB, ALICE);
        compareFilesOfTesters(BOB, CARL);
    }

    private void openTestClasses(SarosTester tester) throws RemoteException {
        for (String className : CLASS_NAMES) {
            tester.superBot().views().packageExplorerView()
                .selectClass(Constants.PROJECT1, Constants.PKG1, className)
                .open();
        }
    }

    private void compareFilesOfTesters(SarosTester testerA, SarosTester testerB)
        throws RemoteException {
        for (String className : CLASS_NAMES) {
            String textByA = testerA.remoteBot().editor(className + ".java")
                .getText();
            String textByB = testerB.remoteBot().editor(className + ".java")
                .getText();
            assertEquals(textByA, textByB);
        }
    }
}
