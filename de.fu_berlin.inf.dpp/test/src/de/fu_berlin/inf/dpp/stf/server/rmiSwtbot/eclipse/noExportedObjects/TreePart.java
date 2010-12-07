package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class TreePart extends EclipseComponent {

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
    public boolean isTreeItemWithMatchTextExist(SWTBotTree tree,
        String... regexs) {
        try {
            getTreeItemWithRegexNodes(tree, regexs);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**
     * 
     * @param tree
     *            a {@link SWTBotTree} with the specified <code>none</code>
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * @return the {@link SWTBotTree} with the given regexNodes
     * @throws WidgetNotFoundException
     *             If the item wasn't found.
     */
    public SWTBotTreeItem getTreeItemWithRegexNodes(SWTBotTree tree,
        String... regexNodes) {
        SWTBotTreeItem currentItem = null;
        SWTBotTreeItem[] allTreeItems;
        for (String regex : regexNodes) {
            if (currentItem == null) {
                allTreeItems = tree.getAllItems();
            } else {
                allTreeItems = currentItem.getItems();
            }
            boolean itemFound = false;
            for (SWTBotTreeItem item : allTreeItems) {
                log.info("treeItem name: " + item.getText());
                if (item.getText().matches(regex)) {
                    currentItem = item;
                    if (!item.isExpanded())
                        item.expand();
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

    public SWTBotTreeItem selectTreeWithLabelsWithWaitungExpand(
        SWTBotTree tree, String... labels) {
        SWTBotTreeItem selectedTreeItem = null;
        for (String label : labels) {
            try {
                if (selectedTreeItem == null) {
                    treePart.waitUntilTreeExisted(tree, label);
                    selectedTreeItem = tree.expandNode(label);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                } else {
                    treePart.waitUntilTreeNodeExisted(selectedTreeItem, label);
                    selectedTreeItem = selectedTreeItem.expandNode(label);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                }
            } catch (WidgetNotFoundException e) {
                log.error("treeitem \"" + label + "\" not found");
            }
        }
        if (selectedTreeItem != null) {
            log.info("treeItem name: " + selectedTreeItem.getText());
            selectedTreeItem.select();
            return selectedTreeItem;
        }
        return null;
    }

    // public SWTBotTreeItem selectTreeWithLabels(SWTBotTree tree,
    // String... labels) {
    // try {
    // return tree.expandNode(labels);
    // } catch (WidgetNotFoundException e) {
    // log.warn("table item not found.", e);
    // return null;
    // }
    //
    // }

    public List<String> getAllItemsOfTreeItem(String... paths) {
        SWTBotTree tree = bot.tree();
        SWTBotTreeItem item = tree.expandNode(paths);
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < item.getItems().length; i++) {
            list.add(item.getItems()[i].getText());
            log.info("existed item In TreeItem:  " + list.get(i));
        }
        return list;
    }

    public List<String> getAllItemsOftree() {
        SWTBotTree tree = bot.tree();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < tree.getAllItems().length; i++) {
            list.add(tree.getAllItems()[i].getText());
            log.info("existed TreeItem in Tree:  " + list.get(i));
        }
        return list;
    }

    public boolean isTreeItemOfTreeExisted(SWTBotTree tree, String label) {
        return getAllItemsOftree().contains(label);
    }

    public void waitUntilTreeItemExisted(SWTBotTreeItem treeItem,
        String nodeName) {
        waitUntil(SarosConditions.existTreeItem(treeItem, nodeName));
    }

    public void waitUntilTreeExisted(SWTBotTree tree, String nodeName) {
        waitUntil(SarosConditions.existTree(tree, nodeName));
    }

    public void waitUntilTreeNodeExisted(SWTBotTreeItem treeItem,
        String nodeName) {
        waitUntil(SarosConditions.existTreeItem(treeItem, nodeName));
    }

    // /**
    // * This method is very useful, if you want to click a submenu of the
    // context
    // * menu of a selected treeitem.e.g. the context menu "Team->Commit...".
    // You
    // * should first select the project that his context menu you want to
    // click.
    // *
    // * @param nodes
    // * node path to expand. Attempts to expand all nodes along the
    // * path specified by the node array parameter.e.g.{"Foo-saros",
    // * "my.pkg", "myClass.java"}
    // * @param contexts
    // * all context menus'name.e.g. {"Team", "Commit..."}
    // */
    // public void clickContextMenusOfTreeItem(String[] nodes, String...
    // contexts) {
    // try {
    // SWTBotTree tree = bot.tree();
    // SWTBotTreeItem treeItem = getTreeItemWithMatchText(tree, nodes);
    // treeItem.select();
    // ContextMenuHelper.clickContextMenu(tree, contexts);
    // } catch (WidgetNotFoundException e) {
    // log.error("context menu can't be found.", e);
    // }
    // }
}
