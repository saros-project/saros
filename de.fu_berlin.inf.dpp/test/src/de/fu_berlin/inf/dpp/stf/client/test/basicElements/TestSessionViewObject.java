package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;

public class TestSessionViewObject {
    private static final Logger log = Logger
        .getLogger(TestSessionViewObject.class);
    private static Musician alice;

    @BeforeClass
    public static void initMusican() {
        alice = InitMusician.newAlice();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        alice.bot.resetSaros();
    }

    @Before
    public void startUp() throws RemoteException {
        alice.bot.openSarosViews();
    }

    @After
    public void cleanUp() throws RemoteException {
        alice.bot.resetWorkbench();
        alice.rosterV.xmppDisconnect();
    }

    @Test
    public void testSetFocusOnSessionView() throws RemoteException {
        log.debug("alice set focus on session view.");
        // alice.sessionV.setFocusOnSessionView();
        // assertTrue(alice.sessionV.isSessionViewActive());
        // log.trace("alice close session view.");
        // alice.sessionV.closeSessionView();
        // assertFalse(alice.sessionV.isSessionViewActive());
        // log.trace("alice open session view again");
        // alice.sessionV.openSessionView();
        // assertTrue(alice.sessionV.isSessionViewActive());
        // log.trace("alice focus on roster view.");
        // alice.rosterV.setFocusOnRosterView();
        // assertFalse(alice.sessionV.isSessionViewActive());
        // log.trace("testSetFocusOnSessionView is done.");
    }
}
