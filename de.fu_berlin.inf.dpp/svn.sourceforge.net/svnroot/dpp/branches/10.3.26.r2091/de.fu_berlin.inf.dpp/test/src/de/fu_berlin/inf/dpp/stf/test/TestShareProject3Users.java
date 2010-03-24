package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestShareProject3Users {
    // bots
    protected Musician inviter;
    protected Musician invitee1;
    protected Musician invitee2;

    @Before
    public void configureInvitee1() throws RemoteException, NotBoundException {
        invitee1 = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        invitee1.initRmi();
        invitee1.openSarosViews();
        invitee1.xmppConnect();
    }

    @Before
    public void configureInvitee2() throws RemoteException, NotBoundException {
        invitee2 = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        invitee2.initRmi();
        invitee2.openSarosViews();
        invitee2.xmppConnect();
    }

    @Before
    public void configureInviter() throws RemoteException, NotBoundException {
        inviter = new Musician(new JID(BotConfiguration.JID_CARL),
            BotConfiguration.PASSWORD_CARL, BotConfiguration.HOST_CARL,
            BotConfiguration.PORT_CARL);

        inviter.initRmi();
        inviter.openSarosViews();
        inviter.xmppConnect();
        inviter.createProjectWithClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @After
    public void cleanupInvitee1() throws RemoteException {
        invitee1.xmppDisconnect();
        invitee1.removeProject(BotConfiguration.PROJECTNAME);
    }

    @After
    public void cleanupInvitee2() throws RemoteException {
        invitee2.xmppDisconnect();
        invitee2.removeProject(BotConfiguration.PROJECTNAME);
    }

    @After
    public void cleanupInviter() throws RemoteException {
        inviter.xmppDisconnect();
    }

    @Test
    public void testShareProjectParallel() throws RemoteException {
        List<Musician> musicians = new LinkedList<Musician>();
        musicians.add(invitee1);
        musicians.add(invitee2);

        inviter.shareProjectParallel(BotConfiguration.PROJECTNAME, musicians);

        invitee1.ackProject(inviter, BotConfiguration.PROJECTNAME);
        invitee2.ackProject(inviter, BotConfiguration.PROJECTNAME);

        assertTrue(invitee1.isParticipant());
        assertTrue(invitee1.isObserver());
        assertTrue(invitee1.isParticipant(invitee2));
        assertTrue(invitee1.isObserver(invitee2));
        assertTrue(invitee1.isParticipant(inviter));
        assertTrue(invitee1.isDriver(inviter));

        assertTrue(invitee2.isParticipant());
        assertTrue(invitee2.isObserver());
        assertTrue(invitee2.isParticipant(invitee1));
        assertTrue(invitee2.isObserver(invitee1));
        assertTrue(invitee2.isParticipant(inviter));
        assertTrue(invitee2.isDriver(inviter));

        assertTrue(inviter.isParticipant());
        assertTrue(inviter.isDriver());
        assertTrue(inviter.isParticipant(invitee1));
        assertTrue(inviter.isObserver(invitee1));
        assertTrue(inviter.isParticipant(invitee2));
        assertTrue(inviter.isObserver(invitee2));

        invitee1.leave(true);
        assertFalse(invitee1.isParticipant());
        invitee2.sleep(500); // balloon window
        invitee2.leave(true);
        assertFalse(invitee2.isParticipant());
        inviter.sleep(500);
        inviter.leave(false);
        assertFalse(inviter.isParticipant());
    }
}
