package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;

public final class RemoteBotTreeItem extends StfRemoteObject implements
    IRemoteBotTreeItem {

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

    public void clickContextMenu(String... texts) throws RemoteException {
        ContextMenuHelper.clickContextMenu(swtBotTree, texts);
    }

    public void toggleCheck() throws RemoteException {
        widget.toggleCheck();
    }

    public void uncheck() throws RemoteException {
        widget.uncheck();
    }

    public IRemoteBotTreeItem select(String... items) throws RemoteException {
        return setWidget(widget.select(items));
    }

    public IRemoteBotTreeItem select() throws RemoteException {
        return setWidget(widget.select());
    }

    public IRemoteBotTreeItem doubleClick() throws RemoteException {
        return setWidget(widget.doubleClick());
    }

    public IRemoteBotTreeItem expand() throws RemoteException {
        return setWidget(widget.expand());
    }

    public IRemoteBotTreeItem expandNode(String... nodes)
        throws RemoteException {
        return setWidget(widget.expandNode(nodes));
    }

    public void check() throws RemoteException {
        widget.check();
    }

    public IRemoteBotTreeItem collapse() throws RemoteException {
        return setWidget(widget.collapse());
    }

    public IRemoteBotTreeItem collapseNode(String nodeText)
        throws RemoteException {
        return setWidget(widget.collapseNode(nodeText));
    }

    public IRemoteBotTreeItem select(String item) throws RemoteException {
        return setWidget(widget.select(item));
    }

    public void click() throws RemoteException {
        widget.click();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public boolean isSelected() throws RemoteException {
        return widget.isSelected();
    }

    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
    }

    public boolean isExpanded() throws RemoteException {
        return widget.isExpanded();
    }

    public int rowCount() throws RemoteException {
        return widget.rowCount();
    }

    public IRemoteBotTreeItem getNode(int row) throws RemoteException {
        return setWidget(widget.getNode(row));
    }

    public IRemoteBotTreeItem getNode(String nodeText) throws RemoteException {
        return setWidget(widget.getNode(nodeText));
    }

    public IRemoteBotTreeItem getNode(String nodeText, int index)
        throws RemoteException {
        return setWidget(widget.getNode(nodeText, index));
    }

    public List<String> getNodes() throws RemoteException {
        return widget.getNodes();
    }

    public List<IRemoteBotTreeItem> getNodes(String nodeText)
        throws RemoteException {
        List<IRemoteBotTreeItem> items = new ArrayList<IRemoteBotTreeItem>();
        for (SWTBotTreeItem item : widget.getNodes(nodeText)) {
            items.add(setWidget(item));
        }
        return items;
    }

    public List<String> getTextOfItems() throws RemoteException {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : widget.getItems()) {
            allItemTexts.add(item.getText());
        }
        return allItemTexts;
    }

    public boolean existsSubItem(String text) throws RemoteException {
        return getTextOfItems().contains(text);
    }

    public boolean existsSubItemWithRegex(String regex) throws RemoteException {
        for (String itemText : getTextOfItems()) {
            if (itemText.matches(regex))
                return true;
        }
        return false;
    }

    public boolean isContextMenuEnabled(String... contextNames)
        throws RemoteException {
        return ContextMenuHelper.isContextMenuEnabled(swtBotTree, contextNames);
    }

    public boolean existsContextMenu(String... contextNames)
        throws RemoteException {
        return ContextMenuHelper.existsContextMenu(swtBotTree, contextNames);
    }

    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    public void waitUntilSubItemExists(final String subItemText)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsSubItem(subItemText);
            }

            public String getFailureMessage() {
                return "the tree node '" + widget.getText()
                    + "'does not contain the tree item: " + subItemText;
            }
        });
    }

    public void waitUntilContextMenuExists(final String... contextNames)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitLongUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsContextMenu(contextNames);
            }

            public String getFailureMessage() {
                return "the context menu for context names + "
                    + Arrays.toString(contextNames) + " does not exists";
            }
        });
    }

}
