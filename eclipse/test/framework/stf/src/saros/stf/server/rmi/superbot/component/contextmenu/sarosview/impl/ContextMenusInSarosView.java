package saros.stf.server.rmi.superbot.component.contextmenu.sarosview.impl;

import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.widget.ContextMenuHelper;
import saros.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSarosView;
import saros.stf.server.rmi.superbot.component.view.saros.ISarosView;
import saros.stf.server.util.WidgetUtil;

public abstract class ContextMenusInSarosView extends StfRemoteObject
    implements IContextMenusInSarosView {

  protected String[] treeItemNodes;
  protected SWTBotTree tree;

  protected ISarosView sarosView;

  public void setTreeItemNodes(String... treeItemNodes) {
    this.treeItemNodes = treeItemNodes;
  }

  public void setTree(SWTBotTree tree) {
    this.tree = tree;
  }

  public void setSarosView(ISarosView sarosView) {
    this.sarosView = sarosView;
  }

  @Override
  public void openChat() throws RemoteException {
    getTreeItem().select();
    ContextMenuHelper.clickContextMenu(tree, CM_OPEN_CHAT);
  }

  protected final void logError(Logger log, Throwable t, SWTBotTree tree, SWTBotTreeItem treeItem) {
    String treeItemText = null;
    String treeText = null;
    try {
      treeText = tree == null ? "not found" : tree.getText();
      treeItemText = treeItem == null ? "not found" : treeItem.getText();
    } catch (RuntimeException e) {
      log.error(e.getMessage(), e);
    }
    log.error(t.getMessage() + "@ tree: " + treeText + ", tree item: " + treeItemText, t);
  }

  /**
   * Gets the tree item that was passed as name by searching through the current tree. Because the
   * Saros Session View tree can be refreshed any times it is necessary not to cache the tree item
   * and always grab a fresh copy of the tree item
   *
   * @return the current tree item
   */
  protected final SWTBotTreeItem getTreeItem() {
    return WidgetUtil.getTreeItemWithRegex(tree, treeItemNodes);
  }
}
