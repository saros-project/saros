package saros.stf.test.followmode;

import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.test.Constants;

public class FollowModeTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testBobFollowAlice() throws Exception {
    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).setTextFromFile(Constants.CP1);

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).save();

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();
    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
    assertTrue(BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).isFollowing());
    assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());

    String clsContentOfAlice =
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG1, Constants.CLS1));

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileContentSame(
            clsContentOfAlice,
            Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG1, Constants.CLS1));
    String clsContentOfBob =
        BOB.superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG1, Constants.CLS1));
    assertTrue(clsContentOfBob.equals(clsContentOfAlice));

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
    BOB.remoteBot().editor(Constants.CLS2_SUFFIX).waitUntilIsActive();
    assertTrue(BOB.remoteBot().editor(Constants.CLS2_SUFFIX).isActive());

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).followParticipant();

    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).show();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();

    assertTrue(ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).isFollowing());

    assertTrue(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
  }
}
