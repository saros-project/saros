package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;

public class TestBasicSarosElements {
    private static final Logger log = Logger
        .getLogger(TestBasicSarosElements.class);
    private static Musician alice;

    @BeforeClass
    public static void initMusican() {
        alice = InitMusician.newAlice();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        alice.workbench.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.workbench.resetWorkbench();
        alice.rosterV.disconnect();
    }

    @Test
    public void testSessionView() throws RemoteException {
        alice.sessionV.closeSessionView();
        assertEquals(false, alice.sessionV.isSessionViewOpen());
        alice.sessionV.openSessionView();
        alice.basic
            .captureScreenshot((alice.state.getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.sessionV.isSessionViewOpen());
    }

    @Test
    public void testRosterView() throws RemoteException {
        alice.rosterV.closeRosterView();
        assertEquals(false, alice.rosterV.isRosterViewOpen());
        alice.rosterV.openRosterView();
        alice.basic
            .captureScreenshot((alice.state.getPathToScreenShot() + "/roster_view.png"));
        assertEquals(true, alice.rosterV.isRosterViewOpen());
    }

    @Test
    public void testChatView() throws RemoteException {
        alice.chatV.closeChatView();
        assertEquals(false, alice.chatV.isChatViewOpen());
        alice.chatV.openChatView();
        assertEquals(true, alice.chatV.isChatViewOpen());
    }

    @Test
    public void testRemoteScreenView() throws RemoteException {
        alice.rSV.closeRemoteScreenView();
        assertEquals(false, alice.rSV.isRemoteScreenViewOpen());
        alice.rSV.openRemoteScreenView();
        assertEquals(true, alice.rSV.isRemoteScreenViewOpen());
    }

    @Test
    public void testXmppConnect() throws RemoteException {
        log.trace("xmppConnect");
        alice.rosterV.connect(alice.jid, alice.password);
        log.trace("captureScreenshot");
        alice.basic
            .captureScreenshot((alice.state.getPathToScreenShot() + "/xmpp_connected.png"));
        assertEquals(true, alice.rosterV.isConnected());
    }

    @Test
    public void testXmppDisconnect() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        alice.rosterV.disconnect();
        alice.basic
            .captureScreenshot((alice.state.getPathToScreenShot() + "/xmpp_disconnected.png"));
        assertEquals(false, alice.rosterV.isConnected());
    }
}
