package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

public class HostInvitesBelatedlyTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  /**
   * Steps:
   *
   * <p>1. ALICE edits the file CLS but don't saves it.
   *
   * <p>2. BOB edits the file CLS in the project currently used and saves it.
   *
   * <p>3. ALICE edits the file CLS2 but don't saves it.
   *
   * <p>4. BOB edits the file CLS2 in the project currently used and don't saves it.
   *
   * <p>5. ALICE invites BOB.
   *
   * <p>6. The question about the changed files at BOB is answered with YES.
   *
   * <p>Expected Results:
   *
   * <p>7. BOB has the same project like host.
   *
   * <p>FIXME: There are some bugs, if BOB's editors are not closed, BOB has the different project
   * like host.
   *
   * @throws CoreException
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testSynchronizeWithOpenDirtyEditorsAtInviteesSide() throws Exception {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1, Constants.CLS2);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1, Constants.CLS2);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).setTextFromFile(Constants.CP1);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).setTextFromFile(Constants.CP1_CHANGE);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
        .open();

    ALICE.remoteBot().editor(Constants.CLS2_SUFFIX).setTextFromFile(Constants.CP2);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
        .open();

    BOB.remoteBot().editor(Constants.CLS2_SUFFIX).setTextFromFile(Constants.CP2_CHANGE);

    Util.buildSessionConcurrently(
        Constants.PROJECT1, TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);

    Thread.sleep(5000);

    String CLSContentOfAlice = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    String CLS2ContentOfAlice = ALICE.remoteBot().editor(Constants.CLS2_SUFFIX).getText();

    String CLSContentOfBob = BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    String CLS2ContentOfBob = BOB.remoteBot().editor(Constants.CLS2_SUFFIX).getText();

    assertEquals(CLSContentOfAlice, CLSContentOfBob);
    assertEquals(CLS2ContentOfAlice, CLS2ContentOfBob);
  }
}
