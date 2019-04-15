package saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.widget.ContextMenuHelper;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRunAsContextMenu;

public final class RunAsContextMenu extends StfRemoteObject implements IRunAsContextMenu {

  private static final RunAsContextMenu INSTANCE = new RunAsContextMenu();

  private SWTBotTree tree;
  private SWTBotTreeItem treeItem;

  public static RunAsContextMenu getInstance() {
    return INSTANCE;
  }

  public void setTree(SWTBotTree tree) {
    this.tree = tree;
  }

  public void setTreeItem(SWTBotTreeItem view) {
    this.treeItem = view;
  }

  @Override
  public void javaApplication() throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenuWithRegEx(
        tree, CM_RUN_AS, "\\d*+\\s*+" + CM_JAVA_APPLICATION);
  }
}
