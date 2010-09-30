package de.fu_berlin.inf.dpp.stf.test.initialising;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestHandleContacts {

    protected static Musician bob = InitMusician.newBob();
    protected static Musician alice = InitMusician.newAlice();

    @AfterClass
    public static void cleanupAlice() throws RemoteException {
        alice.bot.resetSaros();
    }

    @AfterClass
    public static void cleanupBob() throws RemoteException {
        bob.bot.resetSaros();
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
