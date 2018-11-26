package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public final class RemoteBotTreeItem extends StfRemoteObject implements IRemoteBotTreeItem {

  private static final RemoteBotTreeItem INSTANCE = new RemoteBotTreeItem();

  private SWTBotTreeItem widget;

  private SWTBotTree swtBotTree;

  public static RemoteBotTreeItem getInstance() {
    return INSTANCE;
  }

  public IRemoteBotTreeItem setWidget(SWTBotTreeItem item) {

    this.widget = item;
    return this;
  }

  public void setSWTBotTree(SWTBotTree tree) {
    this.swtBotTree = tree;
  }

  @Override
  public void clickContextMenu(String... texts) throws RemoteException {
    ContextMenuHelper.clickContextMenu(swtBotTree, texts);
  }

  @Override
  public void toggleCheck() throws RemoteException {
    widget.toggleCheck();
  }

  @Override
  public void uncheck() throws RemoteException {
    widget.uncheck();
  }

  @Override
  public IRemoteBotTreeItem select(String... items) throws RemoteException {
    return setWidget(widget.select(items));
  }

  @Override
  public IRemoteBotTreeItem select() throws RemoteException {
    return setWidget(widget.select());
  }

  @Override
  public IRemoteBotTreeItem doubleClick() throws RemoteException {
    return setWidget(widget.doubleClick());
  }

  @Override
  public IRemoteBotTreeItem expand() throws RemoteException {
    return setWidget(widget.expand());
  }

  @Override
  public IRemoteBotTreeItem expandNode(String... nodes) throws RemoteException {
    return setWidget(widget.expandNode(nodes));
  }

  @Override
  public void check() throws RemoteException {
    widget.check();
  }

  @Override
  public IRemoteBotTreeItem collapse() throws RemoteException {
    return setWidget(widget.collapse());
  }

  @Override
  public IRemoteBotTreeItem collapseNode(String nodeText) throws RemoteException {
    return setWidget(widget.collapseNode(nodeText));
  }

  @Override
  public IRemoteBotTreeItem select(String item) throws RemoteException {
    return setWidget(widget.select(item));
  }

  @Override
  public void click() throws RemoteException {
    widget.click();
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public boolean isSelected() throws RemoteException {
    return widget.isSelected();
  }

  @Override
  public boolean isChecked() throws RemoteException {
    return widget.isChecked();
  }

  @Override
  public boolean isExpanded() throws RemoteException {
    return widget.isExpanded();
  }

  @Override
  public int rowCount() throws RemoteException {
    return widget.rowCount();
  }

  @Override
  public IRemoteBotTreeItem getNode(int row) throws RemoteException {
    return setWidget(widget.getNode(row));
  }

  @Override
  public IRemoteBotTreeItem getNode(String nodeText) throws RemoteException {
    return setWidget(widget.getNode(nodeText));
  }

  @Override
  public IRemoteBotTreeItem getNode(String nodeText, int index) throws RemoteException {
    return setWidget(widget.getNode(nodeText, index));
  }

  @Override
  public IRemoteBotTreeItem getNodeWithRegex(String regex) throws RemoteException {
    for (String itemText : getTextOfItems()) {
      if (itemText.matches(regex)) return getNode(itemText);
    }
    throw new WidgetNotFoundException("Could not find node with regex: " + regex);
  }

  @Override
  public List<String> getNodes() throws RemoteException {
    return widget.getNodes();
  }

  @Override
  public List<IRemoteBotTreeItem> getNodes(String nodeText) throws RemoteException {
    List<IRemoteBotTreeItem> items = new ArrayList<IRemoteBotTreeItem>();
    for (SWTBotTreeItem item : widget.getNodes(nodeText)) {
      items.add(setWidget(item));
    }
    return items;
  }

  @Override
  public List<String> getTextOfItems() throws RemoteException {
    List<String> allItemTexts = new ArrayList<String>();
    for (SWTBotTreeItem item : widget.getItems()) {
      allItemTexts.add(item.getText());
    }
    return allItemTexts;
  }

  @Override
  public boolean existsSubItem(String text) throws RemoteException {
    return getTextOfItems().contains(text);
  }

  @Override
  public boolean existsSubItemWithRegex(String regex) throws RemoteException {
    for (String itemText : getTextOfItems()) {
      if (itemText.matches(regex)) return true;
    }
    return false;
  }

  @Override
  public boolean isContextMenuEnabled(String... contextNames) throws RemoteException {
    return ContextMenuHelper.isContextMenuEnabled(swtBotTree, contextNames);
  }

  @Override
  public boolean existsContextMenu(String... contextNames) throws RemoteException {
    return ContextMenuHelper.existsContextMenu(swtBotTree, contextNames);
  }

  @Override
  public boolean isEnabled() throws RemoteException {
    return widget.isEnabled();
  }

  @Override
  public boolean isVisible() throws RemoteException {
    return widget.isVisible();
  }

  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public void waitUntilSubItemExists(final String subItemText) throws RemoteException {
    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return existsSubItem(subItemText);
              }

              @Override
              public String getFailureMessage() {
                return "the tree node '"
                    + widget.getText()
                    + "'does not contain the tree item: "
                    + subItemText;
              }
            });
  }

  @Override
  public void waitUntilContextMenuExists(final String... contextNames) throws RemoteException {
    RemoteWorkbenchBot.getInstance()
        .waitLongUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return existsContextMenu(contextNames);
              }

              @Override
              public String getFailureMessage() {
                return "the context menu for context names + "
                    + Arrays.toString(contextNames)
                    + " does not exists";
              }
            });
  }
}
