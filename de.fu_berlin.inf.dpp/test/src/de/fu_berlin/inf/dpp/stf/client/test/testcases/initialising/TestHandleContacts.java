package de.fu_berlin.inf.dpp.stf.client.test.testcases.initialising;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestHandleContacts extends STFTest {

    @BeforeClass
    public static void initMusicians() throws RemoteException {
        bob = InitMusician.newBob();
        alice = InitMusician.newAlice();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @Before
    public void setUp() throws RemoteException {
        alice.addContactDone(bob);
        bob.addContactDone(alice);
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.workbench.resetWorkbench();
        bob.workbench.resetWorkbench();
    }

    // FIXME these testAddContact assumes that testRemoveContact succeeds
    // FIXME all the other tests in the suite would fail if testAddContact fails

    @Test
    public void testBobRemoveContactAlice() throws RemoteException {
        assertTrue(alice.rosterV.hasContactWith(bob.jid));
        assertTrue(bob.rosterV.hasContactWith(alice.jid));
        bob.deleteContactDone(alice);
        // assertFalse(bob.rosterV.hasContactWith(alice.jid));
        assertFalse(alice.rosterV.hasContactWith(bob.jid));
    }

    @Test
    public void testAliceRemoveContactBob() throws RemoteException {
        assertTrue(alice.rosterV.hasContactWith(bob.jid));
        assertTrue(bob.rosterV.hasContactWith(alice.jid));
        alice.deleteContactDone(bob);

        assertFalse(bob.rosterV.hasContactWith(alice.jid));
        assertFalse(alice.rosterV.hasContactWith(bob.jid));
    }

    @Test
    public void testAliceAddContactBob() throws RemoteException {
        alice.deleteContactDone(bob);
        alice.addContactDone(bob);
        assertTrue(bob.rosterV.hasContactWith(alice.jid));
        assertTrue(alice.rosterV.hasContactWith(bob.jid));
    }

    @Test
    public void testBobAddContactAlice() throws RemoteException {
        bob.deleteContactDone(alice);
        bob.addContactDone(alice);
        assertTrue(bob.rosterV.hasContactWith(alice.jid));
        assertTrue(alice.rosterV.hasContactWith(bob.jid));
    }

    @Test
    public void testAddNoValidContact() throws RemoteException {
        alice.rosterV.clickAddANewContactToolbarButton();
        alice.rosterV.confirmNewContactWindow("bob@bla");
        alice.rosterV.waitUntilContactLookupFailedIsActive();
        assertTrue(alice.rosterV.isWindowContactLookupFailedActive());
        alice.rosterV.confirmContactLookupFailedWindow(NO);
    }

    @Test
    public void testAddExistedContact() throws RemoteException {
        alice.rosterV.clickAddANewContactToolbarButton();
        alice.rosterV.confirmNewContactWindow(bob.getBaseJid());
        alice.rosterV.waitUntilWindowContactAlreadyAddedIsActive();
        assertTrue(alice.rosterV.isWindowContactAlreadyAddedActive());
        alice.rosterV.closeWindowContactAlreadyAdded();
    }

}
