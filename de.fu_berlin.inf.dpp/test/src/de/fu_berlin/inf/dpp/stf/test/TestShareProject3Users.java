package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestShareProject3Users {
    // bots
    protected static Musician inviter;
    protected static Musician invitee1;
    protected static Musician invitee2;

    @BeforeClass
    public static void configureInvitee1() throws RemoteException,
        NotBoundException {
        invitee1 = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        invitee1.initBot();
    }

    @BeforeClass
    public static void configureInvitee2() throws RemoteException,
        NotBoundException {
        invitee2 = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        invitee2.initBot();
    }

    @BeforeClass
    public static void configureInviter() throws RemoteException,
        NotBoundException {
        inviter = new Musician(new JID(BotConfiguration.JID_CARL),
            BotConfiguration.PASSWORD_CARL, BotConfiguration.HOST_CARL,
            BotConfiguration.PORT_CARL);
        inviter.initBot();
        inviter.newProjectWithClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @AfterClass
    public static void cleanupInvitee1() throws RemoteException {
        invitee1.xmppDisconnect();
        invitee1.deleteResource(BotConfiguration.PROJECTNAME);
    }

    @AfterClass
    public static void cleanupInvitee2() throws RemoteException {
        invitee2.xmppDisconnect();
        invitee2.deleteResource(BotConfiguration.PROJECTNAME);
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
        assertTrue(invitee1.hasParticipant(invitee2));
        assertTrue(invitee1.isObserver(invitee2));
        assertTrue(invitee1.hasParticipant(inviter));
        assertTrue(invitee1.isDriver(inviter));

        assertTrue(invitee2.isParticipant());
        assertTrue(invitee2.isObserver());
        assertTrue(invitee2.hasParticipant(invitee1));
        assertTrue(invitee2.isObserver(invitee1));
        assertTrue(invitee2.hasParticipant(inviter));
        assertTrue(invitee2.isDriver(inviter));

        assertTrue(inviter.isParticipant());
        assertTrue(inviter.isDriver());
        assertTrue(inviter.hasParticipant(invitee1));
        assertTrue(inviter.isObserver(invitee1));
        assertTrue(inviter.hasParticipant(invitee2));
        assertTrue(inviter.isObserver(invitee2));

        invitee1.leaveSession();
        assertFalse(invitee1.isParticipant());

        invitee2.leaveSession();
        assertFalse(invitee2.isParticipant());

        inviter.leaveSession();
        assertFalse(inviter.isParticipant());
    }
}
