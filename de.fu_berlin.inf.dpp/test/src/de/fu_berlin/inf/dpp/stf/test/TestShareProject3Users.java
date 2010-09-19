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
    protected static Musician alice;
    protected static Musician carl;
    protected static Musician bob;

    @BeforeClass
    public static void configurePeer1() throws RemoteException,
        NotBoundException {
        carl = new Musician(new JID(BotConfiguration.JID_CARL),
            BotConfiguration.PASSWORD_CARL, BotConfiguration.HOST_CARL,
            BotConfiguration.PORT_CARL);
        carl.initBot();
    }

    @BeforeClass
    public static void configurePeer2() throws RemoteException,
        NotBoundException {
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();
    }

    @BeforeClass
    public static void configureHost() throws RemoteException,
        NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
        alice.newProjectWithClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @AfterClass
    public static void cleanupInvitee1() throws RemoteException {
        carl.xmppDisconnect();
        carl.deleteProject(BotConfiguration.PROJECTNAME);
    }

    @AfterClass
    public static void cleanupInvitee2() throws RemoteException {
        bob.xmppDisconnect();
        bob.deleteProject(BotConfiguration.PROJECTNAME);
    }

    @After
    public void cleanupInviter() throws RemoteException {
        alice.xmppDisconnect();
        alice.deleteProject(BotConfiguration.PROJECTNAME);
    }

    @Test
    public void testShareProject() throws RemoteException {

        List<Musician> musicians = new LinkedList<Musician>();
        musicians.add(carl);
        musicians.add(bob);

        alice.shareProjectParallel(BotConfiguration.PROJECTNAME, musicians);

        carl.confirmSessionInvitationWizard(alice, BotConfiguration.PROJECTNAME);
        bob.confirmSessionInvitationWizard(alice, BotConfiguration.PROJECTNAME);

        assertTrue(carl.isParticipant());
        assertTrue(carl.isObserver());
        assertTrue(carl.hasParticipant(bob));
        assertTrue(carl.isObserver(bob));
        assertTrue(carl.hasParticipant(alice));
        assertTrue(carl.isDriver(alice));

        assertTrue(bob.isParticipant());
        assertTrue(bob.isObserver());
        assertTrue(bob.hasParticipant(carl));
        assertTrue(bob.isObserver(carl));
        assertTrue(bob.hasParticipant(alice));
        assertTrue(bob.isDriver(alice));

        assertTrue(alice.isParticipant());
        assertTrue(alice.isDriver());
        assertTrue(alice.hasParticipant(carl));
        assertTrue(alice.isObserver(carl));
        assertTrue(alice.hasParticipant(bob));
        assertTrue(alice.isObserver(bob));

        carl.leaveSession();
        assertFalse(carl.isParticipant());

        bob.leaveSession();
        assertFalse(bob.isParticipant());

        alice.waitUntilOtherLeaveSession(carl);
        alice.waitUntilOtherLeaveSession(bob);
        alice.leaveSession();
        assertFalse(alice.isParticipant());

        // invitee1.sleep(1000);
        // invitee2.sleep(1000);

    }
}
