package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.widget.ContextMenuHelper;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotTree;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;

public final class RemoteBotTree extends StfRemoteObject implements IRemoteBotTree {

  private static final Logger log = Logger.getLogger(RemoteBotTree.class);

  private static final RemoteBotTree INSTANCE = new RemoteBotTree();

  private SWTBotTree widget;

  public static RemoteBotTree getInstance() {
    return INSTANCE;
  }

  public IRemoteBotTree setWidget(SWTBotTree tree) {
    widget = tree;
    return this;
  }

  public IRemoteBotTree uncheckAllItems() {
    for (SWTBotTreeItem item : widget.getAllItems()) {
      while (item.isChecked()) item.uncheck();
    }
    return this;
  }

  @Override
  public void clickContextMenu(String... texts) throws RemoteException {
    ContextMenuHelper.clickContextMenu(widget, texts);
  }

  @Override
  public IRemoteBotTreeItem collapseNode(String nodeText) throws RemoteException {
    return RemoteBotTreeItem.getInstance().setWidget(widget.collapseNode(nodeText));
  }

  @Override
  public IRemoteBotTreeItem expandNode(String nodeText, boolean recursive) throws RemoteException {
    return RemoteBotTreeItem.getInstance().setWidget(widget.expandNode(nodeText, recursive));
  }

  @Override
  public IRemoteBotTreeItem expandNode(String... nodes) throws RemoteException {
    return RemoteBotTreeItem.getInstance().setWidget(widget.expandNode(nodes));
  }

  @Override
  public IRemoteBotTree select(int... indices) throws RemoteException {
    return setWidget(widget.select(indices));
  }

  @Override
  public IRemoteBotTree select(String... items) throws RemoteException {
    return setWidget(widget.select(items));
  }

  @Override
  public IRemoteBotTree unselect() throws RemoteException {
    return setWidget(widget.unselect());
  }

  @Override
  public IRemoteBotTreeItem selectTreeItem(String... pathToTreeItem) throws RemoteException {
    RemoteBotTreeItem.getInstance().setWidget(widget.expandNode(pathToTreeItem).select());
    RemoteBotTreeItem.getInstance().setSWTBotTree(widget);
    return RemoteBotTreeItem.getInstance();
  }

  @Override
  public IRemoteBotTreeItem selectTreeItemWithRegex(String... regexNodes) throws RemoteException {
    assert widget != null : "the passed tree is null.";

    SWTBotTreeItem currentItem = null;
    SWTBotTreeItem[] allChildrenOfCurrentItem;

    for (String regex : regexNodes) {

      if (currentItem == null) allChildrenOfCurrentItem = widget.getAllItems();
      else allChildrenOfCurrentItem = currentItem.getItems();

      boolean itemWithRegexFound = false;

      for (SWTBotTreeItem child : allChildrenOfCurrentItem) {

        if (child.getText().matches(regex)) {
          currentItem = child;
          if (!child.isExpanded()) child.expand();
          itemWithRegexFound = true;
          break;
        }
      }

      if (!itemWithRegexFound) {
        throw new WidgetNotFoundException(
            "tree item matching the regex '"
                + regex
                + "' cannot be found. Nodes: "
                + Arrays.asList(regexNodes));
      }
    }

    if (currentItem != null) {
      SWTBotTreeItem item = currentItem.select();
      RemoteBotTreeItem.getInstance().setWidget(item);
      RemoteBotTreeItem.getInstance().setSWTBotTree(widget);
      return RemoteBotTreeItem.getInstance();
    }

    throw new WidgetNotFoundException(
        "unknown error: " + widget.getText() + ", " + Arrays.asList(regexNodes));
  }

  @Override
  public IRemoteBotTreeItem selectTreeItemAndWait(String... pathToTreeItem) throws RemoteException {
    SWTBotTreeItem selectedTreeItem = null;
    for (String node : pathToTreeItem) {
      try {
        if (selectedTreeItem == null) {
          waitUntilItemExists(node);
          selectedTreeItem = widget.expandNode(node);
        } else {

          RemoteBotTreeItem treeItem = RemoteBotTreeItem.getInstance();
          treeItem.setWidget(selectedTreeItem);
          treeItem.setSWTBotTree(widget);
          treeItem.waitUntilSubItemExists(node);
          selectedTreeItem = selectedTreeItem.expandNode(node);
        }
      } catch (WidgetNotFoundException e) {
        log.error("tree item \"" + node + "\" not found", e);
      }
    }
    if (selectedTreeItem != null) {
      SWTBotTreeItem item = selectedTreeItem.select();
      RemoteBotTreeItem.getInstance().setWidget(item);
      RemoteBotTreeItem.getInstance().setSWTBotTree(widget);
      return RemoteBotTreeItem.getInstance();
    }

    throw new WidgetNotFoundException(
        "unknown error: " + widget.getText() + ", " + Arrays.asList(pathToTreeItem));
  }

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  @Override
  public boolean hasItems() throws RemoteException {
    return widget.hasItems();
  }

  @Override
  public int rowCount() throws RemoteException {
    return widget.rowCount();
  }

  @Override
  public int selectionCount() throws RemoteException {
    return widget.selectionCount();
  }

  @Override
  public int columnCount() throws RemoteException {
    return widget.columnCount();
  }

  @Override
  public List<String> columns() throws RemoteException {
    return widget.columns();
  }

  @Override
  public List<String> getTextOfItems() throws RemoteException {
    List<String> allItemTexts = new ArrayList<String>();
    for (SWTBotTreeItem item : widget.getAllItems()) {
      allItemTexts.add(item.getText());
    }
    return allItemTexts;
  }

  @Override
  public boolean existsSubItem(String treeItemText) throws RemoteException {
    return getTextOfItems().contains(treeItemText);
  }

  @Override
  public boolean existsSubItemWithRegex(String regex) throws RemoteException {
    for (String subItem : getTextOfItems()) {
      if (subItem.matches(regex)) return true;
    }
    return false;
  }

  /**
   * ********************************************
   *
   * <p>waits until
   *
   * <p>********************************************
   */
  @Override
  public void waitUntilItemExists(final String itemText) throws RemoteException {

    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return existsSubItem(itemText);
              }

              @Override
              public String getFailureMessage() {
                return "tree '" + widget.getText() + "' does not contain the treeItem: " + itemText;
              }
            });
  }

  @Override
  public void waitUntilItemNotExists(final String itemText) throws RemoteException {
    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return !existsSubItem(itemText);
              }

              @Override
              public String getFailureMessage() {
                return "tree '" + widget.getText() + "' still contains the treeItem: " + itemText;
              }
            });
  }
}
