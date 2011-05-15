package de.fu_berlin.inf.dpp.stf.client.testProject.testsuitToReproduceBugs;

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
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice
            .superBot()
            .views()
            .sarosView()
            .selectNoSessionRunning()
            .shareProjects(PROJECT1, bob.getJID(), dave.getJID(), carl.getJID());
        bob.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        bob.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
        alice.superBot().views().progressView().removeProcess(0);
        bob.remoteBot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        bob.remoteBot().shell(SHELL_INVITATION_CANCELLED).activate();
        bob.remoteBot().shell(SHELL_INVITATION_CANCELLED).close();

        carl.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        carl.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
        carl.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        alice.superBot().views().progressView().removeProcess(1);
        carl.remoteBot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        assertTrue(carl.remoteBot().shell(SHELL_INVITATION_CANCELLED).isActive());

        carl.remoteBot().shell(SHELL_INVITATION_CANCELLED).close();

        dave.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        dave.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
        dave.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);

        // dave.button.clickButton(FINISH);
        alice.superBot().views().progressView().removeProcess(3);
        // FIXME Timeout exception by MAC OS X, the building session under
        // MAS
        // is so fast that the session process is already done after
        // canceling
        // this process, so dave should never get the window
        // "Invitation canceled".
        dave.remoteBot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        assertTrue(dave.remoteBot().shell(SHELL_INVITATION_CANCELLED).isActive());

        dave.remoteBot().shell(SHELL_INVITATION_CANCELLED).close();
    }
}