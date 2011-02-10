package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestBasicSarosElements extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        disConnectByActiveTesters();
    }

    @Test
    public void testSessionView() throws RemoteException {
        alice.view.closeViewById(VIEW_SAROS_SESSION_ID);
        assertEquals(false, alice.view.isViewOpen(VIEW_SAROS_SESSION));
        alice.view.openViewById(VIEW_SAROS_SESSION_ID);
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.view.isViewOpen(VIEW_SAROS_SESSION));
    }

    @Test
    public void testRosterView() throws RemoteException {
        alice.view.closeViewById(VIEW_SAROS_BUDDIES_ID);
        assertEquals(false, alice.view.isViewOpen(VIEW_SAROS_BUDDIES));
        alice.view.openViewById(VIEW_SAROS_BUDDIES_ID);
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/roster_view.png"));
        assertEquals(true, alice.view.isViewOpen(VIEW_SAROS_BUDDIES));
    }

    @Test
    public void testChatView() throws RemoteException {
        alice.view.closeViewById(VIEW_SAROS_CHAT_ID);
        assertEquals(false, alice.chatV.isChatViewOpen());
        alice.view.closeViewById(VIEW_SAROS_CHAT_ID);
        assertEquals(true, alice.chatV.isChatViewOpen());
    }

    @Test
    public void testRemoteScreenView() throws RemoteException {
        alice.view.closeViewById(VIEW_REMOTE_SCREEN_ID);
        assertEquals(false, alice.rSV.isRemoteScreenViewOpen());
        alice.view.openViewById(VIEW_REMOTE_SCREEN_ID);
        assertEquals(true, alice.rSV.isRemoteScreenViewOpen());
    }

    @Test
    public void connectWithoutGUI() throws RemoteException {
        log.trace("xmppConnect");
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        log.trace("captureScreenshot");
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/xmpp_connected.png"));
        assertEquals(true, alice.sarosBuddiesV.isConnectedNoGUI());
    }

    @Test
    public void disconnectWithoutGUI() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        alice.sarosBuddiesV.disconnectNoGUI();
        assertEquals(false, alice.sarosBuddiesV.isConnectedNoGUI());
    }

    @Test
    public void disconnectGUI() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        alice.sarosBuddiesV.disconnect();
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/xmpp_disconnected.png"));
        assertEquals(false, alice.sarosBuddiesV.isConnectedNoGUI());
    }

}
