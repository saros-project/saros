package de.fu_berlin.inf.dpp.stf.test.initialising;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class TestHandleContacts {
    // bots
    protected static Musician bob;
    protected static Musician alice;

    @BeforeClass
    public static void configureAlice() throws RemoteException,
        NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
    }

    @BeforeClass
    public static void configureBob() throws RemoteException, NotBoundException {
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();
    }

    @AfterClass
    public static void cleanupAlice() throws RemoteException {
        alice.bot.xmppDisconnect();
    }

    @AfterClass
    public static void cleanupBob() throws RemoteException {
        bob.bot.xmppDisconnect();
    }

    @After
    public void reset() throws RemoteException {
        alice.bot.resetWorkbench();
        bob.bot.resetWorkbench();
    }

    // FIXME these testAddContact assumes that testRemoveContact succeeds
    // FIXME all the other tests in the suite would fail if testAddContact fails

    @Test
    public void testRemoveContact() throws RemoteException {
        assertTrue(alice.bot.hasContactWith(bob.jid));
        bob.bot.deleteContact(alice.jid.getBase());
        alice.bot
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION);
        alice.bot.confirmWindow(
            SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION,
            SarosConstant.BUTTON_OK);
        assertFalse(bob.bot.hasContactWith(alice.jid));
        assertFalse(alice.bot.hasContactWith(bob.jid));
    }

    @Test
    public void testAddContact() throws RemoteException {
        bob.bot.addContact(alice.getPlainJid());
        alice.bot.confirmRequestOfSubscriptionReceivedWindow();
        bob.bot.confirmRequestOfSubscriptionReceivedWindow();
        assertTrue(bob.bot.hasContactWith(alice.jid));
        assertTrue(alice.bot.hasContactWith(bob.jid));
    }
}
