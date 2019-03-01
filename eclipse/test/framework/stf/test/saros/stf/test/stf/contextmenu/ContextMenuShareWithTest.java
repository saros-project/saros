package saros.stf.test.stf.contextmenu;

import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.shared.Constants.TypeOfCreateProject;
import saros.stf.test.Constants;

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
