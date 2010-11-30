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

    /**
     * Preconditions:
     * <ol>
     * <li>init Alice</li>
     * <li>init Bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        bob = InitMusician.newBob();
        alice = InitMusician.newAlice();
    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * make sure, alice and bob are connected
     */
    @Before
    public void setUp() throws RemoteException {
        assertTrue(alice.rosterV.isConnected());
        assertTrue(bob.rosterV.isConnected());
    }

    /**
     * make sure, bob and alice contact each other. <br>
     * make sure,all opened popup windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        alice.addBuddyDone(bob);
        bob.addBuddyDone(alice);
        alice.workbench.resetWorkbench();
        bob.workbench.resetWorkbench();
    }

    // FIXME these testAddContact assumes that testRemoveContact succeeds
    // FIXME all the other tests in the suite would fail if testAddContact fails

    /**
     * Steps:
     * <ol>
     * <li>bob delete buddy alice.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob and alice don't contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testBobRemoveBuddyAlice() throws RemoteException {
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(bob.rosterV.hasBuddy(alice.jid));
        bob.deleteBuddyDone(alice);
        assertFalse(bob.rosterV.hasBuddy(alice.jid));
        assertFalse(alice.rosterV.hasBuddy(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete buddy bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob and alice don't contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAliceRemoveBuddyBob() throws RemoteException {
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
        assertTrue(bob.rosterV.hasBuddy(alice.jid));
        alice.deleteBuddyDone(bob);
        assertFalse(bob.rosterV.hasBuddy(alice.jid));
        assertFalse(alice.rosterV.hasBuddy(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete buddy bob first and then add bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob and alice contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAliceAddBuddyBob() throws RemoteException {
        alice.deleteBuddyDone(bob);
        alice.addBuddyDone(bob);
        assertTrue(bob.rosterV.hasBuddy(alice.jid));
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>bob delete buddy alice first and then add alice again.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob and alice contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testBobAddBuddyAlice() throws RemoteException {
        bob.deleteBuddyDone(alice);
        bob.addBuddyDone(alice);
        assertTrue(bob.rosterV.hasBuddy(alice.jid));
        assertTrue(alice.rosterV.hasBuddy(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice click toolbar button "Add a new contact".</li>
     * <li>alice enter invalid contact name in the popup window "New contact"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice should get error message "Contact look up failed".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAddNoValidContact() throws RemoteException {
        alice.rosterV.clickAddANewContactToolbarButton();
        alice.rosterV.confirmNewContactWindow("bob@bla");
        alice.rosterV.waitUntilContactLookupFailedIsActive();
        assertTrue(alice.rosterV.isWindowContactLookupFailedActive());
        alice.rosterV.confirmContactLookupFailedWindow(NO);
    }

    /**
     * Steps:
     * <ol>
     * <li>alice click toolbar button "Add a new contact".</li>
     * <li>alice enter a existed contact name in the popup window "New contact"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice should get error message "Contact already added".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAddExistedContact() throws RemoteException {
        alice.rosterV.clickAddANewContactToolbarButton();
        alice.rosterV.confirmNewContactWindow(bob.getBaseJid());
        alice.rosterV.waitUntilWindowContactAlreadyAddedIsActive();
        assertTrue(alice.rosterV.isWindowContactAlreadyAddedActive());
        alice.rosterV.closeWindowContactAlreadyAdded();
    }

}
