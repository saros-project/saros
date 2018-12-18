package de.fu_berlin.inf.dpp.stf.test.permissions;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.TB_INCONSISTENCY_DETECTED;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.TB_NO_INCONSISTENCIES;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

public class WriteAccessChangeAndImmediateWriteTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  /**
   * Steps:
   *
   * <p>1. ALICE restrict to read only access.
   *
   * <p>2. BOB try to create inconsistency (set Text)
   *
   * <p>3. ALICE grants write access to BOB
   *
   * <p>4. BOB immediately begins to write it.
   *
   * <p>Expected Results:
   *
   * <p>2. inconsistency should occur by BOB.
   *
   * <p>4. no inconsistency occur by BOB.
   */
  @Test
  public void testFollowModeByOpenClassbyAlice() throws Exception {
    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).restrictToReadOnlyAccess();
    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();
    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).setTextFromFile(Constants.CP1);
    BOB.superBot().views().sarosView().waitUntilIsInconsistencyDetected();

    assertTrue(
        BOB.remoteBot()
            .view(VIEW_SAROS)
            .toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*")
            .isEnabled());
    BOB.superBot().views().sarosView().resolveInconsistency();

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).grantWriteAccess();
    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).setTextFromFile(Constants.CP2);

    Thread.sleep(5000);

    assertFalse(BOB.remoteBot().view(VIEW_SAROS).toolbarButton(TB_NO_INCONSISTENCIES).isEnabled());
  }
}
