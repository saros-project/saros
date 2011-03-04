package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

public interface STFBotTree extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/

    public STFBotTreeItem[] getAllItems() throws RemoteException;

    // public STFBotMenu contextMenu(String text) throws RemoteException;

    public STFBotMenu contextMenu(String... texts) throws RemoteException;

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public STFBotTreeItem collapseNode(String nodeText) throws RemoteException;

    public STFBotTreeItem expandNode(String nodeText, boolean recursive)
        throws RemoteException;

    public STFBotTreeItem expandNode(String... nodes) throws RemoteException;

    public STFBotTree select(int... indices) throws RemoteException;

    public STFBotTree select(String... items) throws RemoteException;

    public STFBotTree unselect() throws RemoteException;

    public STFBotTreeItem selectTreeItem(String... nodes)
        throws RemoteException;

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
    public STFBotTreeItem selectTreeItemWithRegex(String... regexNodes)
        throws RemoteException;

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
    public STFBotTreeItem selectTreeItemAndWait(String... nodes)
        throws RemoteException;

    public boolean hasItems() throws RemoteException;

    public int rowCount() throws RemoteException;

    public int selectionCount() throws RemoteException;

    public int columnCount() throws RemoteException;

    public List<String> columns() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
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

    /**
     * This method is suitable for shell widget.
     * 
     * @return all the treeItem'name of the bot.tree().
     * @throws RemoteException
     */
    public List<String> getTextOfItems() throws RemoteException;

    /**********************************************
     * 
     * wait until
     * 
     **********************************************/

    public void waitUntilItemExists(String nodeName) throws RemoteException;

    public void waitUntilItemNotExists(final String itemText)
        throws RemoteException;

}
