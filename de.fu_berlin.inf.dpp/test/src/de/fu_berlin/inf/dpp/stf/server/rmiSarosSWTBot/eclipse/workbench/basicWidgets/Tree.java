package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;

public interface Tree extends EclipseComponent {

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
    public Tree selectTreeItem(String... nodes) throws RemoteException;

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
    public Tree selectTreeItemWithRegexs(String... regexNodes)
        throws RemoteException;

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
    public Tree selectTreeItemAndWait(String... nodes) throws RemoteException;;

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
    public boolean existsSubItemInTreeItem(String itemText, String... nodes)
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
    public boolean existsSubItemInTreeItemInView(String viewTitle,
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
    public boolean existsContextMenuOfTreeItem(String contextName,
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
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsContextMenuOfTreeItemInView(String viewTitle,
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
    public boolean existsContextMenusOfTreeItem(String[] contextNames,
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
    public boolean existsContextMenusOfTreeItemInView(String viewTitle,
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
    public boolean isContextMenuOfTreeItemEnabled(String contextName,
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
    public boolean isContextMenuOfTreeItemInViewEnabled(String viewTitle,
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
    public boolean isContextMenusOfTreeItemEnabled(String[] contextNames,
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
    public boolean isContextMenusOfTreeItemInViewEnabled(String viewTitle,
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
    public List<String> getSubItemsInTreeItem(String... nodes)
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
    public List<String> getSubItemsInTreeItemInView(String viewTitle,
        String... nodes) throws RemoteException;

    /***************** get allItems in tree ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @return all the treeItem'name of the bot.tree().
     * @throws RemoteException
     */
    public List<String> getSubtemsInTree() throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @return all the treeItem'name of the
     *         bot.getViewByTitle(viewTitle).bot().tree().
     * @throws RemoteException
     */
    public List<String> getSubItemsInTreeInView(String viewTitle)
        throws RemoteException;

    public void waitUntilIsTreeItemInTreeExisted(String nodeName)
        throws RemoteException;

    public void waitUntilIsTreeItemInTreeExisted(final SWTBotTree tree,
        final String itemText) throws RemoteException;

    public void waitUntilIsSubItemInTreeItemExisted(String itemText,
        String... nodes) throws RemoteException;

    public void waitUntilIsSubItemInTreeItemExisted(SWTBotTreeItem treeItem,
        String nodeName) throws RemoteException;

    public Menu contextMenu(String text) throws RemoteException;

    public Menu contextMenu(String... texts) throws RemoteException;

}
