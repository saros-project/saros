package saros.stf.test.stf.view.sarosview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.shared.Constants.VIEW_SAROS;
import static saros.stf.shared.Constants.VIEW_SAROS_ID;

import java.rmi.RemoteException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;

public class SarosViewTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
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
    ALICE.superBot().views().sarosView().connectWith(ALICE.getJID(), ALICE.getPassword(), true);
    assertEquals(true, ALICE.superBot().views().sarosView().isConnected());
  }

  @Test
  public void connectWith() throws RemoteException {
    ALICE.superBot().views().sarosView().connectWith(BOB.getJID(), BOB.getPassword(), false);

    assertEquals(true, ALICE.superBot().views().sarosView().isConnected());

    assertEquals(BOB.getJID(), ALICE.superBot().menuBar().saros().preferences().getActiveAccount());
  }

  @Test
  public void connectWithActiveAccount() throws RemoteException {
    ALICE.superBot().views().sarosView().connect();
    assertTrue(ALICE.superBot().views().sarosView().isConnected());
  }

  @Test
  public void disconnect() throws RemoteException {
    ALICE.superBot().views().sarosView().connectWith(ALICE.getJID(), ALICE.getPassword(), true);
    ALICE.superBot().views().sarosView().disconnect();
    assertEquals(false, ALICE.superBot().views().sarosView().isConnected());
  }
}
