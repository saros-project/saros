package de.fu_berlin.inf.dpp.stf.test.initialising;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.FINISH;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.LABEL_XMPP_JABBER_ID;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.NO;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_ADD_BUDDY;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SERVER_NOT_FOUND;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.TB_ADD_A_NEW_BUDDY;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;

public class HandleContactsTest extends StfTestCase {

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
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @Override
    @After
    public void tearDown() throws RemoteException {
        announceTestCaseEnd();
        resetBuddies();
    }

    // FIXME these testAddContact assumes that testRemoveContact succeeds
    // FIXME all the other tests in the suite would fail if testAddContact fails

    /**
     * Steps:
     * <ol>
     * <li>BOB delete buddy ALICE.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>BOB and ALICE don't contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testBobRemoveBuddyAlice() throws RemoteException {
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertTrue(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
        Util.deleteBuddies(BOB, ALICE);
        assertFalse(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
        assertFalse(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE delete buddy BOB.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>BOB and ALICE don't contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAliceRemoveBuddyBob() throws RemoteException {
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertTrue(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
        Util.deleteBuddies(ALICE, BOB);
        assertFalse(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
        assertFalse(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE delete buddy BOB first and then add BOB.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>BOB and ALICE contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAliceAddBuddyBob() throws RemoteException {
        Util.deleteBuddies(ALICE, BOB);
        Util.addBuddies(ALICE, BOB);
        assertTrue(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
    }

    /**
     * Steps:
     * <ol>
     * <li>BOB delete buddy ALICE first and then add ALICE again.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>BOB and ALICE contact each other.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testBobAddBuddyAlice() throws RemoteException {
        Util.deleteBuddies(BOB, ALICE);
        Util.addBuddies(BOB, ALICE);
        assertTrue(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE click toolbar button "Add a new contact".</li>
     * <li>ALICE enter invalid contact name in the popup window "New contact"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>ALICE should get error message "Contact look up failed".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testAddNoValidContact() throws RemoteException {
        ALICE.remoteBot().view(VIEW_SAROS).toolbarButton(TB_ADD_A_NEW_BUDDY)
            .click();
        Map<String, String> labelsAndTexts = new HashMap<String, String>();
        labelsAndTexts.put("XMPP/Jabber ID", "BOB@bla");

        ALICE.remoteBot().waitUntilShellIsOpen(SHELL_ADD_BUDDY);
        IRemoteBotShell shell = ALICE.remoteBot().shell(SHELL_ADD_BUDDY);
        shell.activate();
        shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_ID).setText("BOB@bla");
        shell.bot().button(FINISH).click();
        ALICE.remoteBot().waitUntilShellIsOpen(SHELL_SERVER_NOT_FOUND);
        assertTrue(ALICE.remoteBot().shell(SHELL_SERVER_NOT_FOUND).isActive());
        ALICE.remoteBot().shell(SHELL_SERVER_NOT_FOUND).confirm(NO);

    }

}
