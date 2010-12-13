package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.permutations;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.STFTest;

public class TestParallelInvitationWithTerminationByHost extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL,
            TypeOfTester.DAVE);
        setUpWorkbenchs();
        setUpSaros();
    }

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() {
        //
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice invites everyone else simultaneously.</li>
     * <li>Alice opens the Progress View and cancels Bob's invitation before Bob
     * accepts.</li>
     * <li>Carl accepts the invitation but does not choose a target project.</li>
     * <li>Alice opens the Progress View and cancels Carl's invitation before
     * Carl accepts</li>
     * <li>Dave accepts the invitation and chooses a target project.</li>
     * <li>Alice opens the Progress View and cancels Dave 's invitation during
     * the synchronisation.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>Bob is notified of Alice's canceling the invitation.</li>
     * <li></li>
     * <li>Carl is notified of Alice's canceling the invitation.</li>
     * <li></li>
     * <li>Dave is notified of Alice's canceling the invitation.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void parallelInvitationWithTerminationByHost() throws IOException,
        CoreException, InterruptedException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice.pEV.shareProject(PROJECT1, bob.getBaseJid(), dave.getBaseJid(),
            carl.getBaseJid());

        bob.shell.waitUntilShellOpen(SESSION_INVITATION);
        bob.shell.activateShellWithText(SESSION_INVITATION);
        alice.progressV.removeProcess(0);
        bob.shell.waitUntilShellOpen(INVITATIONCANCELLED);
        bob.shell.activateShellWithText(INVITATIONCANCELLED);
        bob.pEV.closeWindowInvitationCancelled();

        carl.shell.waitUntilShellOpen(SESSION_INVITATION);
        carl.shell.activateShellWithText(SESSION_INVITATION);
        carl.pEV.confirmFirstPageOfWizardSessionInvitation();
        alice.progressV.removeProcess(0);
        carl.pEV.waitUntilWindowInvitationCnacelledActive();
        assertTrue(carl.pEV.isWindowInvitationCancelledActive());
        carl.pEV.closeWindowInvitationCancelled();

        dave.shell.waitUntilShellOpen(SESSION_INVITATION);
        dave.shell.activateShellWithText(SESSION_INVITATION);
        dave.pEV.confirmFirstPageOfWizardSessionInvitation();

        dave.basic.clickButton(FINISH);
        alice.progressV.removeProcess(0);
        // FIXME Timeout exception by MAC OS X, the building session under MAS
        // is sofast that the session process is already done after canceling
        // this process, so dave should never get the window
        // "Invitation canceled".
        dave.pEV.waitUntilWindowInvitationCnacelledActive();
        assertTrue(dave.pEV.isWindowInvitationCancelledActive());
        dave.pEV.closeWindowInvitationCancelled();

    }
}
