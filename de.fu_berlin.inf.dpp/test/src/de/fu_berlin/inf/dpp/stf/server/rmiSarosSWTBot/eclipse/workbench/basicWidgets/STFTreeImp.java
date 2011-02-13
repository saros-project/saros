package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class STFTreeImp extends EclipseComponentImp implements STFTree {

    private static transient STFTreeImp treeImp;

    private SWTBotTree swtBotTree;

    /**
     * {@link STFTableImp} is a singleton, but inheritance is possible.
     */
    public static STFTreeImp getInstance() {
        if (treeImp != null)
            return treeImp;
        treeImp = new STFTreeImp();
        return treeImp;
    }

    public void setSWTBotTree(SWTBotTree tree) {
        swtBotTree = tree;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public STFTreeItem selectTreeItem(String... pathToTreeItem)
        throws RemoteException {
        try {
            SWTBotTreeItem item = swtBotTree.expandNode(pathToTreeItem)
                .select();
            STFTreeItem treeItem = STFTreeItemImp.getInstance();
            treeItem.setSWTBotTreeItem(item);
            treeItem.setSWTBotTree(swtBotTree);
            return treeItem;

        } catch (WidgetNotFoundException e) {
            log.warn("tree item can't be found.", e);
        }
        return null;
    }

    public STFTreeItem selectTreeItemWithRegex(String... regexNodes)
        throws RemoteException {
        assert swtBotTree != null : "the passed tree is null.";
        SWTBotTreeItem currentItem = null;
        SWTBotTreeItem[] allChildrenOfCurrentItem;
        for (String regex : regexNodes) {
            if (currentItem == null) {
                allChildrenOfCurrentItem = swtBotTree.getAllItems();
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
            STFTreeItem treeItem = STFTreeItemImp.getInstance();
            treeItem.setSWTBotTreeItem(item);
            treeItem.setSWTBotTree(swtBotTree);
            return treeItem;
        }
        return null;
    }

    public STFTreeItem selectTreeItemAndWait(String... pathToTreeItem)
        throws RemoteException {
        SWTBotTreeItem selectedTreeItem = null;
        for (String node : pathToTreeItem) {
            try {
                if (selectedTreeItem == null) {
                    waitUntilSubItemExists(node);
                    selectedTreeItem = swtBotTree.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                } else {

                    STFTreeItem treeItem = STFTreeItemImp.getInstance();
                    treeItem.setSWTBotTreeItem(selectedTreeItem);
                    treeItem.setSWTBotTree(swtBotTree);
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
            STFTreeItem treeItem = STFTreeItemImp.getInstance();
            treeItem.setSWTBotTreeItem(item);
            treeItem.setSWTBotTree(swtBotTree);
            return treeItem;
        }
        return null;
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public List<String> getSubtems() throws RemoteException {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : swtBotTree.getAllItems()) {
            allItemTexts.add(item.getText());
            log.info("existed treeItem of the tree: " + item.getText());
        }
        return allItemTexts;
    }

    public boolean existsSubItem(String treeItemText) throws RemoteException {
        return getSubtems().contains(treeItemText);
    }

    public boolean existsSubItemWithRegexs(String regex) throws RemoteException {
        for (String subItem : getSubtems()) {
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
    public void waitUntilSubItemExists(final String itemText)
        throws RemoteException {

        waitUntil(new DefaultCondition() {
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
