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

public class TestFollowMode {
    // bots
    protected Musician inviter;
    protected Musician invitee;

    @Before
    public void configureInvitee() throws RemoteException, NotBoundException {
        invitee = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        invitee.initRmi();
        invitee.activeMusican();

        invitee.openSarosViews();
        invitee.openPerspective("Java");
        invitee.xmppConnect();
    }

    @Before
    public void configureInviter() throws RemoteException, NotBoundException {
        inviter = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        inviter.initRmi();
        inviter.activeMusican();

        inviter.openSarosViews();
        inviter.openPerspective("Java");
        inviter.xmppConnect();
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
        inviter.shareProject(invitee, BotConfiguration.PROJECTNAME);
        invitee.waitOnWindowByTitle("Session Invitation");
        invitee.ackProject(inviter, BotConfiguration.PROJECTNAME);
        invitee.openFile(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        inviter.typeInTextInClass(BotConfiguration.CONTENTPATH,
            BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);

        invitee.sleep(750);
        invitee.follow(inviter);
        invitee.sleep(750);
        assertTrue(invitee.isInFollowMode(inviter));
        invitee.sleep(750);
        inviter.createJavaClassInProject(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME2);
        inviter.sleep(2000);
        assertTrue(invitee.isEditorActive(BotConfiguration.CLASSNAME2));
        inviter.sleep(750);

        inviter.follow(invitee);
        inviter.sleep(750);
        assertTrue(inviter.isInFollowMode(invitee));
        inviter.sleep(750);
        invitee.activeEditor(BotConfiguration.CLASSNAME);
        invitee.sleep(2000);
        assertTrue(inviter.isEditorActive(BotConfiguration.CLASSNAME));
        inviter.sleep(750);

    }
}
