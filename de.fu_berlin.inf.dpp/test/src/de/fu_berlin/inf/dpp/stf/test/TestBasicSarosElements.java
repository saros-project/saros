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
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

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
        try {
            bot.xmppDisconnect();
        } catch (RemoteException e) {
            // ignore
        }
    }

    @Test
    public void testSessionView() throws RemoteException {
        bot.closeViewByTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
        assertEquals(false,
            bot.isViewOpen(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION));
        bot.openSessionView();
        bot.captureScreenshot(bot.getPathToScreenShot() + "/session_view.png");
        assertEquals(true,
            bot.isViewOpen(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION));
    }

    @Test
    public void testRosterView() throws RemoteException {
        bot.closeViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
        assertEquals(false, bot.isViewOpen(SarosConstant.VIEW_TITLE_ROSTER));
        bot.openRosterView();
        bot.captureScreenshot(bot.getPathToScreenShot() + "/roster_view.png");
        assertEquals(true, bot.isViewOpen(SarosConstant.VIEW_TITLE_ROSTER));
    }

    @Test
    public void testXmppConnect() throws RemoteException {
        log.trace("xmppDisconnect");
        bot.xmppDisconnect();
        log.trace("xmppConnect");
        bot.xmppConnect();
        log.trace("captureScreenshot");
        bot.captureScreenshot(bot.getPathToScreenShot() + "/xmpp_connected.png");
        log.trace("waitForConnect");
        bot.waitForConnect();
        assertEquals(true, bot.isConnectedByXMPP());
    }

    @Test
    public void testXmppDisconnect() throws RemoteException {
        bot.xmppConnect();
        bot.waitForConnect();
        bot.xmppDisconnect();

        bot.captureScreenshot(bot.getPathToScreenShot()
            + "/xmpp_disconnected.png");
        assertEquals(false, bot.isConnectedByXMPP());
    }
}
