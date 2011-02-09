package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.initialising;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

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
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetBuddies();
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
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertTrue(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
        deleteBuddies(bob, alice);
        assertFalse(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
        assertFalse(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
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
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertTrue(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
        deleteBuddies(alice, bob);
        assertFalse(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
        assertFalse(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
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
        deleteBuddies(alice, bob);
        addBuddies(alice, bob);
        assertTrue(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
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
        deleteBuddies(bob, alice);
        addBuddies(bob, alice);
        assertTrue(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
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
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipInView(
            VIEW_SAROS_BUDDIES, TB_ADD_A_NEW_CONTACT);
        Map<String, String> labelsAndTexts = new HashMap<String, String>();
        labelsAndTexts.put("XMPP/Jabber ID", "bob@bla");

        alice.shell.confirmShellWithTextFieldAndWait(SHELL_NEW_BUDDY,
            labelsAndTexts, FINISH);

        alice.shell.waitUntilShellActive(SHELL_BUDDY_LOOKUP_FAILED);
        assertTrue(alice.shell.isShellActive(SHELL_BUDDY_LOOKUP_FAILED));
        alice.shell.confirmShell(SHELL_BUDDY_LOOKUP_FAILED, NO);

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
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipInView(
            VIEW_SAROS_BUDDIES, TB_ADD_A_NEW_CONTACT);
        Map<String, String> labelsAndTexts = new HashMap<String, String>();
        labelsAndTexts.put("XMPP/Jabber ID", bob.getBaseJid());

        alice.shell.confirmShellWithTextFieldAndWait(SHELL_NEW_BUDDY,
            labelsAndTexts, FINISH);
        alice.shell.waitUntilShellActive(SHELL_BUDDY_ALREADY_ADDED);
        assertTrue(alice.shell.isShellActive(SHELL_BUDDY_ALREADY_ADDED));
        alice.shell.closeShell(SHELL_BUDDY_ALREADY_ADDED);
    }
}
