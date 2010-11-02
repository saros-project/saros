package de.fu_berlin.inf.dpp.stf.client.test.RosterViewBehaviour;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.SarosConstant;

public class TestChangingNameInRosterView {
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    protected static Musician carl = InitMusician.newCarl();
    protected static Musician alice;
    protected static Musician bob;

    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.buildSessionSequential(BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.bot.renameContact(bob.jid.getName(), bob.jid.getBase());
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @Test
    public void testReanmeInRosterView() throws RemoteException {
        assertTrue(alice.bot.hasContactWith(bob.jid));
        alice.bot.renameContact(bob.jid.getBase(), bob.jid.getName());
        assertTrue(alice.state.hasContactWith(bob.jid));
        assertFalse(alice.bot.hasContactWith(bob.jid));
        // assertTrue(alice.bot.hasContactWith(bob.jid.));
        assertTrue(alice.bot.isContactInSessionView(bob.jid.getBase()));
        assertTrue(alice.bot.isContactInSessionView(bob.jid.getName()));

    }
}
