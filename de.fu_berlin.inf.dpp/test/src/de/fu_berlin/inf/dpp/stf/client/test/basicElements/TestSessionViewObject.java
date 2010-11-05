package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestSessionViewObject {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;

    private static final Logger log = Logger
        .getLogger(TestSessionViewObject.class);
    private static Musician alice;
    private static Musician bob;

    @BeforeClass
    public static void initMusican() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        log.trace("alice create a new proejct and a new class.");
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        log.trace("alice share session with bob.");
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @Before
    public void startUp() throws RemoteException {
        bob.bot.openSarosViews();
        alice.bot.openSarosViews();
        if (!alice.state.isInSession()) {
            bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT;
            alice.buildSessionSequential(PROJECT,
                SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        }
    }

    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();

    }

    @Test
    public void testSetFocusOnSessionView() throws RemoteException {
        log.trace("alice set focus on session view.");
        alice.sessionV.setFocusOnSessionView();
        assertTrue(alice.sessionV.isSessionViewActive());
        log.trace("alice close session view.");
        alice.sessionV.closeSessionView();
        assertFalse(alice.sessionV.isSessionViewActive());
        log.trace("alice open session view again");
        alice.sessionV.openSessionView();
        assertTrue(alice.sessionV.isSessionViewActive());
        log.trace("alice focus on roster view.");
        alice.rosterV.setFocusOnRosterView();
        assertFalse(alice.sessionV.isSessionViewActive());
        log.trace("testSetFocusOnSessionView is done.");
    }

    @Test
    public void testIsInSession() throws RemoteException, InterruptedException {
        assertTrue(alice.sessionV.isInSession());
        assertTrue(alice.state.isInSession());
        assertTrue(bob.sessionV.isInSession());
        assertTrue(bob.state.isInSession());
        alice.leaveSessionFirst(bob);
        assertFalse(alice.sessionV.isInSession());
        assertFalse(alice.state.isInSession());
        assertFalse(bob.sessionV.isInSession());
        assertFalse(bob.state.isInSession());
    }

    @Test
    public void testGiveDriverRole() throws RemoteException {
        log.trace("alice give bob the driver role.");
        alice.sessionV.giveDriverRole(bob.getPlainJid());
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(bob.state.isDriver(bob.jid));
    }
}
