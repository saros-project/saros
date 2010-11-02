package de.fu_berlin.inf.dpp.stf.client.test.invitation.permutations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.SarosConstant;

public class TestParallelInvitationWithTerminationByInvitees {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;
    private static Musician dave;
    private static Musician edna;

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
            BotConfiguration.PORT_ALICE, BotConfiguration.PORT_BOB,
            BotConfiguration.PORT_CARL, BotConfiguration.PORT_DAVE,
            BotConfiguration.PORT_EDNA);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        edna = musicians.get(4);

    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        dave.bot.resetSaros();
        edna.bot.resetSaros();
        alice.bot.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        dave.bot.resetWorkbench();
        edna.bot.resetWorkbench();
        alice.bot.resetWorkbench();
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
    public void testExistDirtyFlagByDaveAndEdnaDuringAlicMakeChange()
        throws IOException, CoreException, InterruptedException {
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);

        /*
         * build session with bob, carl and dave simultaneously
         */
        List<String> peersName = new LinkedList<String>();
        peersName.add(bob.getPlainJid());
        peersName.add(dave.getPlainJid());
        peersName.add(carl.getPlainJid());
        peersName.add(edna.getPlainJid());

        alice.bot.shareProject(PROJECT, peersName);

        bob.bot.waitUntilShellActive("Session Invitation");
        bob.bot.clickButton(SarosConstant.BUTTON_CANCEL);
        alice.bot.waitUntilShellActive("Problem Occurred");
        assertTrue(alice.bot.getSecondLabelOfProblemOccurredWindow().matches(
            bob.getName() + ".*"));
        alice.bot.clickButton(SarosConstant.BUTTON_OK);

        carl.bot.waitUntilShellActive("Session Invitation");
        carl.window.confirmSessionInvitationWindowStep1();
        carl.bot.clickButton(SarosConstant.BUTTON_CANCEL);
        alice.bot.waitUntilShellActive("Problem Occurred");
        assertTrue(alice.bot.getSecondLabelOfProblemOccurredWindow().matches(
            carl.getName() + ".*"));
        alice.bot.clickButton(SarosConstant.BUTTON_OK);

        dave.bot.waitUntilShellActive("Session Invitation");
        dave.window.confirmSessionInvitationWindowStep1();
        // dave.bot.clickButton(SarosConstant.BUTTON_FINISH);
        dave.bot.clickButton(SarosConstant.BUTTON_CANCEL);
        alice.bot.waitUntilShellActive("Problem Occurred");
        assertTrue(alice.bot.getSecondLabelOfProblemOccurredWindow().matches(
            dave.getName() + ".*"));
        alice.bot.clickButton(SarosConstant.BUTTON_OK);

        edna.bot.waitUntilShellActive("Session Invitation");
        edna.window.confirmSessionInvitationWindowStep1();
        edna.window.confirmSessionInvitationWindowStep2UsingNewproject(PROJECT);
        edna.bot.leaveSessionByPeer();
        assertFalse(alice.state.isDriver(edna.jid));
    }
}
