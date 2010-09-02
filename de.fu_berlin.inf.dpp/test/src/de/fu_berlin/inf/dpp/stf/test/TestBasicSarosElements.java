package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertEquals;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.AfterClass;
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

        if (bot.isViewOpen("Welcome"))
            bot.closeViewByTitle("Welcome");

        bot.openSarosViews();
        bot.xmppConnect();
        bot.waitForConnect();
    }

    @AfterClass
    public static void afterClass() {
        try {
            if (!bot.isRosterViewOpen())
                bot.openRosterView();
            if (bot.isConnectedByXMPP())
                bot.xmppDisconnect();
        } catch (RemoteException e) {
            // ignore
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
        if (!bot.isRosterViewOpen())
            bot.openRosterView();
        if (bot.isConnectedByXMPP())
            bot.xmppDisconnect();

        assertEquals(false, bot.isConnectedByXMPP());

        bot.xmppConnect();
        bot.captureScreenshot(bot.getPathToScreenShot() + "/xmpp_connected.png");
        bot.waitForConnect();
        assertEquals(true, bot.isConnectedByXMPP());
    }

    @Test
    public void testXmppDisconnect() throws RemoteException {
        if (!bot.isRosterViewOpen())
            bot.openRosterView();
        if (!bot.isConnectedByXMPP()) {
            bot.xmppConnect();
            bot.waitForConnect();
        }

        assertEquals(true, bot.isConnectedByXMPP());

        bot.xmppDisconnect();
        bot.sleep(2000);
        bot.captureScreenshot(bot.getPathToScreenShot()
            + "/xmpp_disconnected.png");
        assertEquals(false, bot.isConnectedByXMPP());
    }
}
