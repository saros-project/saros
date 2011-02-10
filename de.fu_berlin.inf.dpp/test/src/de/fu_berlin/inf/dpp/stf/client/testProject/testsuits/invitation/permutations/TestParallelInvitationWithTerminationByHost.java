package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.permutations;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestParallelInvitationWithTerminationByHost extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>Dave (Read-Only Access)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL,
            TypeOfTester.DAVE);
        setUpWorkbench();
        setUpSaros();
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
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice.sarosC.shareProject(VIEW_PACKAGE_EXPLORER, PROJECT1,
            bob.getBaseJid(), dave.getBaseJid(), carl.getBaseJid());

        bob.shell.waitUntilShellOpen(SHELL_SESSION_INVITATION);
        bob.shell.activateShell(SHELL_SESSION_INVITATION);
        alice.progressV.removeProcess(0);
        bob.shell.waitUntilShellOpen(SHELL_INVITATION_CANCELLED);
        bob.shell.activateShell(SHELL_INVITATION_CANCELLED);
        bob.sarosC.closeShellInvitationCancelled();

        carl.shell.waitUntilShellOpen(SHELL_SESSION_INVITATION);
        carl.shell.activateShell(SHELL_SESSION_INVITATION);
        carl.sarosC.confirmShellSessionnInvitation();
        alice.progressV.removeProcess(0);
        carl.sarosC.waitUntilIsShellInvitationCnacelledActive();
        assertTrue(carl.sarosC.isShellInvitationCancelledActive());
        carl.sarosC.closeShellInvitationCancelled();

        dave.shell.waitUntilShellOpen(SHELL_SESSION_INVITATION);
        dave.shell.activateShell(SHELL_SESSION_INVITATION);
        dave.sarosC.confirmShellSessionnInvitation();

        // dave.button.clickButton(FINISH);
        alice.progressV.removeProcess(0);
        // FIXME Timeout exception by MAC OS X, the building session under
        // MAS
        // is so fast that the session process is already done after
        // canceling
        // this process, so dave should never get the window
        // "Invitation canceled".
        dave.sarosC.waitUntilIsShellInvitationCnacelledActive();
        assertTrue(dave.sarosC.isShellInvitationCancelledActive());
        dave.sarosC.closeShellInvitationCancelled();

    }
}
