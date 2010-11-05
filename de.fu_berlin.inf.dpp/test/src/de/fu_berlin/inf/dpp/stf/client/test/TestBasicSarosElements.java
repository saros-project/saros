package de.fu_berlin.inf.dpp.stf.client.test;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;

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
        alice.bot.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.bot.resetWorkbench();
        alice.rosterV.xmppDisconnect();
    }

    @Test
    public void testSessionView() throws RemoteException {
        alice.sessionV.closeSessionView();
        assertEquals(false, alice.sessionV.isSessionViewOpen());
        alice.sessionV.openSessionView();
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.sessionV.isSessionViewOpen());
    }

    @Test
    public void testRosterView() throws RemoteException {
        alice.rosterV.closeRosterView();
        assertEquals(false, alice.rosterV.isRosterViewOpen());
        alice.rosterV.openRosterView();
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/roster_view.png"));
        assertEquals(true, alice.rosterV.isRosterViewOpen());
    }

    @Test
    public void testChatView() throws RemoteException {
        alice.bot.closeChatView();
        assertEquals(false, alice.bot.isChatViewOpen());
        alice.bot.openChatView();
        assertEquals(true, alice.bot.isChatViewOpen());
    }

    @Test
    public void testRemoteScreenView() throws RemoteException {
        alice.bot.closeRemoteScreenView();
        assertEquals(false, alice.bot.isRemoteScreenViewOpen());
        alice.bot.openRemoteScreenView();
        assertEquals(true, alice.bot.isRemoteScreenViewOpen());
    }

    @Test
    public void testXmppConnect() throws RemoteException {
        log.trace("xmppConnect");
        alice.bot.xmppConnect(alice.jid, alice.password);
        log.trace("captureScreenshot");
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/xmpp_connected.png"));
        assertEquals(true, alice.rosterV.isConnectedByXMPP());
    }

    @Test
    public void testXmppDisconnect() throws RemoteException {
        alice.bot.xmppConnect(alice.jid, alice.password);
        alice.rosterV.xmppDisconnect();
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/xmpp_disconnected.png"));
        assertEquals(false, alice.rosterV.isConnectedByXMPP());
    }
}
