package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestSarosView extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testSarosView() throws RemoteException {
        alice.bot().view(VIEW_SAROS).close();
        assertEquals(false, alice.bot().isViewOpen(VIEW_SAROS));
        alice.bot().openViewById(VIEW_SAROS_ID);
        alice.bot().captureScreenshot(
            (alice.bot().getPathToScreenShot() + "/session_view.png"));
        assertEquals(true, alice.bot().isViewOpen(VIEW_SAROS));
    }

    @Test
    public void connect() throws RemoteException {
        log.trace("xmppConnect");
        alice.superBot().views().buddiesView()
            .connectWith(alice.getJID(), alice.getPassword());
        log.trace("captureScreenshot");
        alice.bot().captureScreenshot(
            (alice.bot().getPathToScreenShot() + "/xmpp_connected.png"));
        assertEquals(true, alice.superBot().views().buddiesView().isConnected());
    }

    @Test
    public void disconnect() throws RemoteException {
        alice.superBot().views().buddiesView()
            .connectWith(alice.getJID(), alice.getPassword());
        alice.superBot().views().buddiesView().disconnect();
        alice.bot().captureScreenshot(
            (alice.bot().getPathToScreenShot() + "/xmpp_disconnected.png"));
        assertEquals(false, alice.superBot().views().buddiesView()
            .isConnected());
    }

}
