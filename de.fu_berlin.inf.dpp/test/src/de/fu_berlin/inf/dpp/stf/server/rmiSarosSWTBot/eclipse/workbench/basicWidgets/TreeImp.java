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

    // actions
    public void selectTreeItem(String... nodes) throws RemoteException {
        selectTreeItem(bot.tree(), nodes);
    }

    public void selectTreeItem(SWTBotTree tree, String... nodes) {
        try {
            tree.expandNode(nodes).select();
        } catch (WidgetNotFoundException e) {
            log.warn("tree item can't be found.", e);
        }
    }

    public void selectTreeItemInView(String viewTitle, String... nodes)
        throws RemoteException {
        selectTreeItem(getTreeInView(viewTitle), nodes);
    }

    public void selectTreeItemWithRegexs(String... regexNodes)
        throws RemoteException {
        selectTreeItemWithRegexs(bot.tree(), regexNodes);
    }

    public void selectTreeItemWithRegexsInView(String viewTitle,
        String... regexNodes) throws RemoteException {
        selectTreeItemWithRegexs(getTreeInView(viewTitle), regexNodes);
    }

    public void selectTreeItemWithWaitingExpand(String... nodes)
        throws RemoteException {
        selectTreeItemWithWaitingExpand(bot.tree(), nodes);
    }

    public void selectTreeItemWithWaitingExpandInView(String viewTitle,
        String... nodes) throws RemoteException {
        selectTreeItemWithWaitingExpand(getTreeInView(viewTitle), nodes);
    }

    public void clickContextsOfTreeItem(String contextName, String... nodes)
        throws RemoteException {
        clickContextsOfTreeItem(bot.tree(), contextName, nodes);
    }

    public void clickContextsOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException {
        clickContextsOfTreeItem(getTreeInView(viewTitle), contextName, nodes);
    }

    public void clickSubMenuOfContextsOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException {
        clickSubMenuOfContextsOfTreeItem(bot.tree(), contextNames, nodes);
    }

    public void clickSubMenuOfContextsOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException {
        clickSubMenuOfContextsOfTreeItem(getTreeInView(viewTitle),
            contextNames, nodes);
    }

    // states
    public List<String> getAllItemsInTreeNode(String... nodes)
        throws RemoteException {
        SWTBotTree tree = bot.tree();
        SWTBotTreeItem treeNode = tree.expandNode(nodes);
        return getAllItemsInTreeNode(treeNode);
    }

    public List<String> getAllItemsInTreeNodeInView(String viewTitle,
        String... nodes) throws RemoteException {
        SWTBotTreeItem treeNode = getTreeInView(viewTitle).expandNode(nodes);
        return getAllItemsInTreeNode(treeNode);
    }

    public List<String> getAllItemsIntree() throws RemoteException {
        return getAllItemsIntree(bot.tree());
    }

    public List<String> getAllItemsIntreeInView(String viewTitle)
        throws RemoteException {
        return getAllItemsIntree(getTreeInView(viewTitle));
    }

    public boolean existsTreeItemWithRegexs(String... regexs)
        throws RemoteException {
        return existsTreeItemWithRegexs(bot.tree(), regexs);
    }

    public boolean existsTreeItemWithRegexsInView(String viewTitle,
        String... regexs) throws RemoteException {
        return existsTreeItemWithRegexs(getTreeInView(viewTitle), regexs);
    }

    public boolean existsTreeItemInTree(String itemText) throws RemoteException {
        return existsTreeItemInTree(bot.tree(), itemText);
    }

    public boolean existsTreeItemInTreeInView(String viewTitle, String itemText)
        throws RemoteException {
        return existsTreeItemInTree(getTreeInView(viewTitle), itemText);
    }

    public boolean existsTreeItemInTreeNode(String itemText, String... nodes)
        throws RemoteException {
        return getAllItemsInTreeNode(nodes).contains(itemText);
    }

    public boolean existsTreeItemInTreeNodeInView(String viewTitle,
        String itemText, String... nodes) throws RemoteException {
        return getAllItemsInTreeNodeInView(viewTitle, nodes).contains(itemText);
    }

    public boolean existsContextOfTreeItem(String contextName, String... nodes)
        throws RemoteException {
        return existsContextOfTreeItem(bot.tree(), contextName, nodes);
    }

    public boolean existsContextOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException {
        return existsContextOfTreeItem(getTreeInView(viewTitle), contextName,
            nodes);
    }

    public boolean existsSuMenuOfContextOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException {
        return existsSubMenuOfContextOfTreeItem(bot.tree(), contextNames, nodes);
    }

    public boolean existsSubmenuOfContextOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException {
        return existsSubMenuOfContextOfTreeItem(getTreeInView(viewTitle),
            contextNames, nodes);
    }

    public boolean isContextOfTreeItemEnabled(String contextName,
        String... nodes) throws RemoteException {
        return isContextOfTreeItemEnabled(bot.tree(), contextName, nodes);
    }

    public boolean isContextOfTreeItemInViewEnabled(String viewTitle,
        String contextName, String... nodes) throws RemoteException {
        return isContextOfTreeItemEnabled(getTreeInView(viewTitle),
            contextName, nodes);
    }

    public boolean existsContextOfTreeItem(SWTBotTree tree, String contextName,
        String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        return ContextMenuHelper.existsContextMenu(tree, contextName);
    }

    public boolean existsSubMenuOfContextOfTreeItem(SWTBotTree tree,
        String[] contextNames, String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        return ContextMenuHelper.existsContextMenu(tree, contextNames);
    }

    public boolean isContextOfTreeItemEnabled(SWTBotTree tree,
        String contextName, String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        return ContextMenuHelper.isContextMenuEnabled(tree, contextName);
    }

    public void clickContextsOfTreeItem(SWTBotTree tree, String contextName,
        String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        ContextMenuHelper.clickContextMenu(tree, contextName);
    }

    public void clickSubMenuOfContextsOfTreeItem(SWTBotTree tree,
        String[] contextNames, String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        ContextMenuHelper.clickContextMenu(tree, contextNames);
    }

    public boolean isSuMenuOfContextOfTreeItemEnabled(String[] contextNames,
        String... nodes) throws RemoteException {
        return isSubMenuOfContextOfTreeItemEnabled(bot.tree(), contextNames,
            nodes);
    }

    public boolean isSubmenuOfContextOfTreeItemInViewEnabled(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException {
        return isSubMenuOfContextOfTreeItemEnabled(getTreeInView(viewTitle),
            contextNames, nodes);
    }

    public boolean isSubMenuOfContextOfTreeItemEnabled(SWTBotTree tree,
        String[] contextNames, String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        return ContextMenuHelper.isContextMenuEnabled(tree, contextNames);
    }

    // waits until
    public void waitUntilTreeItemInTreeExisted(final String itemText)
        throws RemoteException {
        waitUntilTreeItemInTreeExisted(bot.tree(), itemText);
    }

    public void waitUntilTreeItemInTreeExisted(final SWTBotTree tree,
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

    public boolean existsTreeItemInTree(SWTBotTree tree, String itemText) {
        return getAllItemsIntree(tree).contains(itemText);
    }

    public List<String> getAllItemsIntree(SWTBotTree tree) {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : tree.getAllItems()) {
            allItemTexts.add(item.getText());
            log.info("existed treeItem of the tree: " + item.getText());
        }
        return allItemTexts;
    }

    public void waitUntilTreeItemInTreeNodeExisted(final String itemText,
        final String... nodes) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsTreeItemInTreeNode(itemText, nodes);
            }

            public String getFailureMessage() {
                return "The tree node" + "doesn't contain the treeItem"
                    + itemText;
            }
        });
    }

    public void waitUntilTreeItemInTreeNodeExisted(
        final SWTBotTreeItem treeNode, final String itemText)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsTreeItemInTreeNode(treeNode, itemText);
            }

            public String getFailureMessage() {
                return "TreeNode " + treeNode.getText()
                    + "doesn't contain the treeItem" + itemText;
            }
        });
    }

    public boolean existsTreeItemInTreeNode(SWTBotTreeItem treeNode,
        String itemText) {
        return getAllItemsInTreeNode(treeNode).contains(itemText);
    }

    public List<String> getAllItemsInTreeNode(SWTBotTreeItem treeNode) {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : treeNode.getItems()) {
            allItemTexts.add(item.getText());
            log.info("existed subTreeItem of the TreeNode "
                + treeNode.getText() + ": " + item.getText());
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
        String... nodes) throws RemoteException {
        SWTBotTreeItem selectedTreeItem = null;
        for (String node : nodes) {
            try {
                if (selectedTreeItem == null) {
                    waitUntilTreeItemInTreeExisted(tree, node);
                    selectedTreeItem = tree.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                } else {
                    waitUntilTreeItemInTreeNodeExisted(selectedTreeItem, node);
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
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return a {@link SWTBotTreeItem} specified with the given
     *         <code>nodes</code> in the given view.
     */
    public SWTBotTreeItem getTreeItemInView(String viewTitle, String... nodes) {
        try {
            return getTreeInView(viewTitle).expandNode(nodes).select();
        } catch (WidgetNotFoundException e) {
            return null;
        }
    }

    public SWTBotTreeItem getTreeItemWithRegexs(SWTBotTree tree,
        String... regexNodes) {
        SWTBotTreeItem currentItem = null;
        SWTBotTreeItem[] allChildrenOfCurrentItem;
        for (String regex : regexNodes) {
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
                    + "\" not found. Nodes: " + Arrays.asList(regexNodes));
            }
        }
        return currentItem;
    }

    /**
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return a {@link SWTBotTreeItem} specified with the given
     *         <code>nodes</code>.
     */
    public SWTBotTreeItem getTreeItem(String... nodes) {
        return bot.tree().expandNode(nodes);
    }
}
