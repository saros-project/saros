package saros.stf.test.stf.editor;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.test.Constants;

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
