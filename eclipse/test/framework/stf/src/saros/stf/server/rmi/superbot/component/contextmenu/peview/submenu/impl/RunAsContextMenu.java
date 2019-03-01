package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRunAsContextMenu;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

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
