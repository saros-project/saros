package saros.stf.test.session;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.shared.Constants.MENU_PREFERENCES;
import static saros.stf.shared.Constants.MENU_SAROS;
import static saros.stf.shared.Constants.NODE_SAROS;
import static saros.stf.shared.Constants.NODE_SAROS_NETWORK;
import static saros.stf.shared.Constants.OK;
import static saros.stf.shared.Constants.PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_GROUP;
import static saros.stf.shared.Constants.PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_IBB_CHECKBOX;
import static saros.stf.shared.Constants.PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_SOCKS5_MEDIATED_CHECKBOX;
import static saros.stf.shared.Constants.RESTORE_DEFAULTS;
import static saros.stf.shared.Constants.SHELL_PREFERNCES;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.tester.AbstractTester;
import saros.stf.client.util.Util;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import saros.stf.shared.Constants.TypeOfCreateProject;

/**
 * This test class ensures that it is possible to establish and running sessions with different
 * network transports(Socks5 Direct/Mediated and IBB).
 */
public class EstablishSessionWithDifferentTransportModesTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @AfterClass
  public static void resetTransportMode() throws Exception {
    activateTransportMode(null);
  }

  @Before
  public void runBeforeEveryTest() throws Exception {
    closeAllShells();
    closeAllEditors();
    clearWorkspaces();
    disconnectAllActiveTesters();
  }

  @Test
  public void testSessionWithSocks5() throws Exception {
    activateTransportMode(null);
    connectTesters();
    simulateSession();
  }

  @Test
  @Ignore("saros-con.imp.fu-berlin.de currently does not support that feature")
  public void testSessionWithSocks5Mediated() throws Exception {
    activateTransportMode("mediated");
    connectTesters();
    simulateSession();
  }

  @Test
  public void testSessionWithIBB() throws Exception {
    activateTransportMode("ibb");
    connectTesters();
    simulateSession();
  }

  @After
  public void runAfterEveryTest() throws Exception {
    leaveSessionPeersFirst(ALICE);
  }

  private void simulateSession() throws Exception {

    ALICE.superBot().internal().createProject("foo");
    ALICE.superBot().internal().createFile("foo", "bar.txt", "bar");

    Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);
    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/bar.txt");
    BOB.superBot().views().packageExplorerView().selectFile("foo", "bar.txt").open();
    BOB.remoteBot().editor("bar.txt").typeText("foo");
    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "bar.txt").open();

    assertEquals(
        BOB.remoteBot().editor("bar.txt").getText(), ALICE.remoteBot().editor("bar.txt").getText());
  }

  private static void connectTesters() throws Exception {
    for (AbstractTester tester : getCurrentTesters()) {
      tester.superBot().views().sarosView().connect();
    }
  }

  private static void activateTransportMode(String transportMode) throws Exception {

    for (AbstractTester tester : getCurrentTesters()) {
      tester.remoteBot().activateWorkbench();
      tester.remoteBot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();

      IRemoteBotShell shell = tester.remoteBot().shell(SHELL_PREFERNCES);

      shell.activate();
      shell.bot().tree().expandNode(NODE_SAROS).select(NODE_SAROS_NETWORK);

      shell.bot().button(RESTORE_DEFAULTS).click();

      String checkBoxText = null;

      if ("ibb".equals(transportMode))
        checkBoxText = PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_IBB_CHECKBOX;
      else if ("mediated".equals(transportMode))
        checkBoxText = PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_SOCKS5_MEDIATED_CHECKBOX;

      if (checkBoxText != null) {
        shell
            .bot()
            .checkBoxInGroup(checkBoxText, PREF_NODE_SAROS_NETWORK_TRANSPORT_MODE_GROUP)
            .select();
      }

      shell.bot().button(OK).click();
      shell.waitShortUntilIsClosed();
    }
  }
}
