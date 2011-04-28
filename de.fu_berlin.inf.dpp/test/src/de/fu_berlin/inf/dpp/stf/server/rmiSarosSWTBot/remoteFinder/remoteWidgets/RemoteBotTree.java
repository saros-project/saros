package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.ContextMenuHelper;

public class RemoteBotTree extends AbstractRmoteWidget implements IRemoteBotTree {

    private static transient RemoteBotTree self;

    private SWTBotTree widget;

    /**
     * {@link RemoteBotTable} is a singleton, but inheritance is possible.
     */
    public static RemoteBotTree getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotTree();
        return self;
    }

    public IRemoteBotTree setWidget(SWTBotTree tree) {
        widget = tree;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/

    public IRemoteBotTreeItem[] getAllItems() throws RemoteException {
        IRemoteBotTreeItem[] items = new IRemoteBotTreeItem[widget.getAllItems().length];
        for (int i = 0; i < widget.getAllItems().length; i++) {
            items[i] = stfBotTreeItem.setWidget(widget.getAllItems()[i]);
        }
        return items;
    }

    public IRemoteBotMenu contextMenu(String... texts) throws RemoteException {
        stfBotMenu.setWidget(ContextMenuHelper.getContextMenu(widget, texts));
        return stfBotMenu;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public IRemoteBotTreeItem collapseNode(String nodeText) throws RemoteException {
        return stfBotTreeItem.setWidget(widget.collapseNode(nodeText));
    }

    public IRemoteBotTreeItem expandNode(String nodeText, boolean recursive)
        throws RemoteException {
        return stfBotTreeItem.setWidget(widget.expandNode(nodeText, recursive));
    }

    public IRemoteBotTreeItem expandNode(String... nodes) throws RemoteException {
        return stfBotTreeItem.setWidget(widget.expandNode(nodes));
    }

    public IRemoteBotTree select(int... indices) throws RemoteException {
        return setWidget(widget.select(indices));
    }

    public IRemoteBotTree select(String... items) throws RemoteException {
        return setWidget(widget.select(items));
    }

    public IRemoteBotTree unselect() throws RemoteException {
        return setWidget(widget.unselect());
    }

    public IRemoteBotTreeItem selectTreeItem(String... pathToTreeItem)
        throws RemoteException {
        stfBotTreeItem.setWidget(widget.expandNode(pathToTreeItem).select());
        stfBotTreeItem.setSWTBotTree(widget);
        return stfBotTreeItem;
    }

    public IRemoteBotTreeItem selectTreeItemWithRegex(String... regexNodes)
        throws RemoteException {
        assert widget != null : "the passed tree is null.";
        SWTBotTreeItem currentItem = null;
        SWTBotTreeItem[] allChildrenOfCurrentItem;
        for (String regex : regexNodes) {
            if (currentItem == null) {
                allChildrenOfCurrentItem = widget.getAllItems();
            } else {
                allChildrenOfCurrentItem = currentItem.getItems();
            }
            boolean itemWithRegexFound = false;
            for (SWTBotTreeItem child : allChildrenOfCurrentItem) {
                log.info("treeItem name: " + child.getText());
                if (child.getText().matches(regex)) {
                    currentItem = child;
                    if (!child.isExpanded())
                        child.expand();
                    itemWithRegexFound = true;
                    break;
                }
            }
            if (!itemWithRegexFound) {
                throw new WidgetNotFoundException("Tree item matching the \""
                    + regex + "\" can't be found. Nodes: "
                    + Arrays.asList(regexNodes));
            }
        }
        if (currentItem != null) {
            SWTBotTreeItem item = currentItem.select();
            stfBotTreeItem.setWidget(item);
            stfBotTreeItem.setSWTBotTree(widget);
            return stfBotTreeItem;
        }
        return null;
    }

    public IRemoteBotTreeItem selectTreeItemAndWait(String... pathToTreeItem)
        throws RemoteException {
        SWTBotTreeItem selectedTreeItem = null;
        for (String node : pathToTreeItem) {
            try {
                if (selectedTreeItem == null) {
                    waitUntilItemExists(node);
                    selectedTreeItem = widget.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                } else {

                    RemoteBotTreeItem treeItem = RemoteBotTreeItem
                        .getInstance();
                    treeItem.setWidget(selectedTreeItem);
                    treeItem.setSWTBotTree(widget);
                    treeItem.waitUntilSubItemExists(node);
                    selectedTreeItem = selectedTreeItem.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                }
            } catch (WidgetNotFoundException e) {
                log.error("treeitem \"" + node + "\" not found");
            }
        }
        if (selectedTreeItem != null) {
            log.info("treeItem name: " + selectedTreeItem.getText());

            SWTBotTreeItem item = selectedTreeItem.select();
            stfBotTreeItem.setWidget(item);
            stfBotTreeItem.setSWTBotTree(widget);
            return stfBotTreeItem;
        }
        return null;
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean hasItems() throws RemoteException {
        return widget.hasItems();
    }

    public int rowCount() throws RemoteException {
        return widget.rowCount();
    }

    public int selectionCount() throws RemoteException {
        return widget.selectionCount();
    }

    public int columnCount() throws RemoteException {
        return widget.columnCount();
    }

    public List<String> columns() throws RemoteException {
        return widget.columns();
    }

    public List<String> getTextOfItems() throws RemoteException {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : widget.getAllItems()) {
            allItemTexts.add(item.getText());
            log.info("existed treeItem of the tree: " + item.getText());
        }
        return allItemTexts;
    }

    public boolean existsSubItem(String treeItemText) throws RemoteException {
        return getTextOfItems().contains(treeItemText);
    }

    public boolean existsSubItemWithRegex(String regex) throws RemoteException {
        for (String subItem : getTextOfItems()) {
            if (subItem.matches(regex))
                return true;
        }
        return false;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilItemExists(final String itemText)
        throws RemoteException {

        stfBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsSubItem(itemText);
            }

            public String getFailureMessage() {
                return "Tree " + "doesn't contain the treeItem" + itemText;
            }
        });
    }

    public void waitUntilItemNotExists(final String itemText)
        throws RemoteException {
        stfBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !existsSubItem(itemText);
            }

            public String getFailureMessage() {
                return "Tree " + "doesn't contain the treeItem" + itemText;
            }
        });
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    public SWTBotTreeItem getTreeItemWithRegexs(SWTBotTree tree,
        String... regexPathToTreeItem) {
        SWTBotTreeItem currentItem = null;
        SWTBotTreeItem[] allChildrenOfCurrentItem;
        for (String regex : regexPathToTreeItem) {
            if (currentItem == null) {
                allChildrenOfCurrentItem = tree.getAllItems();
            } else {
                allChildrenOfCurrentItem = currentItem.getItems();
            }
            boolean itemFound = false;
            for (SWTBotTreeItem child : allChildrenOfCurrentItem) {
                log.info("treeItem name: " + child.getText());
                if (child.getText().matches(regex)) {
                    currentItem = child;
                    if (!child.isExpanded())
                        child.expand();
                    itemFound = true;
                    continue;
                }
            }
            if (!itemFound) {
                throw new WidgetNotFoundException("Tree item \"" + regex
                    + "\" not found. Nodes: "
                    + Arrays.asList(regexPathToTreeItem));
            }
        }
        return currentItem;
    }

}
