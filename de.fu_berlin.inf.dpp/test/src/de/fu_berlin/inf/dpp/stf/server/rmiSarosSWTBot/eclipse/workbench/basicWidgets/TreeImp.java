package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.ContextMenuHelper;

public class TreeImp extends EclipsePart implements Tree {

    private static transient TreeImp treeImp;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static TreeImp getInstance() {
        if (treeImp != null)
            return treeImp;
        treeImp = new TreeImp();
        return treeImp;
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
    public void selectTreeItem(String... pathToTreeItem) throws RemoteException {
        selectTreeItem(bot.tree(), pathToTreeItem);
    }

    public void selectTreeItemInView(String viewTitle, String... pathToTreeItem)
        throws RemoteException {
        selectTreeItem(getTreeInView(viewTitle), pathToTreeItem);
    }

    public void selectTreeItemWithRegexs(String... regexPathToTreeItem)
        throws RemoteException {
        selectTreeItemWithRegexs(bot.tree(), regexPathToTreeItem);
    }

    public void selectTreeItemWithRegexsInView(String viewTitle,
        String... regexPathToTreeItem) throws RemoteException {
        selectTreeItemWithRegexs(getTreeInView(viewTitle), regexPathToTreeItem);
    }

    public void selectTreeItemWithWaitingExpand(String... pathToTreeItem)
        throws RemoteException {
        selectTreeItemWithWaitingExpand(bot.tree(), pathToTreeItem);
    }

    public void selectTreeItemWithWaitingExpandInView(String viewTitle,
        String... pathToTreeItem) throws RemoteException {
        selectTreeItemWithWaitingExpand(getTreeInView(viewTitle),
            pathToTreeItem);
    }

    public void clickContextMenuOfTreeItem(String contextName,
        String... pathToTreeItem) throws RemoteException {
        clickContextMenuOfTreeItem(bot.tree(), contextName, pathToTreeItem);
    }

    public void clickContextMenuOfTreeItemInView(String viewTitle,
        String contextName, String... pathToTreeItem) throws RemoteException {
        clickContextMenuOfTreeItem(getTreeInView(viewTitle), contextName,
            pathToTreeItem);
    }

    public void clickContextMenusOfTreeItem(String[] contextNames,
        String... pathToTreeItem) throws RemoteException {
        clickContextMenusOfTreeItem(bot.tree(), contextNames, pathToTreeItem);
    }

    public void clickContextMenusOfTreeItemInView(String viewTitle,
        String[] contextNames, String... pathToTreeItem) throws RemoteException {
        clickContextMenusOfTreeItem(getTreeInView(viewTitle), contextNames,
            pathToTreeItem);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public List<String> getSubItemsInTreeItem(String... pathToTreeItem)
        throws RemoteException {
        SWTBotTree tree = bot.tree();
        SWTBotTreeItem treeItem = tree.expandNode(pathToTreeItem);
        return getSubItemsInTreeItem(treeItem);
    }

    public List<String> getSubItemsInTreeItemInView(String viewTitle,
        String... pathToTreeItem) throws RemoteException {
        SWTBotTreeItem treeItem = getTreeInView(viewTitle).expandNode(
            pathToTreeItem);
        return getSubItemsInTreeItem(treeItem);
    }

    public List<String> getSubtemsInTree() throws RemoteException {
        return getSubItemsInTree(bot.tree());
    }

    public List<String> getSubItemsInTreeInView(String viewTitle)
        throws RemoteException {
        return getSubItemsInTree(getTreeInView(viewTitle));
    }

    public boolean existsTreeItemWithRegexs(String... regexPathToTreeItem)
        throws RemoteException {
        return existsTreeItemWithRegexs(bot.tree(), regexPathToTreeItem);
    }

    public boolean existsTreeItemWithRegexsInView(String viewTitle,
        String... regexPathToTreeItem) throws RemoteException {
        return existsTreeItemWithRegexs(getTreeInView(viewTitle),
            regexPathToTreeItem);
    }

    public boolean existsTreeItemInTree(String treeItemText)
        throws RemoteException {
        return existsTreeItemInTree(bot.tree(), treeItemText);
    }

    public boolean existsTreeItemInTreeInView(String viewTitle,
        String treeItemText) throws RemoteException {
        return existsTreeItemInTree(getTreeInView(viewTitle), treeItemText);
    }

    public boolean existsSubItemInTreeItem(String subItemText,
        String... pathToTreeItem) throws RemoteException {
        return getSubItemsInTreeItem(pathToTreeItem).contains(subItemText);
    }

    public boolean existsSubItemInTreeItemInView(String viewTitle,
        String subItemText, String... pathToTreeItem) throws RemoteException {
        return getSubItemsInTreeItemInView(viewTitle, pathToTreeItem).contains(
            subItemText);
    }

    public boolean existsContextMenuOfTreeItem(String contextName,
        String... pathToTreeItem) throws RemoteException {
        return existsContextMenuOfTreeItem(bot.tree(), contextName,
            pathToTreeItem);
    }

    public boolean existsContextMenuOfTreeItemInView(String viewTitle,
        String contextMenuName, String... pathToTreeItem)
        throws RemoteException {
        return existsContextMenuOfTreeItem(getTreeInView(viewTitle),
            contextMenuName, pathToTreeItem);
    }

    public boolean existsContextMenusOfTreeItem(String[] contextNames,
        String... pathToTreeItem) throws RemoteException {
        return existsContextMenusOfTreeItem(bot.tree(), contextNames,
            pathToTreeItem);
    }

    public boolean existsContextMenusOfTreeItemInView(String viewTitle,
        String[] contextNames, String... pathToTreeItem) throws RemoteException {
        return existsContextMenusOfTreeItem(getTreeInView(viewTitle),
            contextNames, pathToTreeItem);
    }

    public boolean isContextMenuOfTreeItemEnabled(String contextName,
        String... pathToTreeItem) throws RemoteException {
        return isContextMenuOfTreeItemEnabled(bot.tree(), contextName,
            pathToTreeItem);
    }

    public boolean isContextMenuOfTreeItemInViewEnabled(String viewTitle,
        String contextName, String... pathToTreeItem) throws RemoteException {
        return isContextMenuOfTreeItemEnabled(getTreeInView(viewTitle),
            contextName, pathToTreeItem);
    }

    public boolean existsContextMenuOfTreeItem(SWTBotTree tree,
        String contextName, String... pathToTreeItem) {
        selectTreeItemWithRegexs(tree, pathToTreeItem);
        return ContextMenuHelper.existsContextMenu(tree, contextName);
    }

    public boolean existsContextMenusOfTreeItem(SWTBotTree tree,
        String[] contextMenuNames, String... pathToTreeItem) {
        selectTreeItemWithRegexs(tree, pathToTreeItem);
        return ContextMenuHelper.existsContextMenu(tree, contextMenuNames);
    }

    public boolean isContextMenuOfTreeItemEnabled(SWTBotTree tree,
        String contextName, String... pathToTreeItem) {
        selectTreeItemWithRegexs(tree, pathToTreeItem);
        return ContextMenuHelper.isContextMenuEnabled(tree, contextName);
    }

    public boolean isContextMenusOfTreeItemEnabled(String[] contextNames,
        String... pathToTreeItem) throws RemoteException {
        return isContextMenusOfTreeItemEnabled(bot.tree(), contextNames,
            pathToTreeItem);
    }

    public boolean isContextMenusOfTreeItemInViewEnabled(String viewTitle,
        String[] contextNames, String... pathToTreeItem) throws RemoteException {
        return isContextMenusOfTreeItemEnabled(getTreeInView(viewTitle),
            contextNames, pathToTreeItem);
    }

    public boolean isContextMenusOfTreeItemEnabled(SWTBotTree tree,
        String[] contextMenuNames, String... pathToTreeItem) {
        selectTreeItemWithRegexs(tree, pathToTreeItem);
        return ContextMenuHelper.isContextMenuEnabled(tree, contextMenuNames);
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsTreeItemInTreeExisted(final String itemText)
        throws RemoteException {
        waitUntilIsTreeItemInTreeExisted(bot.tree(), itemText);
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/
    public void waitUntilIsTreeItemInTreeExisted(final SWTBotTree tree,
        final String itemText) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsTreeItemInTree(tree, itemText);
            }

            public String getFailureMessage() {
                return "Tree " + "doesn't contain the treeItem" + itemText;
            }
        });
    }

