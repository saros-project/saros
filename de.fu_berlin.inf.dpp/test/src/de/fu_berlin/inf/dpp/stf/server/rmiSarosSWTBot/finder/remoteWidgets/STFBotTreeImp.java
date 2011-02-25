package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class STFBotTreeImp extends AbstractRmoteWidget implements STFBotTree {

    private static transient STFBotTreeImp self;

    private SWTBotTree widget;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTreeImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotTreeImp();
        return self;
    }

    public STFBotTree setWidget(SWTBotTree tree) {
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

    public STFBotTreeItem[] getAllItems() throws RemoteException {
        STFBotTreeItem[] items = new STFBotTreeItem[widget.getAllItems().length];
        for (int i = 0; i < widget.getAllItems().length; i++) {
            items[i] = stfBotTreeItem.setWidget(widget.getAllItems()[i]);
        }
        return items;
    }

    public STFBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public STFBotTreeItem collapseNode(String nodeText) throws RemoteException {
        return stfBotTreeItem.setWidget(widget.collapseNode(nodeText));
    }

    public STFBotTreeItem expandNode(String nodeText, boolean recursive)
        throws RemoteException {
        return stfBotTreeItem.setWidget(widget.expandNode(nodeText, recursive));
    }

    public STFBotTreeItem expandNode(String... nodes) throws RemoteException {
        return stfBotTreeItem.setWidget(widget.expandNode(nodes));
    }

    public STFBotTree select(int... indices) throws RemoteException {
        return setWidget(widget.select(indices));
    }

    public STFBotTree select(String... items) throws RemoteException {
        return setWidget(widget.select(items));
    }

    public STFBotTree unselect() throws RemoteException {
        return setWidget(widget.unselect());
    }

    public STFBotTreeItem selectTreeItem(String... pathToTreeItem)
        throws RemoteException {
        stfBotTreeItem.setWidget(widget.expandNode(pathToTreeItem).select());
        stfBotTreeItem.setSWTBotTree(widget);
        return stfBotTreeItem;
    }

    public STFBotTreeItem selectTreeItemWithRegex(String... regexNodes)
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

    public STFBotTreeItem selectTreeItemAndWait(String... pathToTreeItem)
        throws RemoteException {
        SWTBotTreeItem selectedTreeItem = null;
        for (String node : pathToTreeItem) {
            try {
                if (selectedTreeItem == null) {
                    waitUntilItemExists(node);
                    selectedTreeItem = widget.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                } else {

                    STFBotTreeItemImp treeItem = STFBotTreeItemImp
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

    public boolean existsSubItemWithRegexs(String regex) throws RemoteException {
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
