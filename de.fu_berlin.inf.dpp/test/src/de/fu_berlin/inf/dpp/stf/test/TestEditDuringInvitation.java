package de.fu_berlin.inf.dpp.stf.test;

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
    }

    @BeforeClass
    public static void configureBob() throws RemoteException, NotBoundException {
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();
    }

    @BeforeClass
    public static void configureInviter() throws RemoteException,
        NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
        alice.newProjectWithClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @AfterClass
    public static void cleanupBob() throws RemoteException {
        bob.xmppDisconnect();
        bob.bot.deleteProject(BotConfiguration.PROJECTNAME);
    }

    @AfterClass
    public static void cleanupCarl() throws RemoteException {
        carl.xmppDisconnect();
        carl.bot.deleteProject(BotConfiguration.PROJECTNAME);
    }

    @AfterClass
    public static void cleanupAlice() throws RemoteException {
        alice.xmppDisconnect();
        alice.bot.deleteProject(BotConfiguration.PROJECTNAME);
    }

    private static final Logger log = Logger
        .getLogger(TestEditDuringInvitation.class);

    @Test
    public void testEditDuringInvitation() throws RemoteException {
        log.trace("starting testEditDuringInvitation, alice.buildSession");
        alice.buildSession(bob, BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT,
            SarosConstant.CREATE_NEW_PROJECT);

        log.trace("alice.giveDriverRole");
        alice.giveDriverRole(bob);

        assertTrue(bob.state.isDriver(alice.jid));

        log.trace("alice.inviteUser(carl");
        alice.inviteUser(carl, BotConfiguration.PROJECTNAME);

        log.trace("carl.confirmSessionInvitationWindowStep1");
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        carl.bot.confirmSessionInvitationWindowStep1(alice.getPlainJid());

        log.trace("bob.setTextInJavaEditor");
        bob.bot.setTextInJavaEditor(BotConfiguration.CONTENTPATH, BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        BotConfiguration.CLASSNAME);

        log.trace("carl.confirmSessionInvitationWindowStep2UsingNewproject");
        carl.bot.confirmSessionInvitationWindowStep2UsingNewproject(BotConfiguration.PACKAGENAME);

        log.trace("getTextOfJavaEditor");
        String textFromCarl = carl.bot.getTextOfJavaEditor(BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        String textFormAlice = alice.bot.getTextOfJavaEditor(BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        assertTrue(textFromCarl.equals(textFormAlice));

        log.trace("testEditDuringInvitation done");
    }
}
