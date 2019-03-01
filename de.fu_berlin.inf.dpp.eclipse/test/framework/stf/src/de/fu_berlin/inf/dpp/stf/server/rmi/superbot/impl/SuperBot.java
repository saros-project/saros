package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.IMenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.impl.MenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.IViews;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.impl.Views;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.IInternal;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.impl.InternalImpl;
import de.fu_berlin.inf.dpp.stf.server.util.WidgetUtil;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

public final class SuperBot extends StfRemoteObject implements ISuperBot {

  private static final SuperBot INSTANCE = new SuperBot();

  private JID localJID;

  public static SuperBot getInstance() {
    return INSTANCE;
  }

  @Override
  public IViews views() throws RemoteException {
    return Views.getInstance();
  }

  @Override
  public IMenuBar menuBar() throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    return MenuBar.getInstance();
  }

  @Override
  public void setJID(JID jid) throws RemoteException {
    localJID = jid;
  }

  public JID getJID() {
    return localJID;
  }

  @Override
  public void confirmShellAddProjectWithNewProject(String projectName) throws RemoteException {

    SWTBot bot = new SWTBot();
    bot.waitUntil(
        Conditions.shellIsActive(SHELL_ADD_PROJECTS), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);

    SWTBotShell shell = bot.shell(SHELL_ADD_PROJECTS);
    shell.activate();

    shell.bot().radio(RADIO_CREATE_NEW_PROJECT).click();
    shell.bot().textWithLabel("Project name", 0).setText(projectName);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellAddProjectUsingExistProject(String projectName) throws RemoteException {
    SWTBot bot = new SWTBot();
    bot.waitUntil(
        Conditions.shellIsActive(SHELL_ADD_PROJECTS), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);

    SWTBotShell shell = bot.shell(SHELL_ADD_PROJECTS);
    shell.activate();

    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=344484
    // FIXME fix the bug in the SWTBot Framework, do not use a workaround
    final Button radioButton = shell.bot().radio(RADIO_CREATE_NEW_PROJECT).widget;

    UIThreadRunnable.syncExec(
        new VoidResult() {

          @Override
          public void run() {
            radioButton.setSelection(false);
          }
        });

    shell.bot().radio(RADIO_USING_EXISTING_PROJECT).click();
    shell.bot().textWithLabel("Project name", 1).setText(projectName);
    shell.bot().button(FINISH).click();

    // prevent STF from entering an endless loop

    int timeout = 5;
    confirmDialogs:
    while (timeout-- > 0) {
      bot.sleep(2000);

      for (SWTBotShell currentShell : bot.shells()) {
        if (currentShell.getText().equals(SHELL_WARNING_LOCAL_CHANGES_DELETED)
            || currentShell.getText().equals(SHELL_SAVE_RESOURCE)) {
          currentShell.activate();
          currentShell.bot().button(YES).click();
          currentShell.bot().waitUntil(Conditions.shellCloses(currentShell));
          break confirmDialogs;

        } else if (currentShell.getText().equals(SHELL_CONFIRM_SAVE_UNCHANGED_CHANGES)) {
          currentShell.activate();
          currentShell.bot().button(YES).click();
          currentShell.bot().waitUntil(Conditions.shellCloses(currentShell));

          shell.bot().button(FINISH).click();

          continue confirmDialogs;
        }
      }
    }

    shell.bot().waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellAddProjectUsingExistProjectWithCopy(String projectName)
      throws RemoteException {

    SWTBot bot = new SWTBot();
    bot.waitUntil(
        Conditions.shellIsActive(SHELL_ADD_PROJECTS), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);

    SWTBotShell shell = bot.shell(SHELL_ADD_PROJECTS);
    shell.activate();

    shell.bot().radio("Use existing project").click();
    shell.bot().checkBox("Create copy for working distributed. New project name:").click();
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellAddProjectUsingWhichProject(
      String projectName, TypeOfCreateProject usingWhichProject) throws RemoteException {
    SWTBot bot = new SWTBot();
    bot.waitUntil(
        Conditions.shellIsActive(SHELL_ADD_PROJECTS), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);

    SWTBotShell shell = bot.shell(SHELL_ADD_PROJECTS);
    shell.activate();

    switch (usingWhichProject) {
      case NEW_PROJECT:
        confirmShellAddProjectWithNewProject(projectName);
        break;
      case EXIST_PROJECT:
        confirmShellAddProjectUsingExistProject(projectName);
        break;
      case EXIST_PROJECT_WITH_COPY:
        confirmShellAddProjectUsingExistProjectWithCopy(projectName);
        break;
    }
  }

  @Override
  public void confirmShellEditXMPPAccount(String xmppJabberID, String newPassword)
      throws RemoteException {
    SWTBotShell shell = new SWTBot().shell(SHELL_EDIT_XMPP_JABBER_ACCOUNT);
    shell.activate();

    shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_ID).setText(xmppJabberID);
    shell.bot().textWithLabel(LABEL_PASSWORD).setText(newPassword);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellCreateNewXMPPAccount(JID jid, String password) throws RemoteException {
    SWTBotShell shell = new SWTBot().shell(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
    shell.activate();

    shell.bot().textWithLabel(LABEL_XMPP_JABBER_SERVER).setText(jid.getDomain());
    shell.bot().textWithLabel(LABEL_USER_NAME).setText(jid.getName());
    shell.bot().textWithLabel(LABEL_PASSWORD).setText(password);
    shell.bot().textWithLabel(LABEL_REPEAT_PASSWORD).setText(password);

    shell.bot().button(FINISH).click();
    try {
      shell.bot().waitUntil(Conditions.shellCloses(shell));
    } catch (TimeoutException e) {
      String errorMessage = ((WizardDialog) shell.widget.getData()).getMessage();
      if (errorMessage.matches(ERROR_MESSAGE_TOO_FAST_REGISTER_ACCOUNTS + ".*"))
        throw new RuntimeException("you are not allowed to register accounts so fast");
      else if (errorMessage.matches(ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS + ".*\n*.*"))
        throw new RuntimeException("the Account " + jid.getBase() + " already exists");
    }
  }

  @Override
  public void confirmShellAddXMPPAccount(JID jid, String password) throws RemoteException {

    SWTBotShell shell = new SWTBot().shell(SHELL_ADD_XMPP_JABBER_ACCOUNT);
    shell.activate();

    /*
     * FIXME with comboBoxInGroup(GROUP_EXISTING_ACCOUNT) you wil get
     * WidgetNoFoundException.
     */
    shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_ID).setText(jid.getBase());

    shell.bot().textWithLabel(LABEL_PASSWORD).setText(password);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellClosingTheSession() throws RemoteException {
    SWTBotShell shell = new SWTBot().shell(SHELL_CLOSING_THE_SESSION);
    shell.activate();
    shell.bot().button(OK).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
    // wait for tree update in the saros session view
    new SWTBot().sleep(500);
  }

  @Override
  public void confirmShellAddContactsToSession(String... baseJIDOfinvitees) throws RemoteException {

    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_ADD_CONTACT_TO_SESSION);

    shell.activate();

    // wait for tree update
    bot.sleep(500);

    SWTBotTree tree = shell.bot().tree();

    for (String baseJID : baseJIDOfinvitees)
      WidgetUtil.getTreeItemWithRegex(tree, Pattern.quote(baseJID) + ".*").check();

    shell.bot().button(FINISH).click();
    bot.waitUntil(Conditions.shellCloses(shell));

    // wait for tree update in the saros session view
    bot.sleep(500);
  }

  @Override
  public void confirmShellAddContact(JID jid) throws RemoteException {
    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_ADD_CONTACT_WIZARD);
    shell.activate();

    shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_ID).setText(jid.getBase());

    shell.bot().button(FINISH).click();

    try {
      bot.waitUntil(Conditions.shellCloses(shell));
    } catch (TimeoutException e) {
      // If the dialog didn't close in time, close any message boxes that
      // a you can answer with "Yes, I want to add the contact anyway"

      // FIXME Hard-coded message titles (see AddContactWizard)
      List<String> messagesToIgnore =
          Arrays.asList(
              "Contact Unknown",
              "Server Not Found",
              "Unsupported Contact Status Check",
              "Unknown Contact Status",
              "Server Not Responding",
              "Unknown Error");

      for (SWTBotShell currentShell : bot.shells()) {
        String text = currentShell.getText();

        if (messagesToIgnore.contains(text)) {
          currentShell.bot().button(YES).click();
        }
      }
    }

    // wait for tree update in the saros session view
    bot.sleep(500);
  }

  @Override
  public void confirmShellShareProjects(String projectName, JID... jids) throws RemoteException {
    confirmShellShareProjects(new String[] {projectName}, jids);
  }

  @Override
  public void confirmShellShareProjects(String[] projectNames, JID... jids) throws RemoteException {

    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_SHARE_PROJECT);
    shell.activate();

    // wait for tree update
    bot.sleep(500);

    SWTBotTree tree = shell.bot().tree();

    for (SWTBotTreeItem item : tree.getAllItems()) while (item.isChecked()) item.uncheck();

    for (String projectName : projectNames) tree.getTreeItem(projectName).check();

    shell.bot().button(NEXT).click();

    // wait for tree update
    bot.sleep(500);

    tree = shell.bot().tree();

    for (SWTBotTreeItem item : tree.getAllItems()) while (item.isChecked()) item.uncheck();

    for (JID jid : jids)
      WidgetUtil.getTreeItemWithRegex(tree, Pattern.quote(jid.getBase()) + ".*").check();

    shell.bot().button(FINISH).click();
    bot.waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellAddProjectsToSession(String... projectNames) throws RemoteException {

    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_ADD_PROJECTS_TO_SESSION);
    shell.activate();

    // wait for tree update
    bot.sleep(500);

    SWTBotTree tree = shell.bot().tree();

    for (String projectName : projectNames) tree.getTreeItem(projectName).check();

    shell.bot().button(FINISH).click();
    bot.waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellAddProjectToSession(String project, String[] files)
      throws RemoteException {
    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_ADD_PROJECTS_TO_SESSION);
    shell.activate();

    // wait for tree update
    bot.sleep(500);

    SWTBotTree tree = shell.bot().tree();

    selectProjectFiles(tree, project, files);

    shell.bot().button(FINISH).click();
    bot.waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellSessionInvitationAndShellAddProject(
      String projectName, TypeOfCreateProject usingWhichProject) throws RemoteException {

    SWTBot bot = new SWTBot();
    bot.waitUntil(
        Conditions.shellIsActive(SHELL_SESSION_INVITATION),
        SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);

    SWTBotShell invitationShell = bot.shell(SHELL_SESSION_INVITATION);
    invitationShell.bot().button(ACCEPT).click();

    bot.waitUntil(
        Conditions.shellCloses(invitationShell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);

    confirmShellAddProjectUsingWhichProject(projectName, usingWhichProject);
    views().sarosView().waitUntilIsInSession();
  }

  @Override
  public void confirmShellRequestOfSubscriptionReceived() throws RemoteException {

    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED);

    shell.activate();
    shell.bot().button(OK).click();
    bot.waitUntil(Conditions.shellCloses(shell));
    // wait for tree update in the saros session view
    bot.sleep(500);
  }

  @Override
  public void confirmShellLeavingClosingSession() throws RemoteException {
    SWTBot bot = new SWTBot();
    SWTBotShell shell;

    if (!Views.getInstance().sarosView().isHost()) {
      shell = bot.shell(SHELL_CONFIRM_LEAVING_SESSION);
    } else {
      shell = bot.shell(SHELL_CONFIRM_CLOSING_SESSION);
    }
    shell.activate();
    shell.bot().button(YES).click();
    Views.getInstance().sarosView().waitUntilIsNotInSession();
  }

  @Override
  public IInternal internal() throws RemoteException {
    return InternalImpl.getInstance();
  }

  public void confirmShellShareProjectFiles(String project, String[] files, JID[] jids) {
    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_SHARE_PROJECT);
    shell.activate();

    // wait for tree update
    bot.sleep(500);

    SWTBotTree tree = shell.bot().tree();

    selectProjectFiles(tree, project, files);

    shell.bot().button(NEXT).click();

    // wait for tree update
    bot.sleep(500);

    tree = shell.bot().tree();

    for (SWTBotTreeItem item : tree.getAllItems()) while (item.isChecked()) item.uncheck();

    for (JID jid : jids)
      WidgetUtil.getTreeItemWithRegex(tree, Pattern.quote(jid.getBase()) + ".*").check();

    shell.bot().button(FINISH).click();
    bot.waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellNewSharedFile(String decision) {
    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_NEW_FILE_SHARED);
    shell.activate();
    shell.bot().button(decision).click();
    bot.waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void confirmShellNeedBased(String decsision, boolean remember) throws RemoteException {
    SWTBot bot = new SWTBot();
    SWTBotShell shell = bot.shell(SHELL_NEED_BASED_SYNC);
    shell.activate();
    if (remember) shell.bot().checkBox("Remember my decision.").click();
    shell.bot().button(decsision).click();
    bot.waitUntil(Conditions.shellCloses(shell));
  }

  private void selectProjectFiles(SWTBotTree tree, String project, String[] files) {
    for (SWTBotTreeItem item : tree.getAllItems()) while (item.isChecked()) item.uncheck();

    for (String file : files) {
      String[] nodes = file.split("/|\\\\");
      List<String> regex = new ArrayList<String>(nodes.length + 1);
      regex.add(Pattern.quote(project));

      for (String node : nodes) regex.add(Pattern.quote(node));

      WidgetUtil.getTreeItemWithRegex(tree, regex.toArray(new String[0])).check();
    }
  }
}
