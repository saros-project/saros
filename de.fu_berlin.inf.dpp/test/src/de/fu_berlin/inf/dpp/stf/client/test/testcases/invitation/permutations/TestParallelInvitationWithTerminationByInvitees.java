package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.permutations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

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
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusican() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            MusicianConfigurationInfos.PORT_ALICE,
            MusicianConfigurationInfos.PORT_BOB,
            MusicianConfigurationInfos.PORT_CARL,
            MusicianConfigurationInfos.PORT_DAVE,
            MusicianConfigurationInfos.PORT_EDNA);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        edna = musicians.get(4);
    }

    /**
     * Closes all opened xmppConnects, popup windows and editor.<br/>
     * Delete all existed projects.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        carl.workbench.resetSaros();
        dave.workbench.resetSaros();
        edna.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * Closes all opened popup windows and editor.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        dave.workbench.resetWorkbench();
        edna.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
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

        bob.pEV.waitUntilWindowSessionInvitationActive();
        bob.basic.clickButton(CANCEL);
        alice.pEV.waitUntilWindowProblemOccurredActive();
        assertTrue(alice.pEV.getSecondLabelOfWindowProblemOccurred().matches(
            bob.getName() + ".*"));

        alice.basic.clickButton(OK);

        carl.pEV.waitUntilWindowSessionInvitationActive();
        carl.pEV.confirmFirstPageOfWizardSessionInvitation();
        carl.basic.clickButton(CANCEL);
        alice.pEV.waitUntilWindowProblemOccurredActive();
        assertTrue(alice.pEV.getSecondLabelOfWindowProblemOccurred().matches(
            carl.getName() + ".*"));
        alice.basic.clickButton(OK);

        dave.pEV.isWIndowSessionInvitationActive();
        dave.pEV.confirmFirstPageOfWizardSessionInvitation();
        dave.basic.clickButton(CANCEL);
        alice.pEV.waitUntilWindowProblemOccurredActive();
        assertTrue(alice.pEV.getSecondLabelOfWindowProblemOccurred().matches(
            dave.getName() + ".*"));
        alice.basic.clickButton(OK);

        edna.pEV.isWIndowSessionInvitationActive();
        edna.pEV.confirmWirzardSessionInvitationWithNewProject(PROJECT1);
        edna.sessionV.leaveTheSessionByPeer();
        assertFalse(edna.sessionV.isInSessionGUI());
        assertFalse(alice.sessionV.isObserver(edna.jid));
    }
}
