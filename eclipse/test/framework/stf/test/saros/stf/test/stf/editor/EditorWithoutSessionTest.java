package de.fu_berlin.inf.dpp.stf.test.stf.editor;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

public class EditorWithoutSessionTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testConcurrentEditing() throws Exception {
    Util.createProjectWithEmptyFile(Constants.PROJECT1, Constants.FILE3, ALICE, BOB);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile(Constants.PROJECT1, Constants.FILE3)
        .open();

    ALICE.remoteBot().editor(Constants.FILE3).typeText(ALICE.toString());

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectFile(Constants.PROJECT1, Constants.FILE3)
        .open();

    BOB.remoteBot().editor(Constants.FILE3).typeText(BOB.toString());

    assertEquals(ALICE.toString(), ALICE.remoteBot().editor(Constants.FILE3).getText());
    assertEquals(BOB.toString(), BOB.remoteBot().editor(Constants.FILE3).getText());
  }
}
