package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertEquals;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestBasicSarosElements {
    private static final Logger log = Logger
        .getLogger(TestBasicSarosElements.class);
    // bots
    protected static Musician bot;

    @BeforeClass
    public static void configureInvitee() throws RemoteException,
        NotBoundException {
        log.debug("configureInvitee start");
        bot = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        log.debug("initBot");
        bot.initBot();
        log.debug("configureInvitee end");
    }

    @AfterClass
    public static void afterClass() {
        bot.xmppDisconnect();
    }

    @Test
    public void testSessionView() {
        bot.closeSarosSessionView();
        assertEquals(false, bot.issharedSessionViewOpen());
        bot.openSarosSessionView();
        bot.captureScreenshot(bot.getPathToScreenShot() + "/session_view.png");
        assertEquals(true, bot.issharedSessionViewOpen());
    }

    @Test
    public void testRosterView() {
        bot.closeRosterView();
        assertEquals(false, bot.isRosterViewOpen());
        bot.openRosterView();
        bot.captureScreenshot(bot.getPathToScreenShot() + "/roster_view.png");
        assertEquals(true, bot.isRosterViewOpen());
    }

    @Test
    public void testChatView() {
        bot.closeChatView();
        assertEquals(false, bot.isChatViewOpen());
        bot.openChatView();
        assertEquals(true, bot.isChatViewOpen());
    }

    @Test
    public void testRemoteScreenView() {
        bot.closeRmoteScreenView();
        assertEquals(false, bot.isRemoteScreenViewOpen());
        bot.openRemoteScreenView();
        assertEquals(true, bot.isRemoteScreenViewOpen());
    }

    @Test
    public void testXmppConnect() {
        log.trace("xmppDisconnect");
        bot.xmppDisconnect();
        log.trace("xmppConnect");
        bot.xmppConnect();
        log.trace("captureScreenshot");
        bot.captureScreenshot(bot.getPathToScreenShot() + "/xmpp_connected.png");
        assertEquals(true, bot.isConnectedByXMPP());
    }

    @Test
    public void testXmppDisconnect() {
        bot.xmppConnect();
        bot.xmppDisconnect();
        bot.captureScreenshot(bot.getPathToScreenShot()
            + "/xmpp_disconnected.png");
        assertEquals(false, bot.isConnectedByXMPP());
    }
}
