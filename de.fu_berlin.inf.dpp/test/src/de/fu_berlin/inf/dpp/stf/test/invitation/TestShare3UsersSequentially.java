package de.fu_berlin.inf.dpp.stf.test.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestShare3UsersSequentially {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static Musician carl = InitMusician.newCarl();
    private static Musician alice = InitMusician.newAlice();
    private static Musician bob = InitMusician.newBob();

    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        carl = InitMusician.newCarl();
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        carl.bot.resetSaros();
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        carl.bot.resetWorkbench();
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @Test
    public void testShareProject3UsersSequentially() throws RemoteException {
        List<String> musicians = new LinkedList<String>();
        musicians.add(carl.getPlainJid());
        musicians.add(bob.getPlainJid());

        // alice.bot.shareProjectParallel(PROJECT,
        // musicians);
        alice.bot.clickCMShareProjectInPEView(PROJECT);
        alice.bot
            .confirmInvitationWindow(carl.getPlainJid(), bob.getPlainJid());
        carl.bot.confirmSessionInvitationWizard(alice.getPlainJid(), PROJECT);
        // FIXME if this times out, cancel the invitation!
        bob.bot.confirmSessionInvitationWizard(alice.getPlainJid(), PROJECT);

        assertTrue(carl.state.isParticipant(carl.jid));
        assertTrue(carl.state.isObserver(carl.jid));
        assertTrue(carl.state.isParticipant(bob.jid));
        assertTrue(carl.state.isObserver(bob.jid));
        assertTrue(carl.state.isParticipant(alice.jid));
        assertTrue(carl.state.isDriver(alice.jid));

        assertTrue(bob.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
        assertTrue(bob.state.isParticipant(carl.jid));
        assertTrue(bob.state.isObserver(carl.jid));
        assertTrue(bob.state.isParticipant(alice.jid));
        assertTrue(bob.state.isDriver(alice.jid));

        assertTrue(alice.state.isParticipant(alice.jid));
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(carl.jid));
        assertTrue(alice.state.isObserver(carl.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(alice.state.isObserver(bob.jid));

        // Need to check for isDriver before leaving.
        carl.bot.leaveSessionByPeer();
        assertFalse(carl.state.isParticipant(carl.jid));

        // Need to check for isDriver before leaving.
        bob.bot.leaveSessionByPeer();
        assertFalse(bob.state.isParticipant(bob.jid));

        // alice.waitUntilOtherLeaveSession(carl);
        // alice.waitUntilOtherLeaveSession(bob);
        alice.bot.waitUntilSessionClosedBy(carl.state);
        alice.bot.waitUntilSessionClosedBy(bob.state);
        // Need to check for isDriver before leaving.
        alice.bot.leaveSessionByHost();
        assertFalse(alice.state.isParticipant(alice.jid));

        // invitee1.sleep(1000);
        // invitee2.sleep(1000);

    }
}
