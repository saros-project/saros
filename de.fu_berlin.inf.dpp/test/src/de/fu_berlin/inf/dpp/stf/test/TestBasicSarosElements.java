package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertEquals;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestBasicSarosElements {
    // bots
    protected static Musician bot;

    @BeforeClass
    public static void configureInvitee() throws RemoteException,
        NotBoundException {
        bot = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        bot.initRmi();

        bot.activeMusican();

        if (bot.isViewOpen("Welcome"))
            bot.closeViewByTitle("Welcome");

        bot.openSarosViews();
        bot.xmppConnect();
        bot.waitForConnect();
    }

    @AfterClass
    public static void afterClass() {
        try {
            if (bot.isConnectedByXMPP())
                bot.xmppDisconnect();
        } catch (RemoteException e) {
            // ignore
        }
    }

    @Before
    public void xmppConnect() {
        try {
            if (!bot.isRosterViewOpen())
                bot.openRosterView();
            if (!bot.isConnectedByXMPP()) {
                bot.xmppConnect();
                bot.waitForConnect();
            }
        } catch (RemoteException e) {
            // ignore cleanup
        }
    }

    @After
    public void xmppDisconnect() {
        try {
            if (bot.isConnectedByXMPP())
                bot.xmppDisconnect();
        } catch (RemoteException e) {
            // ignore cleanup
        }
    }

    @Test
    public void testSessionView() throws RemoteException {
        if (bot.isViewOpen("Shared Project Session"))
            bot.closeViewByTitle("Shared Project Session");
        assertEquals(false, bot.isViewOpen("Shared Project Session"));
        bot.openSessionView();
        bot.captureScreenshot(bot.getPathToScreenShot() + "/session_view.png");
        assertEquals(true, bot.isViewOpen("Shared Project Session"));
    }

    @Test
    public void testRosterView() throws RemoteException {
        if (bot.isViewOpen("Roster"))
            bot.closeViewByTitle("Roster");
        assertEquals(false, bot.isViewOpen("Roster"));
        bot.openRosterView();
        bot.captureScreenshot(bot.getPathToScreenShot() + "/roster_view.png");
        assertEquals(true, bot.isViewOpen("Roster"));
    }

    @Test
    public void testXmppConnect() throws RemoteException {
        if (bot.isConnectedByXMPP())
            bot.xmppDisconnect();
        bot.xmppConnect();
        bot
            .captureScreenshot(bot.getPathToScreenShot()
                + "/xmpp_connected.png");
        bot.waitForConnect();
        assertEquals(true, bot.isConnectedByXMPP());
    }

    @Test
    public void testXmppDisconnect() throws RemoteException {
        if (!bot.isConnectedByXMPP()) {
            bot.xmppConnect();
            bot.waitForConnect();
        }

        bot.xmppDisconnect();
        bot.sleep(2000);
        bot.captureScreenshot(bot.getPathToScreenShot()
            + "/xmpp_disconnected.png");
        assertEquals(false, bot.isConnectedByXMPP());
    }
}
