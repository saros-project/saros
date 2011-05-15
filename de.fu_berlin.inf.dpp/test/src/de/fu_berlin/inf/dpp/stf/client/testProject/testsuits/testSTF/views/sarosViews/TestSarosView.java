package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestSarosView extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testSarosView() throws RemoteException {
        alice.remoteBot().view(VIEW_SAROS).close();
        assertEquals(false, alice.remoteBot().isViewOpen(VIEW_SAROS));
        alice.remoteBot().openViewById(VIEW_SAROS_ID);
        alice.remoteBot().captureScreenshot(
            (alice.remoteBot().getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.remoteBot().isViewOpen(VIEW_SAROS));
    }

    @Test
    public void connect() throws RemoteException {
        log.trace("xmppConnect");
        alice.superBot().views().sarosView()
            .connectWith(alice.getJID(), alice.getPassword());
        log.trace("captureScreenshot");
        alice.remoteBot().captureScreenshot(
            (alice.remoteBot().getPathToScreenShot() + "/xmpp_connected.png"));
        assertEquals(true, alice.superBot().views().sarosView().isConnected());
    }

    @Test
    public void connectWith() throws RemoteException {
        alice.superBot().views().sarosView().connectWith(TEST_JID, PASSWORD);
        assertTrue(alice.superBot().menuBar().saros().preferences()
            .isAccountActive(TEST_JID));
    }

    @Test
    public void connectWithActiveAccount() throws RemoteException {
        alice.superBot().views().sarosView().connectWithActiveAccount();
        assertTrue(alice.superBot().views().sarosView().isConnected());
    }

    @Test
    public void disconnect() throws RemoteException {
        alice.superBot().views().sarosView()
            .connectWith(alice.getJID(), alice.getPassword());
        alice.superBot().views().sarosView().disconnect();
        alice.remoteBot().captureScreenshot(
            (alice.remoteBot().getPathToScreenShot() + "/xmpp_disconnected.png"));
        assertEquals(false, alice.superBot().views().sarosView().isConnected());
    }

    @Test
    public void addExistedBuddy() throws RemoteException {
        assertTrue(alice.superBot().views().sarosView().hasBuddy(TEST_JID));
        alice.superBot().views().sarosView().addANewBuddy(TEST_JID);
        assertTrue(alice.superBot().views().sarosView().hasBuddy(TEST_JID));
    }
}
