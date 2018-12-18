package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.FINISH;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_ADD_CONTACT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_SAROS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.NEXT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHARE_PROJECTS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SHARE_PROJECT;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import java.rmi.RemoteException;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MenuSarosByAliceBobTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Before
  public void beforeEveryTest() throws Exception {

    int timeout = 10;

    while (timeout > 0) {
      if (clearWorkspaces()) break;
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw e;
      }
      timeout--;
    }

    if (timeout == 0) throw new RuntimeException("unable to delete workspace");

    resetWorkbenches();
  }

  @Test
  public void testShareProjectsWithRemoteBot() throws Exception {

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE.remoteBot().activateWorkbench();
    ALICE.remoteBot().menu(MENU_SAROS).menu(SHARE_PROJECTS).click();

    IRemoteBotShell shell = ALICE.remoteBot().shell(SHELL_SHARE_PROJECT);
    shell.activate();
    Thread.sleep(1000);
    shell.bot().tree().selectTreeItem(Constants.PROJECT1).check();
    shell.bot().button(NEXT).click();
    Thread.sleep(1000);
    shell.bot().tree().selectTreeItemWithRegex(Pattern.quote(BOB.getBaseJid()) + ".*").check();
    shell.bot().button(FINISH).click();

    BOB.remoteBot().waitUntilShellIsOpen(SHELL_SESSION_INVITATION);
    IRemoteBotShell shell2 = BOB.remoteBot().shell(SHELL_SESSION_INVITATION);
    shell2.activate();
    shell2.bot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    BOB.superBot()
        .confirmShellAddProjectUsingWhichProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);
    BOB.superBot().views().sarosView().waitUntilIsInSession();
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared(
            Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG1, Constants.CLS1));

    leaveSessionHostFirst(ALICE);
  }

  @Test
  public void testShareProjectsWithSuperBot() throws Exception {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE.superBot().menuBar().saros().shareProjects(Constants.PROJECT1, BOB.getJID());

    BOB.superBot()
        .confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);

    BOB.superBot().views().sarosView().waitUntilIsInSession();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared(
            Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG1, Constants.CLS1));

    leaveSessionHostFirst(ALICE);
  }

  @Test
  public void testAddContact() throws RemoteException {
    ALICE.superBot().views().sarosView().connectWith(ALICE.getJID(), ALICE.getPassword(), false);
    ALICE.remoteBot().activateWorkbench();
    ALICE.remoteBot().menu(MENU_SAROS).menu(MENU_ADD_CONTACT).click();
    ALICE.superBot().confirmShellAddContact(BOB.getJID());
  }

  @Test
  public void addProjects() throws Exception {
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
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT2, Constants.PKG1, Constants.CLS1);

    ALICE.superBot().menuBar().saros().addProjects(Constants.PROJECT2);
    BOB.superBot()
        .confirmShellAddProjectUsingWhichProject(
            Constants.PROJECT2, TypeOfCreateProject.NEW_PROJECT);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT2, Constants.PKG1, Constants.CLS1);

    leaveSessionHostFirst(ALICE);
  }

  @Test
  public void stopSession() throws Exception {
    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);

    ALICE.superBot().views().sarosView().waitUntilIsInSession();
    BOB.superBot().views().sarosView().waitUntilIsInSession();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE.superBot().views().sarosView().leaveSession();

    ALICE.superBot().views().sarosView().waitUntilIsNotInSession();
    BOB.superBot().views().sarosView().waitUntilIsNotInSession();
  }
}
