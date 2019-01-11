package de.fu_berlin.inf.dpp.stf.test.session;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-18_creating_new_files")
public class CreatingNewFileTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testCreatingNewFileTest() throws Exception {

    CARL.superBot().internal().createProject("foo");

    Util.buildSessionConcurrently("foo", TypeOfCreateProject.NEW_PROJECT, CARL, ALICE, BOB);

    ALICE.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo");
    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo");

    CARL.superBot().internal().createFile("foo", "readme.txt", "this is a test case");

    ALICE.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");
    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    CARL.superBot().views().sarosView().selectUser(ALICE.getJID()).restrictToReadOnlyAccess();

    CARL.superBot().views().sarosView().selectUser(ALICE.getJID()).waitUntilHasReadOnlyAccess();

    // Lin's fault not mine !

    // BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID()).waitUntilHasReadOnlyAccess();

    // ALICE.superBot().views().sarosView().selectParticipant(ALICE.getJID()).waitUntilHasReadOnlyAccess();

    CARL.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();
    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();

    assertTrue(CARL.superBot().views().sarosView().isFollowing());
    assertTrue(BOB.superBot().views().sarosView().isFollowing());

    ALICE.superBot().internal().createFile("foo", "bar/readme.txt", "not visible");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "bar", "readme.txt").open();

    ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();
    ALICE
        .remoteBot()
        .editor("readme.txt")
        .typeText(
            "eene meene miste es rappelt in der kiste, eene meene meck und du bist weck ! weck bist du noch lange nicht ...");

    assertFalse("Carls editor must not be opened", CARL.remoteBot().isEditorOpen("readme.txt"));
    assertFalse("Bobs editor must not be opened", BOB.remoteBot().isEditorOpen("readme.txt"));

    assertFalse(
        "Alices created file must not be marked as shared (CARL)",
        CARL.superBot().views().packageExplorerView().isResourceShared("foo/bar/readme.txt"));

    assertFalse(
        "Alices created file must not be marked as shared (BOB)",
        BOB.superBot().views().packageExplorerView().isResourceShared("foo/bar/readme.txt"));

    assertEquals(
        "Alice had changed a file during read only access while typing",
        ALICE.remoteBot().editor("readme.txt").getText(),
        "not visible");

    try {
      ALICE.superBot().views().sarosView().waitUntilIsInconsistencyDetected();
    } catch (TimeoutException e) {
      fail("ALICE should have received an inconsistency warning, " + e.getMessage());
    }
  }
}
