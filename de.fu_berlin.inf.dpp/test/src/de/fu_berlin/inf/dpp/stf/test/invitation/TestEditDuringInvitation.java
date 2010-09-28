package de.fu_berlin.inf.dpp.stf.test.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class TestEditDuringInvitation {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    /**
     * Steps: 1. Alice invites Bob 2. Bob accepts the invitation 3. Alice gives
     * Bob driver capability 4. Alice invites Carl 5. Bob changes data during
     * the running invtiation of Carl.
     * 
     * Expected Results: All changes that Bob has done should be on Carl's side.
     * There should not be an inconsistency.
     */
    // bots
    protected static Musician alice;
    protected static Musician bob;
    protected static Musician carl;

    @BeforeClass
    public static void configureCarl() throws RemoteException,
        NotBoundException {
        carl = new Musician(new JID(BotConfiguration.JID_CARL),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_CARL,
            BotConfiguration.PORT_CARL);
        carl.initBot();
        if (carl.bot.isJavaProjectExist(PROJECT))
            carl.bot.deleteProject(PROJECT);
        assertFalse(carl.bot.isJavaProjectExist(PROJECT));
    }

    @BeforeClass
    public static void configureBob() throws RemoteException, NotBoundException {
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();
        if (bob.bot.isJavaProjectExist(PROJECT))
            bob.bot.deleteProject(PROJECT);
        assertFalse(bob.bot.isJavaProjectExist(PROJECT));
    }

    @BeforeClass
    public static void configureInviter() throws RemoteException,
        NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
        alice.bot.newJavaProject(PROJECT);
        alice.bot.newClass(PROJECT, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);
    }

    @AfterClass
    public static void cleanup() throws RemoteException {
        carl.bot.xmppDisconnect();
        carl.bot.deleteProject(PROJECT);
        bob.bot.xmppDisconnect();
        bob.bot.deleteProject(PROJECT);
        alice.bot.xmppDisconnect();
        alice.bot.deleteProject(PROJECT);
        carl.bot.resetWorkbench();
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    private static final Logger log = Logger
        .getLogger(TestEditDuringInvitation.class);

    /**
     * @throws RemoteException
     */
    @Test
    public void testEditDuringInvitation() throws RemoteException {
        log.trace("starting testEditDuringInvitation, alice.buildSession");
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);

        log.trace("alice.giveDriverRole");
        alice.bot.giveDriverRole(bob.getPlainJid());

        assertTrue(bob.state.isDriver(alice.jid));

        log.trace("alice.inviteUser(carl");
        alice.bot.inviteUser(carl.getPlainJid(), PROJECT);

        log.trace("carl.confirmSessionInvitationWindowStep1");
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        carl.bot.confirmSessionInvitationWindowStep1(alice.getPlainJid());

        log.trace("bob.setTextInJavaEditor");
        bob.bot.setTextInJavaEditor(BotConfiguration.CONTENTPATH, PROJECT,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        log.trace("carl.confirmSessionInvitationWindowStep2UsingNewproject");
        carl.bot
            .confirmSessionInvitationWindowStep2UsingNewproject(BotConfiguration.PACKAGENAME);

        log.trace("getTextOfJavaEditor");
        String textFromCarl = carl.bot.getTextOfJavaEditor(PROJECT,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        String textFormAlice = alice.bot.getTextOfJavaEditor(PROJECT,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        assertTrue(textFromCarl.equals(textFormAlice));

        log.trace("testEditDuringInvitation done");
    }
}
