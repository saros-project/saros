package de.fu_berlin.inf.dpp.stf.test.consistency;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-84_synchronize_file_modifications_outside_of_editors")
public class ModifyFileWithoutEditorTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testCreateSameFileAtOnce() throws Exception {
    Util.setUpSessionWithProjectAndFile("foo", "readme.txt", "Chuck Norris", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().internal().append("foo", "readme.txt", " finished");

    // do not make inconsistencies
    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    BOB.superBot().internal().append("foo", "readme.txt", " World of Warcraft");

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();

    ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();
    BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

    String aliceText = ALICE.remoteBot().editor("readme.txt").getText();
    String bobText = BOB.remoteBot().editor("readme.txt").getText();
    assertEquals("Chuck Norris finished World of Warcraft", aliceText);
    assertEquals(aliceText, bobText);
  }
}
