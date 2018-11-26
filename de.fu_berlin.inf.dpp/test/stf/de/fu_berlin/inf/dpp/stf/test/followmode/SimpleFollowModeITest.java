package de.fu_berlin.inf.dpp.stf.test.followmode;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import java.util.List;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-44_simple_follow_mode_1")
public class SimpleFollowModeITest extends StfTestCase {

  private final String fileContent =
      "1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n";

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testSimpleFollowMode() throws Exception {
    Util.setUpSessionWithProjectAndFile("foo", "readme.txt", fileContent, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();

    ALICE.remoteBot().editor("readme.txt").typeText("123456789");

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    ALICE.remoteBot().editor("readme.txt").selectCurrentLine();

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    assertEquals(
        ALICE.remoteBot().editor("readme.txt").getSelection(),
        BOB.remoteBot().editor("readme.txt").getSelectionByAnnotation());

    ALICE.remoteBot().editor("readme.txt").navigateTo(0, 2);

    BOB.remoteBot().editor("readme.txt").navigateTo(0, 0);

    ALICE
        .remoteBot()
        .editor("readme.txt")
        .pressShortcut(IKeyLookup.BACKSPACE_NAME, IKeyLookup.BACKSPACE_NAME);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    assertEquals(
        ALICE.remoteBot().editor("readme.txt").getTextOnCurrentLine(),
        BOB.remoteBot().editor("readme.txt").getTextOnCurrentLine());

    int lineCount = ALICE.remoteBot().editor("readme.txt").getLineCount();

    ALICE.remoteBot().editor("readme.txt").navigateTo(lineCount - 1, 0);

    // type one character, otherwise bobs view port does not change because
    // navigateTo does not trigger events

    ALICE.remoteBot().editor("readme.txt").typeText("0");

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    List<Integer> viewport = BOB.remoteBot().editor("readme.txt").getViewport();

    assertEquals(lineCount, viewport.get(0) + viewport.get(1));

    ALICE.superBot().internal().createFile("foo", "help.txt", "HELP ME !");
    ALICE.superBot().views().packageExplorerView().selectFile("foo", "help.txt").open();

    BOB.remoteBot().editor("help.txt").waitUntilIsActive();
  }
}
