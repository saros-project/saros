package de.fu_berlin.inf.dpp.stf.test;

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

public class TestShareProject {
    private static final Logger log = Logger.getLogger(TestShareProject.class);

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
        alice.newProjectWithClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @AfterClass
    public static void cleanupInvitee() throws RemoteException {
        bob.xmppDisconnect();
        bob.deleteProject(BotConfiguration.PROJECTNAME);
    }

    @AfterClass
    public static void cleanupInviter() throws RemoteException {
        alice.xmppDisconnect();
        alice.deleteProject(BotConfiguration.PROJECTNAME);
    }

    @Test
    public void testShareProject() throws RemoteException {
        log.trace("testShareProject enter");

        alice.buildSession(bob, BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT,
            SarosConstant.CREATE_NEW_PROJECT);

        bob.captureScreenshot(bob.getPathToScreenShot()
            + "/invitee_in_sharedproject.png");
        alice.captureScreenshot(alice.getPathToScreenShot()
            + "/inviter_in_sharedproject.png");

        log.trace("inviter.setTextInClass");
        alice.setTextInJavaEditor(BotConfiguration.CONTENTPATH,
            BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);

        log.trace("invitee.openFile");
        bob.openJavaFileWithEditor(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        // invitee.sleep(2000);
        assertTrue(bob.isParticipant());
        assertTrue(bob.isObserver());
        assertTrue(bob.hasParticipant(alice));
        assertTrue(bob.isDriver(alice));

        assertTrue(alice.isParticipant());
        assertTrue(alice.isDriver());
        assertTrue(alice.hasParticipant(bob));
        assertTrue(alice.isObserver(bob));

        // TODO make tests independent of each other
        // @Test
        // public void testLeaveSession() throws RemoteException {
        bob.leaveSession();
        log.trace("invitee.leave");
        assertFalse(bob.isParticipant());

        alice.waitUntilSessionClosesBy(bob);
        alice.sleep(50);
        alice.leaveSession();
        log.trace("inviter.leave");
        assertFalse(alice.isParticipant());
        // invitee.waitUntilSessionClosesBy(inviter);
    }
}
