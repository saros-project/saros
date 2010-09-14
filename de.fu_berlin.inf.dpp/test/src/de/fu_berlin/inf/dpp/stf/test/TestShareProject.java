package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class TestShareProject {
    private static final Logger log = Logger.getLogger(TestShareProject.class);

    // bots
    protected Musician inviter;
    protected Musician invitee;

    @Before
    public void configureInvitee() throws RemoteException, NotBoundException {
        log.trace("configureInvitee enter");
        invitee = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        invitee.initBot();
    }

    @Before
    public void configureInviter() throws RemoteException, NotBoundException {
        log.trace("configureInviter");
        inviter = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        inviter.initBot();
        inviter.createProjectWithClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @After
    public void cleanupInvitee() throws RemoteException {
        invitee.xmppDisconnect();
        invitee.removeProject(BotConfiguration.PROJECTNAME);
    }

    @After
    public void cleanupInviter() throws RemoteException {
        inviter.xmppDisconnect();
    }

    @Test
    public void testShareProject() throws RemoteException {
        log.trace("testShareProject enter");

        inviter.buildSession(invitee, BotConfiguration.PROJECTNAME,
            SarosConstant.SHARE_PROJECT, SarosConstant.CREATE_NEW_PROJECT);

        invitee.captureScreenshot(invitee.getPathToScreenShot()
            + "/invitee_in_sharedproject.png");
        inviter.captureScreenshot(inviter.getPathToScreenShot()
            + "/inviter_in_sharedproject.png");

        log.trace("inviter.setTextInClass");
        inviter.setTextInClass(BotConfiguration.CONTENTPATH,
            BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);

        log.trace("invitee.openFile");
        invitee.openFile(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        invitee.sleep(2000);
        assertTrue(invitee.isParticipant());
        assertTrue(invitee.isObserver());
        assertTrue(invitee.isParticipant(inviter));
        assertTrue(invitee.isDriver(inviter));

        assertTrue(inviter.isParticipant());
        assertTrue(inviter.isDriver());
        assertTrue(inviter.isParticipant(invitee));
        assertTrue(inviter.isObserver(invitee));

        log.trace("invitee.leave");
        invitee.leave(true);
        invitee.sleep(2000);
        assertFalse(invitee.isParticipant());

        log.trace("inviter.leave");
        inviter.leave(false);
        invitee.sleep(2000);
        assertFalse(inviter.isParticipant());
    }
}
