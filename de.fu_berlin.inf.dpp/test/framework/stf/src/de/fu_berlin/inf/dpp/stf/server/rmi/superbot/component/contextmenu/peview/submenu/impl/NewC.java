package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.INewC;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

public final class NewC extends StfRemoteObject implements INewC {

  private static final Logger log = Logger.getLogger(NewC.class);

  private static final NewC INSTANCE = new NewC();

  private SWTBotTree tree;

  public static NewC getInstance() {
    return INSTANCE;
  }

  public void setTree(SWTBotTree tree) {
    this.tree = tree;
  }

  @Override
  public void project(String projectName) throws RemoteException {
    ContextMenuHelper.clickContextMenu(tree, MENU_NEW, MENU_PROJECT);
    confirmWizardNewProject(projectName);
  }

  @Override
  public void javaProject(String projectName) throws RemoteException {
    ContextMenuHelper.clickContextMenu(tree, MENU_NEW, MENU_JAVA_PROJECT);
    confirmShellNewJavaProject(projectName);
  }

  @Override
  public void folder(String folderName) throws RemoteException {

    ContextMenuHelper.clickContextMenu(tree, MENU_NEW, MENU_FOLDER);
    confirmShellNewFolder(folderName);
  }

  @Override
  public void pkg(String projectName, String pkg) throws RemoteException {

    ContextMenuHelper.clickContextMenu(tree, MENU_NEW, MENU_PACKAGE);
    confirmShellNewJavaPackage(projectName, pkg);
  }

  @Override
  public void file(String fileName) throws RemoteException {

    ContextMenuHelper.clickContextMenu(tree, MENU_NEW, MENU_FILE);
    confirmShellNewFile(fileName);
  }

  @Override
  public void cls(String className) throws RemoteException {

    ContextMenuHelper.clickContextMenu(tree, MENU_NEW, MENU_CLASS);
    confirmShellNewJavaClass(className);
  }

  @Override
  public void cls(String projectName, String pkg, String className) throws RemoteException {

    try {
      ContextMenuHelper.clickContextMenu(tree, MENU_NEW, MENU_CLASS);
      confirmShellNewJavaClass(projectName, pkg, className);
    } catch (WidgetNotFoundException e) {
      final String cause =
          "error creating new Java class '"
              + className
              + "' in package '"
              + pkg
              + "' and project '"
              + projectName
              + "'";
      log.error(cause, e);
      throw new RemoteException(cause, e);
    }
    // }
  }

  @Override
  public void clsImplementsRunnable(String className) throws RemoteException {

    RemoteWorkbenchBot.getInstance().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();
    IRemoteBotShell shell_new = RemoteWorkbenchBot.getInstance().shell(SHELL_NEW_JAVA_CLASS);
    shell_new.activate();

    shell_new.bot().textWithLabel(LABEL_NAME).setText(className);
    shell_new.bot().button("Add...").click();
    RemoteWorkbenchBot.getInstance().waitUntilShellIsOpen("Implemented Interfaces Selection");
    IRemoteBotShell shell =
        RemoteWorkbenchBot.getInstance().shell("Implemented Interfaces Selection");
    shell.activate();
    shell.bot().textWithLabel("Choose interfaces:").setText("java.lang.Runnable");
    shell.bot().table().waitUntilTableHasRows(1);
    shell.bot().button(OK).click();
    RemoteWorkbenchBot.getInstance().shell(SHELL_NEW_JAVA_CLASS).activate();
    shell.bot().checkBox("Inherited abstract methods").click();
    shell.bot().button(FINISH).click();
    shell.waitLongUntilIsClosed();
  }

  @Override
  public void javaProjectWithClasses(String projectName, String pkg, String... classNames)
      throws RemoteException {
    javaProject(projectName);
    for (String className : classNames) {
      cls(projectName, pkg, className);
    }
  }

  /**
   * ************************************************************
   *
   * <p>Inner functions
   *
   * <p>************************************************************
   */
  private void confirmShellNewJavaClass(String className) {
    final String defaultPkg = "";

    SWTBotShell shell = new SWTBot().shell(SHELL_NEW_JAVA_CLASS);
    shell.activate();
    shell.bot().textWithLabel(LABEL_NAME).setText(className);
    shell.bot().textWithLabel(LABEL_PACKAGE).setText(defaultPkg);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  private void confirmShellNewJavaClass(String projectName, String pkg, String className) {
    SWTBotShell shell = new SWTBot().shell(SHELL_NEW_JAVA_CLASS);
    shell.activate();
    shell.bot().textWithLabel(LABEL_SOURCE_FOLDER).setText(projectName + "/" + SRC);
    shell.bot().textWithLabel(LABEL_PACKAGE).setText(pkg);
    shell.bot().textWithLabel(LABEL_NAME).setText(className);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  private void confirmWizardNewProject(String projectName) {
    SWTBotShell shell = new SWTBot().shell(SHELL_NEW_PROJECT);
    shell.activate();
    shell.bot().tree().expandNode(NODE_GENERAL, NODE_PROJECT).select();
    shell.bot().button(NEXT).click();
    shell.bot().textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  private void confirmShellNewFile(String fileName) {
    SWTBotShell shell = new SWTBot().shell(SHELL_NEW_FILE);
    shell.activate();
    shell.bot().textWithLabel(LABEL_FILE_NAME).setText(fileName);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  private void confirmShellNewJavaPackage(String projectName, String pkg) {
    SWTBotShell shell = new SWTBot().shell(SHELL_NEW_JAVA_PACKAGE);
    shell.activate();
    shell.bot().textWithLabel(LABEL_SOURCE_FOLDER).setText((projectName + "/" + SRC));
    shell.bot().textWithLabel(LABEL_NAME).setText(pkg);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  private void confirmShellNewFolder(String folderName) {
    SWTBotShell shell = new SWTBot().shell(SHELL_NEW_FOLDER);
    shell.activate();
    shell.bot().textWithLabel(LABEL_FOLDER_NAME).setText(folderName);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  private void confirmShellNewJavaProject(String projectName) {
    SWTBotShell shell = new SWTBot().shell(SHELL_NEW_JAVA_PROJECT);
    shell.activate();
    shell.bot().textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
    shell.bot().button(FINISH).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }
}
