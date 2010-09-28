package de.fu_berlin.inf.dpp.stf.test.RosterViewBehaviour;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class TestChangingNameInRosterView {
    // bots
    protected static Musician bob;
    protected static Musician carl;
    protected static Musician alice;

    @BeforeClass
    public static void configureAlice() throws RemoteException,
        NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
        String projectName = BotConfiguration.PROJECTNAME;
        alice.bot.newJavaProject(projectName);
        alice.bot.newClass(projectName, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);

        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();

        alice.buildSessionSequential(BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    // @BeforeClass
    // public static void configureCarl() throws RemoteException,
    // NotBoundException {
    // carl = new Musician(new JID(BotConfiguration.JID_CARL),
    // BotConfiguration.PASSWORD_CARL, BotConfiguration.HOST_CARL,
    // BotConfiguration.PORT_CARL);
    // carl.initBot();
    // }
    //

    @AfterClass
    public static void cleanupBob() throws RemoteException {
        bob.bot.xmppDisconnect();
    }

    @AfterClass
    public static void cleanupAlice() throws RemoteException {
        alice.bot.xmppDisconnect();
    }

    //
    // @AfterClass
    // public static void cleanupCarl() throws RemoteException {
    // carl.bot.xmppDisconnect();
    // }
    //
    @After
    public void reset() throws RemoteException {
        alice.bot.renameContact(bob.jid.getName(), bob.jid.getBase());
        // bob.bot.resetWorkbench();
        // carl.bot.resetWorkbench();
    }

    @Test
    public void testReanmeInRosterView() throws RemoteException {
        assertTrue(alice.bot.hasContactWith(bob.jid));
        alice.bot.renameContact(bob.jid.getBase(), bob.jid.getName());
        assertTrue(alice.state.hasContactWith(bob.jid));
        assertFalse(alice.bot.hasContactWith(bob.jid));
        assertTrue(alice.bot.hasContactWith(bob.jid.getName()));
        assertTrue(alice.bot.isContactInSessionView(bob.jid.getBase()));
        assertTrue(alice.bot.isContactInSessionView(bob.jid.getName()));

    }
}
