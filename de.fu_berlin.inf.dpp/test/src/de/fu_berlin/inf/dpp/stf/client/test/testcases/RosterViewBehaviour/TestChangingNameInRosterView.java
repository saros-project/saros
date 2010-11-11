package de.fu_berlin.inf.dpp.stf.client.test.testcases.RosterViewBehaviour;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

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
        alice.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
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
        alice.rosterV.renameContact(bob.jid.getName(), bob.jid.getBase());
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @Test
    public void testReanmeInRosterView() throws RemoteException {
        assertTrue(alice.rosterV.hasContactWith(bob.jid));
        alice.rosterV.renameContact(bob.jid.getBase(), bob.jid.getName());
        assertTrue(alice.state.hasContactWith(bob.jid));
        assertFalse(alice.rosterV.hasContactWith(bob.jid));
        // assertTrue(alice.bot.hasContactWith(bob.jid.));
        assertTrue(alice.sessionV.isContactInSessionView(bob.jid.getBase()));
        assertTrue(alice.sessionV.isContactInSessionView(bob.jid.getName()));

    }
}
