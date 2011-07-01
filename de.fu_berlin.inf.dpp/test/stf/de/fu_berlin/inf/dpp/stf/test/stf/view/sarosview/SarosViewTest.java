package de.fu_berlin.inf.dpp.stf.test.stf.view.sarosview;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;

public class SarosViewTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @Before
    public void disconnectFromServer() throws RemoteException {
        ALICE.superBot().views().sarosView().disconnect();
    }

    @Test
    public void testSarosView() throws RemoteException {
        ALICE.remoteBot().view(VIEW_SAROS).close();
        assertEquals(false, ALICE.remoteBot().isViewOpen(VIEW_SAROS));
        ALICE.remoteBot().openViewById(VIEW_SAROS_ID);
        assertEquals(true, ALICE.remoteBot().isViewOpen(VIEW_SAROS));
    }

    @Test
    public void connect() throws RemoteException {
        ALICE.superBot().views().sarosView()
            .connectWith(ALICE.getJID(), ALICE.getPassword());
        assertEquals(true, ALICE.superBot().views().sarosView().isConnected());
    }

    @Test
    public void connectWith() throws RemoteException {
        ALICE.superBot().views().sarosView()
            .connectWith(BOB.getJID(), BOB.getPassword());

        assertEquals(true, ALICE.superBot().views().sarosView().isConnected());

        assertTrue(ALICE.superBot().menuBar().saros().preferences()
            .isAccountActive(BOB.getJID()));
    }

    @Test
    public void connectWithActiveAccount() throws RemoteException {
        ALICE.superBot().views().sarosView().connectWithActiveAccount();
        assertTrue(ALICE.superBot().views().sarosView().isConnected());
    }

    @Test
    public void disconnect() throws RemoteException {
        ALICE.superBot().views().sarosView()
            .connectWith(ALICE.getJID(), ALICE.getPassword());
        ALICE.superBot().views().sarosView().disconnect();
        assertEquals(false, ALICE.superBot().views().sarosView().isConnected());
    }
}
