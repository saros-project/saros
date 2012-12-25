package de.fu_berlin.inf.dpp.stf.test.consistency;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertEquals;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import de.fu_berlin.inf.dpp.test.util.TestThread;

public class EditDuringNonHostInvitationTest extends StfTestCase {

    private TestThread bobIsWriting, aliceIsWriting;

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    @Before
    public void tidyUp() throws Exception {
        closeAllShells();
        closeAllEditors();
        clearWorkspaces();
    }

    @After
    public void runAfterEveryTest() throws Exception {
        if (bobIsWriting != null)
            killThread(bobIsWriting);

        if (aliceIsWriting != null)
            killThread(aliceIsWriting);

        leaveSessionHostFirst(ALICE);

    }

    @Test
    @Ignore("Non-Host Invitation is currently deactivated")
    public void testEditDuringInvitationNonHostInvites() throws Exception {

        Util.setUpSessionWithJavaProjectAndClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);

        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        BOB.superBot().views().sarosView().selectBuddy(CARL.getJID())
            .addToSarosSession();

        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();

        CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

        aliceIsWriting = new TestThread(new TestThread.Runnable() {

            @Override
            public void run() throws Exception {
                for (int i = 0; i < 20; i++)
                    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
                        .typeText("FooBar");

            }
        });

        aliceIsWriting.start();

        CARL.superBot()
            .confirmShellAddProjectWithNewProject(Constants.PROJECT1);

        aliceIsWriting.join();
        aliceIsWriting.verify();

        CARL.superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        CARL.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();

        BOB.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();

        String textByBob = BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(textByBob);

        String textByAlice = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();

        try {
            CARL.remoteBot().editor(Constants.CLS1_SUFFIX)
                .waitUntilIsTextSame(textByBob);
        } catch (TimeoutException e) {
            //
        }
        String textByCarl = CARL.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();

        assertEquals(textByBob, textByAlice);
        assertEquals(textByBob, textByCarl);
    }

    private void killThread(Thread thread) throws InterruptedException {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread.join(10000);
        }
    }
}
