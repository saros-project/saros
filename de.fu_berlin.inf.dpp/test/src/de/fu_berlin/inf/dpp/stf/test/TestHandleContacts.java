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
    public void cleanupRespondent() throws RemoteException {
        alice.xmppDisconnect();
    }

    @After
    public void cleanupQuestioner() throws RemoteException {
        bob.xmppDisconnect();
    }

    @Test
    public void testAddAndRemoveContact() throws RemoteException {
        assertTrue(bob.hasContactWith(alice));
        assertTrue(alice.hasContactWith(bob));

        bob.deleteContact(alice);

        alice
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION);
        try {
            // Don't accept immediately, Bob needs time to realize that he
            // removed Alice.
            Thread.sleep(500);
        } catch (InterruptedException e) {
            //
        }

        alice.confirmWindow(SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION,
            SarosConstant.BUTTON_OK);

        try {
            // Wait for the server.
            Thread.sleep(500);
        } catch (InterruptedException e) {
            //
        }

        assertFalse(bob.hasContactWith(alice));
        assertFalse(alice.hasContactWith(bob));

        bob.addContact(alice);

        alice.ackContact(bob);
        bob.ackContact(alice);

        assertTrue(bob.hasContactWith(alice));
        assertTrue(alice.hasContactWith(bob));
    }
}