    public void selectTreeItem(SWTBotTree tree, String... pathToTreeItem) {
        try {
            tree.expandNode(pathToTreeItem).select();
        } catch (WidgetNotFoundException e) {
            log.warn("tree item can't be found.", e);
        }
    }

    public boolean existsTreeItemInTree(SWTBotTree tree, String treeItemText) {
        return getSubItemsInTree(tree).contains(treeItemText);
    }

    public List<String> getSubItemsInTree(SWTBotTree tree) {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : tree.getAllItems()) {
            allItemTexts.add(item.getText());
            log.info("existed treeItem of the tree: " + item.getText());
        }
        return allItemTexts;
    }

    public void waitUntilIsSubItemInTreeItemExisted(final String subItemText,
        final String... pathToTreeItem) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsSubItemInTreeItem(subItemText, pathToTreeItem);
            }

            public String getFailureMessage() {
                return "The tree node" + "doesn't contain the treeItem"
                    + subItemText;
            }
        });
    }

    public void waitUntilIsSubItemInTreeItemExisted(
        final SWTBotTreeItem treeItem, final String subItemText)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsSubItemInTreeItem(treeItem, subItemText);
            }

            public String getFailureMessage() {
                return "TreeNode " + treeItem.getText()
                    + "doesn't contain the treeItem" + subItemText;
            }
        });
    }

    public boolean existsSubItemInTreeItem(SWTBotTreeItem treeItem,
        String subItemText) {
        return getSubItemsInTreeItem(treeItem).contains(subItemText);
    }

    public List<String> getSubItemsInTreeItem(SWTBotTreeItem treeItem) {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : treeItem.getItems()) {
            allItemTexts.add(item.getText());
            log.info("existed subTreeItem of the TreeItem "
                + treeItem.getText() + ": " + item.getText());
        }
        return allItemTexts;
    }

    /**
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @return a {@link SWTBotTree} with the specified <code>none</code> in
     *         specified view.
     */
    public SWTBotTree getTreeInView(String viewTitle) {
        return bot.viewByTitle(viewTitle).bot().tree();
    }

    /**
     * select a treeItem specified with the given regexs. This method ist very
     * helpful, if you are not sure, how exactly is the tree item's name.
     * 
     * @param tree
     *            a {@link SWTBotTree} with the specified <code>none</code>
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array parameter.e.g.
     *            {"Buddies","bob_stf@saros-con.imp.fu-berlin.de.*" }
     * 
     */
    public void selectTreeItemWithRegexs(SWTBotTree tree, String... regexNodes) {
        assert tree != null : "the passed tree is null.";
        SWTBotTreeItem currentItem = null;
        SWTBotTreeItem[] allChildrenOfCurrentItem;
        for (String regex : regexNodes) {
            if (currentItem == null) {
                allChildrenOfCurrentItem = tree.getAllItems();
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
        if (currentItem != null)
            currentItem.select();
    }

    /**
     * This method ist very helpful, if you are not sure, how exactly is the
     * tree item's name.
     * 
     * @param tree
     *            a {@link SWTBotTree} with the specified <code>none</code>
     * @param regexs
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array parameter.e.g.
     *            {"Buddies","bob_stf@saros-con.imp.fu-berlin.de.*" }
     * @return <tt>true</tt>, if the three item specified with the given regexs
     *         exists
     */
    public boolean existsTreeItemWithRegexs(SWTBotTree tree, String... regexs) {
        try {
            selectTreeItemWithRegexs(tree, regexs);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public void selectTreeItemWithWaitingExpand(SWTBotTree tree,
        String... pathToTreeItem) throws RemoteException {
        SWTBotTreeItem selectedTreeItem = null;
        for (String node : pathToTreeItem) {
            try {
                if (selectedTreeItem == null) {
                    waitUntilIsTreeItemInTreeExisted(tree, node);
                    selectedTreeItem = tree.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                } else {
                    waitUntilIsSubItemInTreeItemExisted(selectedTreeItem, node);
                    selectedTreeItem = selectedTreeItem.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                }
            } catch (WidgetNotFoundException e) {
                log.error("treeitem \"" + node + "\" not found");
            }
        }
        if (selectedTreeItem != null) {
            log.info("treeItem name: " + selectedTreeItem.getText());
            selectedTreeItem.select();

        }
    }

    /**
     * @param viewTitle
     *            the title on the view tab.
     * @param pathToTreeItem
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return a {@link SWTBotTreeItem} specified with the given
     *         <code>nodes</code> in the given view.
     */
    public SWTBotTreeItem getTreeItemInView(String viewTitle,
        String... pathToTreeItem) {
        try {
            return getTreeInView(viewTitle).expandNode(pathToTreeItem).select();
        } catch (WidgetNotFoundException e) {
            return null;
        }
    }

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

    /**
     * 
     * @param pathToTreeItem
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return a {@link SWTBotTreeItem} specified with the given
     *         <code>nodes</code>.
     */
    public SWTBotTreeItem getTreeItem(String... pathToTreeItem) {
        return bot.tree().expandNode(pathToTreeItem);
    }

    public void clickContextMenuOfTreeItem(SWTBotTree tree,
        String contextMenuName, String... pathToTreeItem) {
        selectTreeItemWithRegexs(tree, pathToTreeItem);
        ContextMenuHelper.clickContextMenu(tree, contextMenuName);
    }

    public void clickContextMenusOfTreeItem(SWTBotTree tree,
        String[] contextMenuNames, String... pathTotreeItem) {
        selectTreeItemWithRegexs(tree, pathTotreeItem);
        ContextMenuHelper.clickContextMenu(tree, contextMenuNames);
    }
}
