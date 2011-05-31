package de.fu_berlin.inf.dpp.stf.test.reproducebugs;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.DAVE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_INVITATION_CANCELLED;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;

public class ParallelInvitationWithTerminationByHostTest extends StfTestCase {

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
        initTesters(ALICE, BOB, CARL, DAVE);
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
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        /*
         * build session with BOB, CARL and DAVE simultaneously
         */
        ALICE
            .superBot()
            .views()
            .sarosView()
            .selectNoSessionRunning()
            .shareProjects(Constants.PROJECT1, BOB.getJID(), DAVE.getJID(),
                CARL.getJID());
        BOB.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        BOB.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
        ALICE.superBot().views().progressView().removeProcess(0);
        BOB.remoteBot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        BOB.remoteBot().shell(SHELL_INVITATION_CANCELLED).activate();
        BOB.remoteBot().shell(SHELL_INVITATION_CANCELLED).close();

        CARL.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        CARL.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
        CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);
        ALICE.superBot().views().progressView().removeProcess(1);
        CARL.remoteBot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        assertTrue(CARL.remoteBot().shell(SHELL_INVITATION_CANCELLED)
            .isActive());

        CARL.remoteBot().shell(SHELL_INVITATION_CANCELLED).close();

        DAVE.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        DAVE.remoteBot().shell(SHELL_SESSION_INVITATION).activate();
        DAVE.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

        // DAVE.button.clickButton(FINISH);
        ALICE.superBot().views().progressView().removeProcess(3);
        // FIXME Timeout exception by MAC OS X, the building session under
        // MAS
        // is so fast that the session process is already done after
        // canceling
        // this process, so DAVE should never get the window
        // "Invitation canceled".
        DAVE.remoteBot().waitLongUntilShellIsOpen(SHELL_INVITATION_CANCELLED);
        assertTrue(DAVE.remoteBot().shell(SHELL_INVITATION_CANCELLED)
            .isActive());

        DAVE.remoteBot().shell(SHELL_INVITATION_CANCELLED).close();
    }
}