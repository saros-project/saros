package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
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

public class TestHandleContacts {
    // bots
    protected Musician bob;
    protected Musician alice;

    @Before
    public void configureRespondent() throws RemoteException, NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
    }

    @Before
    public void configureQuestioner() throws RemoteException, NotBoundException {
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();
    }

    @After
    public void cleanupRespondent() {
        alice.xmppDisconnect();
    }

    @After
    public void cleanupQuestioner() {
        bob.xmppDisconnect();
    }

    // FIXME these testAddContact assumes that testRemoveContact succeeds
    // FIXME all the other tests in the suite would fail if testAddContact fails

    @Test
    public void testRemoveContact() throws RemoteException {
        assertTrue(alice.hasContactWith(bob));
        bob.deleteContact(alice);
        alice
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION);
        alice.confirmWindow(SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION,
            SarosConstant.BUTTON_OK);
        bob.waitUntilHasNoContactWith(alice);
        assertFalse(bob.hasContactWith(alice));
        alice.waitUntilHasNoContactWith(bob);
        assertFalse(alice.hasContactWith(bob));
    }

    @Test
    public void testAddContact() throws RemoteException {
        bob.addContact(alice);
        alice.confirmContact(bob);
        bob.confirmContact(alice);
        bob.waitUntilHasContactWith(alice);
        assertTrue(bob.hasContactWith(alice));
        alice.waitUntilHasContactWith(bob);
        assertTrue(alice.hasContactWith(bob));
    }
}
