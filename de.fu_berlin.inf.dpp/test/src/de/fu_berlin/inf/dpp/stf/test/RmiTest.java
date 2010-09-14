package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class RmiTest {
    private final static Logger log = Logger.getLogger(RmiTest.class);
    static {
        log.addAppender(new ConsoleAppender(new PatternLayout(
            "%-5p %d{HH:mm:ss,SSS} (%F:%L) %m%n")));
    }

    protected Musician bot;

    @Before
    public void configureBot() throws RemoteException, NotBoundException {
        bot = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bot.initBot();
    }

    @After
    public void cleanupBot() {
        bot.closeViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
    }

    @Test
    public void testWindows() throws RemoteException {
        bot.waitForConnect();
        bot.closeViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
        assertFalse(bot.isViewOpen(SarosConstant.VIEW_TITLE_ROSTER));
        bot.openRosterView();
        assertTrue(bot.isViewOpen(SarosConstant.VIEW_TITLE_ROSTER));
    }
}
