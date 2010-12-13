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

import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.STFTest;

public class TestParallelInvitationWithTerminationByInvitees extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer)</li>
     * <li>Edna (Observer)</li>
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
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice.pEV.shareProject(PROJECT1, bob.getBaseJid(), dave.getBaseJid(),
            carl.getBaseJid(), edna.getBaseJid());

        bob.shell.waitUntilShellOpen(SESSION_INVITATION);
        bob.shell.activateShellWithText(SESSION_INVITATION);
        bob.basic.clickButton(CANCEL);
        alice.pEV.waitUntilWindowProblemOccurredActive();
        assertTrue(alice.pEV.getSecondLabelOfWindowProblemOccurred().matches(
            bob.getName() + ".*"));

        alice.basic.clickButton(OK);

        carl.shell.waitUntilShellOpen(SESSION_INVITATION);
        carl.shell.activateShellWithText(SESSION_INVITATION);
        carl.pEV.confirmFirstPageOfWizardSessionInvitation();
        carl.basic.clickButton(CANCEL);
        alice.pEV.waitUntilWindowProblemOccurredActive();
        assertTrue(alice.pEV.getSecondLabelOfWindowProblemOccurred().matches(
            carl.getName() + ".*"));
        alice.basic.clickButton(OK);

        dave.shell.waitUntilShellOpen(SESSION_INVITATION);
        dave.shell.activateShellWithText(SESSION_INVITATION);
        dave.pEV.confirmFirstPageOfWizardSessionInvitation();
        dave.basic.clickButton(CANCEL);
        alice.pEV.waitUntilWindowProblemOccurredActive();
        assertTrue(alice.pEV.getSecondLabelOfWindowProblemOccurred().matches(
            dave.getName() + ".*"));
        alice.basic.clickButton(OK);

        edna.shell.waitUntilShellOpen(SESSION_INVITATION);
        edna.shell.activateShellWithText(SESSION_INVITATION);
        edna.pEV.confirmWirzardSessionInvitationWithNewProject(PROJECT1);
        edna.sessionV.leaveTheSessionByPeer();
        assertFalse(edna.sessionV.isInSessionGUI());
        assertFalse(alice.sessionV.isObserver(edna.jid));
    }
}
