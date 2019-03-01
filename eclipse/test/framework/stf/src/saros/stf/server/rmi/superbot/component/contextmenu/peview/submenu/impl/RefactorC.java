package saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.SarosSWTBotPreferences;
import saros.stf.server.bot.widget.ContextMenuHelper;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRefactorC;

public final class RefactorC extends StfRemoteObject implements IRefactorC {

  private static final RefactorC INSTANCE = new RefactorC();

  private SWTBotTree tree;
  private SWTBotTreeItem treeItem;

  private TreeItemType type;

  public static RefactorC getInstance() {
    return INSTANCE;
  }

  public void setTree(SWTBotTree tree) {
    this.tree = tree;
  }

  public void setTreeItem(SWTBotTreeItem treeItem) {
    this.treeItem = treeItem;
  }

  public void setTreeItemType(TreeItemType type) {
    this.type = type;
  }

  @Override
  public void moveTo(String targetProject, String folder) throws RemoteException {
    moveTo(SHELL_MOVE, OK, targetProject.concat("/").concat(folder).replace('\\', '/').split("/"));
  }

  @Override
  public void moveClassTo(String targetProject, String targetPkg) throws RemoteException {
    moveTo(SHELL_MOVE, OK, targetProject, SRC, targetPkg);
  }

  @Override
  public void rename(String newName) throws RemoteException {
    switch (type) {
      case JAVA_PROJECT:
        rename(SHELL_RENAME_JAVA_PROJECT, OK, newName);
        break;
      case PKG:
        rename(SHELL_RENAME_PACKAGE, OK, newName);
        break;
      case CLASS:
        rename(SHELL_RENAME_COMPIIATION_UNIT, FINISH, newName);
        break;
      default:
        rename(SHELL_RENAME_RESOURCE, OK, newName);
        break;
    }
  }

  private void rename(String shellTitle, String buttonName, String newName) {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, MENU_REFACTOR, MENU_RENAME);
    SWTBotShell shell = new SWTBot().shell(shellTitle);
    shell.activate();
    shell.bot().textWithLabel(LABEL_NEW_NAME).setText(newName);
    shell.bot().button(buttonName).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  private void moveTo(String shellTitle, String buttonName, String... nodes) {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, MENU_REFACTOR, MENU_MOVE);
    SWTBotShell shell = new SWTBot().shell(shellTitle);
    shell.activate();
    shell.bot().tree().expandNode(nodes).select();
    shell.bot().button(buttonName).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }
}
