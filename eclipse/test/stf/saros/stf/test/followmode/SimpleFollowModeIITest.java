package saros.stf.test.followmode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import java.util.List;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;

@TestLink(id = "Saros-44_simple_follow_mode_2")
public class SimpleFollowModeIITest extends StfTestCase {

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

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).restrictToReadOnlyAccess();

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();

    int lineCount = ALICE.remoteBot().editor("readme.txt").getLineCount();

    ALICE.remoteBot().editor("readme.txt").navigateTo(lineCount - 1, 0);

    ALICE.remoteBot().editor("readme.txt").typeText("123456789");

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    List<Integer> viewport = BOB.remoteBot().editor("readme.txt").getViewport();

    assertEquals(lineCount, viewport.get(0) + viewport.get(1));

    BOB.remoteBot().editor("readme.txt").navigateTo(lineCount - 1, 0);
    BOB.remoteBot().editor("readme.txt").typeText("0");

    try {
      BOB.remoteBot().shell("Read-Only Notification");
    } catch (WidgetNotFoundException e) {
      fail(
          "BOB got no notification that he has read only access, balloon window is not present: "
              + e.getMessage());
    }

    BOB.remoteBot().editor("readme.txt").selectCurrentLine();

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    assertEquals(
        BOB.remoteBot().editor("readme.txt").getSelection(),
        ALICE.remoteBot().editor("readme.txt").getSelectionByAnnotation());
  }
}
