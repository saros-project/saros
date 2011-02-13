package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponent;

public interface STFTree extends EclipseComponent {

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
    public STFTreeItem selectTreeItem(String... nodes) throws RemoteException;

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
    public STFTreeItem selectTreeItemWithRegex(String... regexNodes)
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
    public STFTreeItem selectTreeItemAndWait(String... nodes)
        throws RemoteException;;

    /***************** exist tree item with regexs ****************** */

    /***************** exists tree item ****************** */
    /**
     * This method is suitable for shell widget.
     * 
     * @param itemText
     *            name of the tree item.
     * @return<tt>true</tt>, if the treeItem in the bot.tree() exists.
     * @throws RemoteException
     */
    public boolean existsSubItem(String itemText) throws RemoteException;

    public boolean existsSubItemWithRegexs(String regex) throws RemoteException;

    /***************** is context of tree item enabled****************** */

    /***************** get allItems in treeNode ****************** */

    /***************** get allItems in tree ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @return all the treeItem'name of the bot.tree().
     * @throws RemoteException
     */
    public List<String> getSubtems() throws RemoteException;

    public void waitUntilSubItemExists(String nodeName) throws RemoteException;

}
