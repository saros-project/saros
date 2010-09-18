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
    protected static Musician inviter;
    protected static Musician invitee;

    @BeforeClass
    public static void configureInvitee() throws RemoteException,
        NotBoundException {
        log.trace("configureInvitee enter");
        invitee = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        invitee.initBot();
    }

    @BeforeClass
    public static void configureInviter() throws RemoteException,
        NotBoundException {
        log.trace("configureInviter");
        inviter = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        inviter.initBot();
        inviter.newProjectWithClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @AfterClass
    public static void cleanupInvitee() throws RemoteException {
        invitee.xmppDisconnect();
        invitee.deleteResource(BotConfiguration.PROJECTNAME);
    }

    @AfterClass
    public static void cleanupInviter() throws RemoteException {
        inviter.xmppDisconnect();
        inviter.deleteResource(BotConfiguration.PROJECTNAME);
    }

    @Test
    public void testShareProject() throws RemoteException {
        log.trace("testShareProject enter");

        inviter.buildSession(invitee, BotConfiguration.PROJECTNAME,
            SarosConstant.CREATE_NEW_PROJECT);

        invitee.captureScreenshot(invitee.getPathToScreenShot()
            + "/invitee_in_sharedproject.png");
        inviter.captureScreenshot(inviter.getPathToScreenShot()
            + "/inviter_in_sharedproject.png");

        log.trace("inviter.setTextInClass");
        inviter.setTextInJavaEditor(BotConfiguration.CONTENTPATH,
            BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);

        log.trace("invitee.openFile");
        invitee.openJavaFileWithEditor(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        // invitee.sleep(2000);
        assertTrue(invitee.isParticipant());
        assertTrue(invitee.isObserver());
        assertTrue(invitee.hasParticipant(inviter));
        assertTrue(invitee.isDriver(inviter));

        assertTrue(inviter.isParticipant());
        assertTrue(inviter.isDriver());
        assertTrue(inviter.hasParticipant(invitee));
        assertTrue(inviter.isObserver(invitee));

    }

    @Test
    public void testLeaveSession() throws RemoteException {
        invitee.leaveSession();
        log.trace("invitee.leave");
        assertFalse(invitee.isParticipant());
        inviter.waitUntilSessionClosesBy(invitee);

        inviter.leaveSession();
        log.trace("inviter.leave");
        assertFalse(inviter.isParticipant());
        // invitee.waitUntilSessionClosesBy(inviter);
    }
}
