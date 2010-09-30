package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestBasicSarosElements {
    private static final Logger log = Logger
        .getLogger(TestBasicSarosElements.class);
    private static Musician alice = InitMusician.newAlice();

    @AfterClass
    public static void afterClass() throws RemoteException {
        alice.bot.resetSaros();
    }

    @After
    public void cleanup() throws RemoteException {
        alice.bot.resetWorkbench();
    }

    @Test
    public void testSessionView() throws RemoteException {
        alice.bot.closeSessionView();
        assertEquals(false, alice.bot.isSharedSessionViewOpen());
        alice.bot.openSessionView();
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.bot.isSharedSessionViewOpen());
    }

    @Test
    public void testRosterView() throws RemoteException {
        alice.bot.closeRosterView();
        assertEquals(false, alice.bot.isRosterViewOpen());
        alice.bot.openRosterView();
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/roster_view.png"));
        assertEquals(true, alice.bot.isRosterViewOpen());
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
        log.trace("xmppDisconnect");
        alice.bot.xmppDisconnect();
        log.trace("xmppConnect");
        alice.bot.xmppConnect(alice.jid, alice.password);
        log.trace("captureScreenshot");
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/xmpp_connected.png"));
        assertEquals(true, alice.bot.isConnectedByXMPP());
    }

    @Test
    public void testXmppDisconnect() throws RemoteException {
        alice.bot.xmppConnect(alice.jid, alice.password);
        alice.bot.xmppDisconnect();
        alice.bot
            .captureScreenshot((alice.state.getPathToScreenShot() + "/xmpp_disconnected.png"));
        assertEquals(false, alice.bot.isConnectedByXMPP());
    }
}
