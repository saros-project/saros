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
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;

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
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice
            .sarosBot()
            .packageExplorerView()
            .saros()
            .shareProject(VIEW_PACKAGE_EXPLORER, PROJECT1, bob.getBaseJid(),
                dave.getBaseJid(), carl.getBaseJid(), edna.getBaseJid());

        bob.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        STFBotShell shell_bob = bob.bot().shell(SHELL_SESSION_INVITATION);
        shell_bob.activate();
        shell_bob.bot().button(CANCEL).click();

        STFBotShell shell_alice = alice.bot().shell(SHELL_PROBLEM_OCCURRED);
        shell_alice.waitUntilActive();
        assertTrue(alice.sarosBot().packageExplorerView().saros()
            .getSecondLabelOfShellProblemOccurred()
            .matches(bob.getName() + ".*"));
        shell_alice.bot().button(OK).click();

        STFBotShell shell_carl = carl.bot().shell(SHELL_SESSION_INVITATION);
        carl.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        shell_carl.activate();
        carl.sarosBot().packageExplorerView().saros()
            .confirmShellSessionnInvitation();
        shell_carl.bot().button(CANCEL).click();

        shell_alice.waitUntilActive();
        assertTrue(alice.sarosBot().packageExplorerView().saros()
            .getSecondLabelOfShellProblemOccurred()
            .matches(carl.getName() + ".*"));
        shell_alice.bot().button(OK).click();

        dave.bot().waitsUntilShellIsClosed(SHELL_SESSION_INVITATION);
        STFBotShell shell_dave = dave.bot().shell(SHELL_SESSION_INVITATION);
        shell_dave.activate();
        dave.sarosBot().packageExplorerView().saros()
            .confirmShellSessionnInvitation();
        shell_dave.bot().button(CANCEL).click();

        shell_alice.waitUntilActive();
        assertTrue(alice.sarosBot().packageExplorerView().saros()
            .getSecondLabelOfShellProblemOccurred()
            .matches(dave.getName() + ".*"));
        shell_alice.bot().button(OK).click();

        edna.bot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
        edna.bot().shell(SHELL_SESSION_INVITATION).activate();
        edna.sarosBot().packageExplorerView().saros()
            .confirmShellSessionnInvitation();
        edna.sarosBot().packageExplorerView().saros()
            .confirmShellAddProjectWithNewProject(PROJECT1);
        edna.sarosBot().sessionView().leaveTheSessionByPeer();
        assertFalse(edna.sarosBot().sessionView().isInSession());
        assertFalse(alice.sarosBot().sessionView()
            .hasReadOnlyAccessNoGUI(edna.jid));
    }
}
