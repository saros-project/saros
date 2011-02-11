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
        alice.view.closeById(VIEW_SAROS_SESSION_ID);
        assertEquals(false, alice.commonWidgets().view(VIEW_SAROS_SESSION)
            .isOpen());
        alice.commonWidgets().view(VIEW_SAROS_SESSION).openById();
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.commonWidgets().view(VIEW_SAROS_SESSION)
            .isOpen());
    }

    @Test
    public void testRosterView() throws RemoteException {
        alice.commonWidgets().view(VIEW_SAROS_BUDDIES).close();
        assertEquals(false, alice.commonWidgets().view(VIEW_SAROS_BUDDIES)
            .isOpen());
        alice.commonWidgets().view(VIEW_SAROS_BUDDIES).openById();
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/roster_view.png"));
        assertEquals(true, alice.commonWidgets().view(VIEW_SAROS_BUDDIES)
            .isOpen());
    }

    @Test
    public void testChatView() throws RemoteException {
        alice.commonWidgets().view(VIEW_SAROS_CHAT).close();
        assertEquals(false, alice.commonWidgets().view(VIEW_SAROS_CHAT)
            .isOpen());
        alice.commonWidgets().view(VIEW_SAROS_CHAT).openById();
        assertEquals(true, alice.commonWidgets().view(VIEW_SAROS_CHAT).isOpen());
    }

    @Test
    public void testRemoteScreenView() throws RemoteException {
        alice.commonWidgets().view(VIEW_REMOTE_SCREEN).close();
        assertEquals(false, alice.commonWidgets().view(VIEW_REMOTE_SCREEN)
            .isOpen());
        alice.commonWidgets().view(VIEW_REMOTE_SCREEN).openById();
        assertEquals(true, alice.commonWidgets().view(VIEW_REMOTE_SCREEN)
            .isOpen());
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
