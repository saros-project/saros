package saros.stf.test.stf.controlbot.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.net.xmpp.JID;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;

public class NetworkManipulatorTest extends StfTestCase {

  @BeforeClass
  public static void selectTester() throws Exception {
    select(ALICE, BOB);
  }

  @AfterClass
  public static void resetNetwork() throws Exception {
    ALICE.controlBot().getNetworkManipulator().unblockIncomingSessionPackets();

    ALICE.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();

    BOB.controlBot().getNetworkManipulator().unblockIncomingSessionPackets();

    BOB.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();
  }

  @Before
  public void createProjectandOpenFiles() throws Exception {
    Util.setUpSessionWithProjectAndFile("foo", "bar.txt", "bla", ALICE, BOB);
    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/bar.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "bar.txt").open();
    ALICE.remoteBot().editor("bar.txt").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "bar.txt").open();
    BOB.remoteBot().editor("bar.txt").waitUntilIsActive();
  }

  @After
  public void unblockNetworkAndCleanUp() throws Exception {
    ALICE.controlBot().getNetworkManipulator().unblockIncomingSessionPackets();

    ALICE.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();

    BOB.controlBot().getNetworkManipulator().unblockIncomingSessionPackets();

    BOB.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();

    leaveSessionPeersFirst(ALICE);

    closeAllShells();
    closeAllEditors();
    clearWorkspaces();
  }

  @Test
  public void testBlockAndUnblockOutgoingTrafficOnAlice() throws Exception {
    ALICE.controlBot().getNetworkManipulator().blockOutgoingSessionPackets();

    ALICE.remoteBot().editor("bar.txt").typeText("foo");

    Thread.sleep(1000);

    assertEquals("bla", BOB.remoteBot().editor("bar.txt").getText());

    ALICE.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    assertEquals("foobla", BOB.remoteBot().editor("bar.txt").getText());
  }

  @Test
  public void testBlockAndUnblockIncommingTrafficOnBob() throws Exception {
    BOB.controlBot().getNetworkManipulator().blockIncomingSessionPackets();

    ALICE.remoteBot().editor("bar.txt").typeText("foo");

    Thread.sleep(1000);

    assertEquals("bla", BOB.remoteBot().editor("bar.txt").getText());

    BOB.controlBot().getNetworkManipulator().unblockIncomingSessionPackets();

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    assertEquals("foobla", BOB.remoteBot().editor("bar.txt").getText());
  }

  @Test
  public void testBlockNonExistingSessionJID() throws Exception {
    BOB.controlBot().getNetworkManipulator().blockIncomingSessionPackets(new JID("my@example.com"));

    ALICE.remoteBot().editor("bar.txt").typeText("foo");

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    assertEquals("foobla", BOB.remoteBot().editor("bar.txt").getText());
  }

  @Test
  public void testBlockAndUnblockOutgoingTrafficOnAliceToSingleJID() throws Exception {
    ALICE.controlBot().getNetworkManipulator().blockOutgoingSessionPackets(BOB.getJID());

    ALICE.remoteBot().editor("bar.txt").typeText("foo");

    Thread.sleep(1000);

    assertEquals("bla", BOB.remoteBot().editor("bar.txt").getText());

    ALICE.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets(BOB.getJID());

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    assertEquals("foobla", BOB.remoteBot().editor("bar.txt").getText());
  }

  @Test
  public void testSynchronizeOnActivityQueue() throws Exception {

    BOB.superBot().internal().createFile("foo", "bigfile", 10 * 1024 * 1024, true);

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    assertTrue("synchronization failed", ALICE.superBot().internal().existsResource("foo/bigfile"));

    assertEquals(10 * 1024 * 1024, ALICE.superBot().internal().getFileSize("foo", "bigfile"));
  }
}
