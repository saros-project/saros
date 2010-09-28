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

public class TestShare2UsersSequentially {
    private static final Logger log = Logger
        .getLogger(TestShare2UsersSequentially.class);

    // bots
    protected static Musician alice;
    protected static Musician bob;

    @BeforeClass
    public static void configureInvitee() throws RemoteException,
        NotBoundException {
        log.trace("configureInvitee enter");
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();
    }

    @BeforeClass
    public static void configureInviter() throws RemoteException,
        NotBoundException {
        log.trace("configureInviter");
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
        String projectName = BotConfiguration.PROJECTNAME;
        alice.bot.newJavaProject(projectName);
        alice.bot.newClass(projectName, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);
    }

    @AfterClass
    public static void cleanupInvitee() throws RemoteException {
        bob.bot.xmppDisconnect();
        bob.bot.deleteProject(BotConfiguration.PROJECTNAME);
        bob.bot.resetWorkbench();
    }

    @AfterClass
    public static void cleanupInviter() throws RemoteException {
        alice.bot.xmppDisconnect();
        alice.bot.deleteProject(BotConfiguration.PROJECTNAME);
        alice.bot.resetWorkbench();
    }

    @Test
    public void testShareProject() throws RemoteException {
        log.trace("testShareProject enter");

        alice.buildSessionSequential(BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        bob.bot
            .captureScreenshot((bob.state.getPathToScreenShot() + "/invitee_in_sharedproject.png"));
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/inviter_in_sharedproject.png"));
        log.trace("inviter.setTextInClass");
        alice.bot.setTextInJavaEditor(BotConfiguration.CONTENTPATH,
            BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);

        log.trace("invitee.openFile");
        bob.bot.openClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        // invitee.sleep(2000);
        assertTrue(bob.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
        assertTrue(bob.state.isParticipant(alice.jid));
        assertTrue(bob.state.isDriver(alice.jid));

        assertTrue(alice.state.isParticipant(alice.jid));
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(alice.state.isObserver(bob.jid));

        // TODO make tests independent of each other
        // @Test
        // public void testLeaveSession() throws RemoteException {
        bob.leaveSession();
        log.trace("invitee.leave");
        assertFalse(bob.state.isParticipant(bob.jid));

        bob.bot.waitUntilSessionCloses(bob.state);
        // TODO Dialog "Do you really want to close" pops up
        alice.leaveSession();
        log.trace("inviter.leave");
        assertFalse(alice.state.isParticipant(alice.jid));
        // invitee.waitUntilSessionClosesBy(inviter);
    }
}
