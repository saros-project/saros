package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.permutations;

import static org.junit.Assert.assertFalse;
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

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

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
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice.sarosC.shareProject(PROJECT1, bob.getBaseJid(),
            dave.getBaseJid(), carl.getBaseJid(), edna.getBaseJid());

        bob.shell.waitUntilShellOpen(SHELL_SESSION_INVITATION);
        bob.shell.activateShell(SHELL_SESSION_INVITATION);
        bob.button.clickButton(CANCEL);
        alice.sarosC.waitUntilIsShellProblemOccurredActive();
        assertTrue(alice.sarosC.getSecondLabelOfShellProblemOccurred()
            .matches(bob.getName() + ".*"));

        alice.button.clickButton(OK);

        carl.shell.waitUntilShellOpen(SHELL_SESSION_INVITATION);
        carl.shell.activateShell(SHELL_SESSION_INVITATION);
        carl.sarosC.confirmShellSessionnInvitation();
        carl.button.clickButton(CANCEL);
        alice.sarosC.waitUntilIsShellProblemOccurredActive();
        assertTrue(alice.sarosC.getSecondLabelOfShellProblemOccurred()
            .matches(carl.getName() + ".*"));
        alice.button.clickButton(OK);

        dave.shell.waitUntilShellOpen(SHELL_SESSION_INVITATION);
        dave.shell.activateShell(SHELL_SESSION_INVITATION);
        dave.sarosC.confirmShellSessionnInvitation();
        dave.button.clickButton(CANCEL);
        alice.sarosC.waitUntilIsShellProblemOccurredActive();
        assertTrue(alice.sarosC.getSecondLabelOfShellProblemOccurred()
            .matches(dave.getName() + ".*"));
        alice.button.clickButton(OK);

        edna.shell.waitUntilShellOpen(SHELL_SESSION_INVITATION);
        edna.shell.activateShell(SHELL_SESSION_INVITATION);
        edna.sarosC.confirmShellSessionnInvitation();
        edna.sarosC.confirmShellAddProjectWithNewProject(PROJECT1);
        edna.sarosSessionV.leaveTheSessionByPeer();
        assertFalse(edna.sarosSessionV.isInSession());
        assertFalse(alice.sarosSessionV.hasReadOnlyAccessNoGUI(edna.jid));
    }
}
