package de.fu_berlin.inf.dpp.stf.test.team2;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS_ID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.shared.Constants;

public class RosterContextTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @After
    public void runAfterEveryTest() throws Exception {
        resetDefaultAccount();
    }

    @Before
    public void disconnectFromServer() throws RemoteException {
        ALICE.superBot().views().sarosView().disconnect();
    }

    @Test
    public void testRosterSessionContext() throws RemoteException {
        assertFalse(ALICE.superBot().views().sarosView().isConnected());

        ALICE.remoteBot().openViewById(VIEW_SAROS_ID);

        // is offline
        testDisconnectedState();

        // connect..
        ALICE.superBot().views().sarosView().connectWithActiveAccount();
        assertTrue(ALICE.superBot().views().sarosView().isConnected());

        testConnectedStateWithoutSession();

        // disconnect
        // no session should be there
    }

    private void testDisconnectedState() throws RemoteException {
        assertTrue(ALICE.remoteBot().activeView().bot().clabel().getText()
            .equals("Not connected"));

        assertTrue(ALICE.remoteBot().activeView().bot().tree()
            .selectTreeItem(Constants.NODE_BUDDIES)
            .existsContextMenu(Constants.CM_ADD_BUDDY_OFFLINE));

        // No Session should be there and no Contextmenu with "Share Projects"
        assertFalse(ALICE.remoteBot().activeView().bot().tree()
            .selectTreeItem(Constants.NODE_NO_SESSION_RUNNING)
            .existsContextMenu(Constants.SHARE_PROJECTS));
    }

    private void testConnectedStateWithoutSession() throws RemoteException {
        assertFalse(ALICE.remoteBot().activeView().bot().clabel().getText()
            .contains("Error")
            || ALICE.remoteBot().activeView().bot().clabel().getText()
                .equals("Not connected"));

        assertTrue(ALICE.remoteBot().activeView().bot().tree()
            .selectTreeItem(Constants.NODE_NO_SESSION_RUNNING)
            .existsContextMenu(Constants.SHARE_PROJECTS));

        assertTrue(ALICE.remoteBot().activeView().bot().tree()
            .selectTreeItem(Constants.NODE_BUDDIES)
            .existsContextMenu("Add Buddy..."));

    }
}
