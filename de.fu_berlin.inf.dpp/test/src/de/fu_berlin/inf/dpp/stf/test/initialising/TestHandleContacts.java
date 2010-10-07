package de.fu_berlin.inf.dpp.stf.test.initialising;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestHandleContacts {

    protected static Musician bob;
    protected static Musician alice;

    @BeforeClass
    public static void initMusicians() {
        bob = InitMusician.newBob();
        alice = InitMusician.newAlice();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @Before
    public void setUp() throws RemoteException {
        alice.bot.addContact(bob.jid, bob.bot);
        bob.bot.addContact(alice.jid, alice.bot);
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.bot.resetWorkbench();
        bob.bot.resetWorkbench();
    }

    // FIXME these testAddContact assumes that testRemoveContact succeeds
    // FIXME all the other tests in the suite would fail if testAddContact fails

    @Test
    public void testBobRemoveContactAlice() throws RemoteException {
        assertTrue(alice.bot.hasContactWith(bob.jid));
        assertTrue(bob.bot.hasContactWith(alice.jid));
        bob.bot.deleteContact(alice.jid, alice.bot);
        assertFalse(bob.bot.hasContactWith(alice.jid));
        assertFalse(alice.bot.hasContactWith(bob.jid));
    }

    @Test
    public void testAliceRemoveContactBob() throws RemoteException {
        assertTrue(alice.bot.hasContactWith(bob.jid));
        assertTrue(bob.bot.hasContactWith(alice.jid));
        alice.bot.deleteContact(bob.jid, bob.bot);
        assertFalse(bob.bot.hasContactWith(alice.jid));
        assertFalse(alice.bot.hasContactWith(bob.jid));
    }

    @Test
    public void testAliceAddContactBob() throws RemoteException {
        alice.bot.deleteContact(bob.jid, bob.bot);
        alice.bot.addContact(bob.jid, bob.bot);
        assertTrue(bob.bot.hasContactWith(alice.jid));
        assertTrue(alice.bot.hasContactWith(bob.jid));
    }

    @Test
    public void testBobAddContactAlice() throws RemoteException {
        bob.bot.deleteContact(alice.jid, alice.bot);
        bob.bot.addContact(alice.jid, alice.bot);
        assertTrue(bob.bot.hasContactWith(alice.jid));
        assertTrue(alice.bot.hasContactWith(bob.jid));
    }

    @Test
    public void testAddNoValidContact() throws RemoteException {
        alice.bot.clickTBAddANewContactInRosterView();
        alice.bot.confirmNewContactWindow("bob@bla");
        alice.bot.waitUntilShellActive("Contact look-up failed");
        assertTrue(alice.bot.isShellActive("Contact look-up failed"));
        alice.bot.confirmWindow("Contact look-up failed",
            SarosConstant.BUTTON_NO);
    }

    @Test
    public void testAddExistedContact() throws RemoteException {
        alice.bot.clickTBAddANewContactInRosterView();
        alice.bot.confirmNewContactWindow(bob.getPlainJid());
        alice.bot.waitUntilShellActive("Contact already added");
        assertTrue(alice.bot.isShellActive("Contact already added"));
        alice.bot.closeShell("Contact already added");
    }

}
