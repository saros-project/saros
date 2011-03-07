package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.permutations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotShell;

public class TestParallelInvitationWithTerminationByInvitees extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>Dave (Read-Only Access)</li>
     * <li>Edna (Read-Only Access)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * 
     */

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL,
            TypeOfTester.DAVE, TypeOfTester.EDNA);
        setUpWorkbench();
        setUpSaros();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice invites everyone else simultaneously.</li>
     * <li>Bob cancels the invitation at the beginning.</li>
     * <li>Carl cancels the invitation after receiving the file list, but before
     * the synchronisation starts.</li>
     * <li>Dave cancels the invitation during the synchronisation.</li>
     * <li>Edna accepts the invitation (using a new project).</li>
     * <li>Edna leaves the session.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Alice is notified of the peer canceling the invitation.</li>
     * <li>Alice is notified of Edna joining the session.</li>
     * <li></li>
     * <li>Alice is notified of Edna leaving the session.</li> *
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void parallelInvitationWihtTerminationByInvitees()
        throws IOException, CoreException, InterruptedException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(PROJECT1)
            .shareWith()
            .multipleBuddies(PROJECT1, bob.getJID(), dave.getJID(),
                carl.getJID(), edna.getJID());

        bob.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        RemoteBotShell shell_bob = bob.bot().shell(SHELL_SESSION_INVITATION);
        shell_bob.activate();
        shell_bob.bot().button(CANCEL).click();

        RemoteBotShell shell_alice = alice.bot().shell(SHELL_PROBLEM_OCCURRED);
        shell_alice.waitUntilActive();
        assertTrue(alice.bot().shell(SHELL_PROBLEM_OCCURRED).bot().label(2)
            .getText().matches(bob.getName() + ".*"));
        shell_alice.bot().button(OK).click();

        RemoteBotShell shell_carl = carl.bot().shell(SHELL_SESSION_INVITATION);
        carl.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        shell_carl.activate();
        carl.bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        shell_carl.bot().button(CANCEL).click();

        shell_alice.waitUntilActive();
        assertTrue(alice.bot().shell(SHELL_PROBLEM_OCCURRED).bot().label(2)
            .getText().matches(carl.getName() + ".*"));
        shell_alice.bot().button(OK).click();

        dave.bot().waitUntilShellIsClosed(SHELL_SESSION_INVITATION);
        RemoteBotShell shell_dave = dave.bot().shell(SHELL_SESSION_INVITATION);
        shell_dave.activate();
        dave.bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        shell_dave.bot().button(CANCEL).click();

        shell_alice.waitUntilActive();
        assertTrue(alice.bot().shell(SHELL_PROBLEM_OCCURRED).bot().label(2)
            .getText().matches(dave.getName() + ".*"));
        shell_alice.bot().button(OK).click();

        edna.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        edna.bot().shell(SHELL_SESSION_INVITATION).activate();
        edna.bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        edna.superBot().confirmShellAddProjectWithNewProject(PROJECT1);
        edna.superBot().views().sessionView().leaveSession();
        assertFalse(edna.superBot().views().sessionView().isInSession());
        assertFalse(alice.superBot().views().sessionView()
            .selectParticipant(edna.getJID()).hasReadOnlyAccess());

    }
}
