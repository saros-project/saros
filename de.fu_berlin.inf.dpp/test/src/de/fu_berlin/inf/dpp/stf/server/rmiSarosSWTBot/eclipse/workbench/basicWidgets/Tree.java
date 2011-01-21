package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public interface Tree extends Remote {

    /**********************************************
     * 
     * actions with basic widget: {@link SWTBotTree}.
     * 
     **********************************************/

    /***************** select tree item ****************** */

    /**
     * Selects the treeItem matching the given nodes in the tree: bot.tree().
     * This method is suitable for Shell widget.
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void selectTreeItem(String... nodes) throws RemoteException;

    /**
     * Selects the treeItem matching the given nodes in the tree:
     * bot.getViewTitle(viewTitle).bot().tree(). This method is suitable for
     * view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void selectTreeItemInView(String viewTitle, String... nodes)
        throws RemoteException;

    /***************** select tree item with regexs ******************/

    /**
     * Selects the treeItem matching the given Regexnodes in the tree:
     * bot.tree(). This method is suitable for shell widget.
     * 
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * 
     * @throws WidgetNotFoundException
     *             If the item wasn't found.
     */
    public void selectTreeItemWithRegexs(String... regexNodes)
        throws RemoteException;

    /**
     * Selects the treeItem matching the given Regexnodes in the tree:
     * bot.getViewTitle(viewTitle).bot().tree(). This method is suitable for
     * view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * @throws RemoteException
     */
    public void selectTreeItemWithRegexsInView(String viewTitle,
        String... regexNodes) throws RemoteException;

    /***************** select tree item with waiting expand ****************** */
    /**
     * Selects the treeItem matching the given nodes in the tree: bot.tree()
     * with waiting until the parentTreeItem is expanded. This method is
     * suitable for shell widget.
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void selectTreeItemWithWaitingExpand(String... nodes)
        throws RemoteException;;

    /**
     * Selects the treeItem matching the given nodes in the tree:
     * bot.getViewTitle(viewTitle).bot().tree() with waiting until the
     * parentTreeItem is expanded. This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void selectTreeItemWithWaitingExpandInView(String viewTitle,
        String... nodes) throws RemoteException;

    /***************** exist tree item with regexs ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * @return<tt>true</tt>, if the treeItem specified with the given regexNodes
     *                       exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemWithRegexs(String... regexNodes)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * @return<tt>true</tt>, if the treeItem specified with the given regexNodes
     *                       exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemWithRegexsInView(String viewTitle,
        String... regexNodes) throws RemoteException;

    /***************** exists tree item ****************** */
    /**
     * This method is suitable for shell widget.
     * 
     * @param itemText
     *            name of the tree item.
     * @return<tt>true</tt>, if the treeItem in the bot.tree() exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemInTree(String itemText) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            name of the tree item.
     * @return<tt>true</tt>, if the treeItem in the
     *                       bot.viewByTitle(viewTitle).bot().tree() exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemInTreeInView(String viewTitle, String itemText)
        throws RemoteException;

    /**
     * This method is suitable for shell widget.
     * 
     * @param itemText
     *            name of the tree item.
     * @param nodes
     *            parent node path of the treeItem to expand. Attempts to expand
     *            all nodes along the path specified by the node array
     *            parameter.
     * @return<tt>true</tt>, if the treeItem in the treeNode specified with the
     *                       given nodes exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemInTreeNode(String itemText, String... nodes)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            name of the tree item.
     * @param nodes
     *            parent node path of the treeItem to expand. Attempts to expand
     *            all nodes along the path specified by the node array
     *            parameter.
     * @return<tt>true</tt>, if the treeItem in the treeNode specified with the
     *                       given nodes exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemInTreeNodeInView(String viewTitle,
        String itemText, String... nodes) throws RemoteException;

    /**
     * This method is suitable for shell widget.
     * 
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsContextOfTreeItem(String contextName, String... nodes)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsContextOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    /**
     * This method is suitable for shell widget.
     * 
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the subMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsSuMenuOfContextOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the subMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsSubmenuOfContextOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

    /***************** is context of tree item enabled****************** */
    /**
     * This method is suitable for sehll widget.
     * 
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem is
     *                       enabled.
     * @throws RemoteException
     */
    public boolean isContextOfTreeItemEnabled(String contextName,
        String... nodes) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem is
     *                       enabled.
     * @throws RemoteException
     */
    public boolean isContextOfTreeItemInViewEnabled(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    /**
     * This method is suitable for shell widget.
     * 
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the subMenu of the selected TreeItem is enabled.
     * @throws RemoteException
     */
    public boolean isSuMenuOfContextOfTreeItemEnabled(String[] contextNames,
        String... nodes) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the subMenu of the selected TreeItem is enabled.
     * @throws RemoteException
     */
    public boolean isSubmenuOfContextOfTreeItemInViewEnabled(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

    /***************** click context of tree item ****************** */
    /**
     * 
     * This method is suitable for shell widget.
     * 
     * Clicks the contextMenu of the selected TreeItem.
     * 
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickContextsOfTreeItem(String contextName, String... nodes)
        throws RemoteException;

    /**
     * 
     * This method is suitable for view widget.
     * 
     * Clicks the contextMenu of the selected TreeItem.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickContextsOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    /**
     * 
     * This method is suitable for shell widget.
     * 
     * Clicks the subMenu of the selected TreeItem.
     * 
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickSubMenuOfContextsOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException;

    /**
     * 
     * This method is suitable for view widget.
     * 
     * Clicks the subMenu of the selected TreeItem.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickSubMenuOfContextsOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

    /***************** get allItems in treeNode ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return all the treeItem'name of the given TreeNode.
     * @throws RemoteException
     */
    public List<String> getAllItemsInTreeNode(String... nodes)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return all the treeItem'name of the given TreeNode.
     * @throws RemoteException
     */
    public List<String> getAllItemsInTreeNodeInView(String viewTitle,
        String... nodes) throws RemoteException;

    /***************** get allItems in tree ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @return all the treeItem'name of the bot.tree().
     * @throws RemoteException
     */
    public List<String> getAllItemsIntree() throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @return all the treeItem'name of the
     *         bot.getViewByTitle(viewTitle).bot().tree().
     * @throws RemoteException
     */
    public List<String> getAllItemsIntreeInView(String viewTitle)
        throws RemoteException;

    public void waitUntilTreeItemInTreeExisted(String nodeName)
        throws RemoteException;

    public void waitUntilTreeItemInTreeExisted(final SWTBotTree tree,
        final String itemText) throws RemoteException;

    public void waitUntilTreeItemInTreeNodeExisted(String itemText,
        String... nodes) throws RemoteException;

    public void waitUntilTreeItemInTreeNodeExisted(SWTBotTreeItem treeItem,
        String nodeName) throws RemoteException;

}
