package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class TestFollowMode {
    // bots
    protected Musician inviter;
    protected Musician invitee;

    @Before
    public void configureInvitee() throws RemoteException, NotBoundException {

        invitee = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        invitee.initBot();

    }

    @Before
    public void configureInviter() throws RemoteException, NotBoundException {

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
        inviter.removeProject(BotConfiguration.PROJECTNAME);
    }

    @Test
    public void testShareProject() throws RemoteException {
        inviter.buildSession(invitee, BotConfiguration.PROJECTNAME,
            SarosConstant.SHARE_PROJECT, SarosConstant.CREATE_NEW_PROJECT);

        invitee.openFile(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        inviter.setTextInClass(BotConfiguration.CONTENTPATH,
            BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);

        invitee.follow(inviter);
        invitee.sleep(1000);
        assertTrue(invitee.isInFollowMode(inviter));

        inviter.createJavaClassInProject(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME2);
        inviter.sleep(1000);
        assertTrue(invitee.isEditorActive(BotConfiguration.CLASSNAME2));

        inviter.follow(invitee);
        inviter.sleep(1000);
        assertTrue(inviter.isInFollowMode(invitee));

        invitee.activeEditor(BotConfiguration.CLASSNAME);
        invitee.sleep(750);
        assertTrue(inviter.isEditorActive(BotConfiguration.CLASSNAME));

    }
}
