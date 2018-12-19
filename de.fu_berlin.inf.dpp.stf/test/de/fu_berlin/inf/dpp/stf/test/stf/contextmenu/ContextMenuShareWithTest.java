package de.fu_berlin.inf.dpp.stf.test.stf.contextmenu;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContextMenuShareWithTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @After
  public void afterEveryTest() throws Exception {
    leaveSessionPeersFirst(ALICE);
  }

  @Test
  public void testShareWithMultipleUsers() throws Exception {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectJavaProject(Constants.PROJECT1)
        .shareWith()
        .contact(BOB.getJID());
    BOB.superBot()
        .confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);

    BOB.superBot().views().sarosView().waitUntilIsInSession();

    // TODO remove this line
    Thread.sleep(10000);
  }
}
