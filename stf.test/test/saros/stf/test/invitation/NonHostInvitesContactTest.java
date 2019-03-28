package saros.stf.test.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;
import static saros.stf.shared.Constants.ACCEPT;
import static saros.stf.shared.Constants.SHELL_SESSION_INVITATION;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import saros.session.User.Permission;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;
import saros.stf.test.Constants;

public class NonHostInvitesContactTest extends StfTestCase {

  /**
   * Preconditions:
   *
   * <ol>
   *   <li>Alice (Host, Write Access)
   *   <li>Bob (Write Access)
   *   <li>Carl (Write Access)
   * </ol>
   */
  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Before
  public void tidyUp() throws Exception {
    closeAllShells();
    closeAllEditors();
    clearWorkspaces();
  }

  /**
   * * Steps:
   *
   * <ol>
   *   <li>ALICE share project with BOB.
   *   <li>BOB invites CARL to session
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>Alice, Bob and Carl are participants and have {@link Permission#WRITE_ACCESS}.
   * </ol>
   *
   * @throws Exception
   */
  @Test
  @Ignore("Non-Host Invitation is currently deactivated")
  public void AliceInvitesBobAndBobInvitesCarlTest() throws Exception {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    Util.buildSessionSequentially(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    assertTrue(BOB.superBot().views().sarosView().isInSession());
    assertTrue(ALICE.superBot().views().sarosView().isInSession());

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    BOB.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    CARL.superBot().confirmShellAddProjectWithNewProject(Constants.PROJECT1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    assertTrue(CARL.superBot().views().sarosView().isInSession());
  }

  @Test
  @Ignore("Non-Host Invitation is currently deactivated")
  public void CarlTypesAndEverybodyGetsCarlsTextTest() throws Exception {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    Util.buildSessionSequentially(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    assertTrue(BOB.superBot().views().sarosView().isInSession());
    assertTrue(ALICE.superBot().views().sarosView().isInSession());

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    BOB.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    CARL.superBot().confirmShellAddProjectWithNewProject(Constants.PROJECT1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    assertTrue(CARL.superBot().views().sarosView().isInSession());

    CARL.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    CARL.remoteBot().editor(Constants.CLS1_SUFFIX).selectLine(3);

    CARL.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("Foo was going in a bar");

    CARL.remoteBot().editor(Constants.CLS1_SUFFIX).save();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    String textByCarl = CARL.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsTextSame(textByCarl);
    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsTextSame(textByCarl);

    assertTrue(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText().equals(textByCarl));
    assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText().equals(textByCarl));

    assertFalse(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
    assertFalse(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
  }

  @Test
  @Ignore("Non-Host Invitation is currently deactivated")
  public void NonHostBobInvitesCarlAndLeavesTest() throws Exception {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    Util.buildSessionSequentially(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    assertTrue(BOB.superBot().views().sarosView().isInSession());
    assertTrue(ALICE.superBot().views().sarosView().isInSession());

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    BOB.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    CARL.superBot().confirmShellAddProjectWithNewProject(Constants.PROJECT1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    assertTrue(CARL.superBot().views().sarosView().isInSession());

    // //////////////////////////////////////////////////////////////

    BOB.superBot().views().sarosView().leaveSession();

    assertFalse(BOB.superBot().views().sarosView().isInSession());

    CARL.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    CARL.remoteBot().editor(Constants.CLS1_SUFFIX).selectLine(3);
    CARL.remoteBot()
        .editor(Constants.CLS1_SUFFIX)
        .typeText("public static void main(String args[]){\n\n}");

    ALICE
        .remoteBot()
        .editor(Constants.CLS1_SUFFIX)
        .waitUntilIsTextSame(CARL.remoteBot().editor(Constants.CLS1_SUFFIX).getText());

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).selectLine(4);
    ALICE
        .remoteBot()
        .editor(Constants.CLS1_SUFFIX)
        .typeText("System.out.println(\"Hello World\");");

    CARL.remoteBot()
        .editor(Constants.CLS1_SUFFIX)
        .waitUntilIsTextSame(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText());
  }

  @After
  public void runAfterEveryTest() throws Exception {
    leaveSessionHostFirst(ALICE);
    clearWorkspaces();
  }
}
