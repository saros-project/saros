package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestBasicSarosElements extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    // @After
    // public void runAfterEveryTest() throws RemoteException {
    // disConnectByActiveTesters();
    // }

    @Test
    public void testSessionView() throws RemoteException {
        alice.bot().view(VIEW_SAROS_SESSION).close();
        assertEquals(false, alice.bot().isViewOpen(VIEW_SAROS_SESSION));
        alice.bot().openViewById(VIEW_SAROS_SESSION_ID);
        alice.bot().captureScreenshot(
            (alice.bot().getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.bot().isViewOpen(VIEW_SAROS_SESSION));
    }

    @Test
    public void testRosterView() throws RemoteException {
        alice.bot().view(VIEW_SAROS_BUDDIES).close();
        assertEquals(false, alice.bot().isViewOpen(VIEW_SAROS_BUDDIES));
        alice.bot().openViewById(VIEW_SAROS_BUDDIES_ID);
        alice.bot().captureScreenshot(
            (alice.bot().getPathToScreenShot() + "/roster_view.png"));
        assertEquals(true, alice.bot().isViewOpen(VIEW_SAROS_BUDDIES));
    }

    @Test
    public void testChatView() throws RemoteException {
        alice.bot().view(VIEW_SAROS_CHAT).close();
        assertEquals(false, alice.bot().isViewOpen(VIEW_SAROS_CHAT));
        alice.bot().openViewById(VIEW_SAROS_CHAT_ID);
        assertEquals(true, alice.bot().isViewOpen(VIEW_SAROS_CHAT));
    }

    @Test
    public void testRemoteScreenView() throws RemoteException {
        alice.bot().view(VIEW_REMOTE_SCREEN).close();
        assertEquals(false, alice.bot().isViewOpen(VIEW_REMOTE_SCREEN));
        alice.bot().openViewById(VIEW_REMOTE_SCREEN_ID);
        assertEquals(true, alice.bot().isViewOpen(VIEW_REMOTE_SCREEN));
    }

    @Test
    public void connectWithoutGUI() throws RemoteException {
        log.trace("xmppConnect");
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        log.trace("captureScreenshot");
        alice.bot().captureScreenshot(
            (alice.bot().getPathToScreenShot() + "/xmpp_connected.png"));
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
        alice.bot().captureScreenshot(
            (alice.bot().getPathToScreenShot() + "/xmpp_disconnected.png"));
        assertEquals(false, alice.sarosBuddiesV.isConnectedNoGUI());
    }

}
