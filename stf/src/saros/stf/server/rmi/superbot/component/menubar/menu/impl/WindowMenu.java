package saros.stf.server.rmi.superbot.component.menubar.menu.impl;

import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotCombo;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotTree;
import saros.stf.server.rmi.superbot.component.Perspective;
import saros.stf.server.rmi.superbot.component.menubar.menu.IWindowMenu;
import saros.stf.server.util.WidgetUtil;

public final class WindowMenu extends StfRemoteObject implements IWindowMenu {

  private static final Logger log = Logger.getLogger(WindowMenu.class);
  private static final WindowMenu INSTANCE = new WindowMenu();

  public static WindowMenu getInstance() {
    return INSTANCE;
  }

  @Override
  public void setNewTextFileLineDelimiter(String OS) throws RemoteException {
    clickMenuPreferences();
    IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(SHELL_PREFERNCES);
    IRemoteBotTree tree = shell.bot().tree();
    tree.expandNode(TREE_ITEM_GENERAL_IN_PRFERENCES).select(TREE_ITEM_WORKSPACE_IN_PREFERENCES);

    if (OS.equals("Default")) {
      shell.bot().radioInGroup("Default", "New text file line delimiter").click();
    } else {
      shell.bot().radioInGroup("Other:", "New text file line delimiter").click();
      shell.bot().comboBoxInGroup("New text file line delimiter").setSelection(OS);
    }
    shell.bot().button(APPLY).click();
    shell.bot().button(OK).click();
    RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(SHELL_PREFERNCES);
  }

  @Override
  public void clickMenuPreferences() throws RemoteException {
    if (WidgetUtil.getOperatingSystem() == WidgetUtil.OperatingSystem.MAC)
      RemoteWorkbenchBot.getInstance().menu("Eclipse").menu(MENU_PREFERENCES).click();
    else RemoteWorkbenchBot.getInstance().menu(MENU_WINDOW).menu(MENU_PREFERENCES).click();
  }

  @Override
  public void showViewProblems() throws RemoteException {
    showViewWithName(TREE_ITEM_GENERAL_IN_SHELL_SHOW_VIEW, TREE_ITEM_PROBLEM_IN_SHELL_SHOW_VIEW);
  }

  @Override
  public void showViewProjectExplorer() throws RemoteException {
    showViewWithName(
        TREE_ITEM_GENERAL_IN_SHELL_SHOW_VIEW, TREE_ITEM_PROJECT_EXPLORER_IN_SHELL_SHOW_VIEW);
  }

  @Override
  public void showViewWithName(String parentNode, String node) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    RemoteWorkbenchBot.getInstance()
        .menu(MENU_WINDOW)
        .menu(MENU_SHOW_VIEW)
        .menu(MENU_OTHER)
        .click();
    RemoteWorkbenchBot.getInstance()
        .shell(SHELL_SHOW_VIEW)
        .confirmWithTreeWithFilterText(parentNode, node, OK);
  }

  @Override
  public void openPerspective() throws RemoteException {
    switch (Perspective.WHICH_PERSPECTIVE) {
      case JAVA:
        openPerspectiveJava();
        break;
      case DEBUG:
        openPerspectiveDebug();
        break;
      case RESOURCE:
        openPerspectiveResource();
        break;
      default:
        openPerspectiveJava();
        break;
    }
  }

  @Override
  public void openPerspectiveResource() throws RemoteException {
    RemoteWorkbenchBot.getInstance().openPerspectiveWithId(ID_RESOURCE_PERSPECTIVE);
  }

  @Override
  public void openPerspectiveJava() throws RemoteException {
    RemoteWorkbenchBot.getInstance().openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
  }

  @Override
  public void openPerspectiveDebug() throws RemoteException {
    RemoteWorkbenchBot.getInstance().openPerspectiveWithId(ID_DEBUG_PERSPECTIVE);
  }

  @Override
  public String getTextFileLineDelimiter() throws RemoteException {
    clickMenuPreferences();
    IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(SHELL_PREFERNCES);
    IRemoteBotTree tree = shell.bot().tree();
    tree.expandNode(TREE_ITEM_GENERAL_IN_PRFERENCES).select(TREE_ITEM_WORKSPACE_IN_PREFERENCES);
    if (shell.bot().radioInGroup("Default", "New text file line delimiter").isSelected()) {
      shell.close();
      return "Default";
    } else if (shell.bot().radioInGroup("Other:", "New text file line delimiter").isSelected()) {
      IRemoteBotCombo combo = shell.bot().comboBoxInGroup("New text file line delimiter");
      String itemName = combo.items()[combo.selectionIndex()];
      RemoteWorkbenchBot.getInstance().shell(SHELL_PREFERNCES).close();
      return itemName;
    }
    shell.close();
    return "";
  }

  @Override
  public boolean isJavaPerspectiveActive() throws RemoteException {
    try {
      return RemoteWorkbenchBot.getInstance().isPerspectiveActive(ID_JAVA_PERSPECTIVE);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isDebugPerspectiveActive() throws RemoteException {
    try {
      return RemoteWorkbenchBot.getInstance().isPerspectiveActive(ID_DEBUG_PERSPECTIVE);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }
}
