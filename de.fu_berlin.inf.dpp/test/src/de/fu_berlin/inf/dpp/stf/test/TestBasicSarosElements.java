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
        bot.openSarosViews();
        bot.xmppConnect();
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
            if (!bot.isConnectedByXMPP())
                bot.xmppConnect();
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
        bot.captureScreenshot(BotConfiguration.TEMPDIR + "/session_view.png");
        assertEquals(true, bot.isViewOpen("Shared Project Session"));
    }

    @Test
    public void testRosterView() throws RemoteException {
        if (bot.isViewOpen("Roster"))
            bot.closeViewByTitle("Roster");
        assertEquals(false, bot.isViewOpen("Roster"));
        bot.openRosterView();
        bot.captureScreenshot(BotConfiguration.TEMPDIR + "/roster_view.png");
        assertEquals(true, bot.isViewOpen("Roster"));
    }

    @Test
    public void testXmppConnect() throws RemoteException {
        if (bot.isConnectedByXMPP())
            bot.xmppDisconnect();
        bot.xmppConnect();
        bot.sleep(1000);
        bot.captureScreenshot(BotConfiguration.TEMPDIR + "/xmpp_connected.png");
        assertEquals(true, bot.isConnectedByXMPP());
    }

    @Test
    public void testXmppDisconnect() throws RemoteException {
        if (!bot.isConnectedByXMPP())
            bot.xmppConnect();

        bot.xmppDisconnect();
        bot.sleep(2000);
        bot.captureScreenshot(BotConfiguration.TEMPDIR
            + "/xmpp_disconnected.png");
        assertEquals(false, bot.isConnectedByXMPP());
    }
}
