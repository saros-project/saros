package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

public interface IRemoteBotTree extends Remote {

  /**
   * ********************************************
   *
   * <p>finders
   *
   * <p>********************************************
   */
  public void clickContextMenu(String... texts) throws RemoteException;

  /**
   * Collapses the node matching the node information.
   *
   * @param nodeText the text on the node.
   * @return the Tree item that was expanded.
   * @throws WidgetNotFoundException if the node is not found.
   */
  public IRemoteBotTreeItem collapseNode(String nodeText) throws RemoteException;

  /**
   * Expands the nodes as if the plus sign was clicked.
   *
   * @param nodeText the node to be expanded.
   * @param recursive if the expansion should be recursive.
   * @return the tree item that was expanded.
   * @throws WidgetNotFoundException if the node is not found.
   */
  public IRemoteBotTreeItem expandNode(String nodeText, boolean recursive) throws RemoteException;

  /**
   * Attempts to expand all nodes along the path specified by the node array parameter.
   *
   * @param nodes node path to expand
   * @return the last Tree item that was expanded.
   * @throws WidgetNotFoundException if any of the nodes on the path do not exist
   */
  public IRemoteBotTreeItem expandNode(String... nodes) throws RemoteException;

  /**
   * Select the indexes provided.
   *
   * @param indices the indices to select.
   * @return this same instance.
   */
  public IRemoteBotTree select(int... indices) throws RemoteException;

  /**
   * Selects the items matching the array list.
   *
   * @param items the items to select.
   * @return this same instance.
   */
  public IRemoteBotTree select(String... items) throws RemoteException;

  /**
   * Unselects the selection in the tree.
   *
   * @return this same instance.
   */
  public IRemoteBotTree unselect() throws RemoteException;

  public IRemoteBotTreeItem selectTreeItem(String... nodes) throws RemoteException;

  /**
   * Selects the treeItem matching the given Regexnodes in the tree: bot.tree(). This method is
   * suitable for shell widget.
   *
   * @param regexNodes node path to expand. Attempts to expand all nodes along the path specified by
   *     the regex array parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
   * @throws WidgetNotFoundException If the item wasn't found.
   */
  public IRemoteBotTreeItem selectTreeItemWithRegex(String... regexNodes) throws RemoteException;

  /**
   * Selects the treeItem matching the given nodes in the tree: bot.tree() with waiting until the
   * parentTreeItem is expanded. This method is suitable for shell widget.
   *
   * @param nodes node path to expand. Attempts to expand all nodes along the path specified by the
   *     node array parameter.
   * @throws RemoteException
   */
  public IRemoteBotTreeItem selectTreeItemAndWait(String... nodes) throws RemoteException;

  /**
   * Gets if this tree has items within it.
   *
   * @return <code>true</code> if the tree has any items, <code>false</code> otherwise.
   * @since 1.0
   */
  public boolean hasItems() throws RemoteException;

  /**
   * Gets the number of rows in the tree.
   *
   * @return the number of rows in the tree
   */
  public int rowCount() throws RemoteException;

  /**
   * Gets the current selection count.
   *
   * @return the number of selected items.
   */
  public int selectionCount() throws RemoteException;

  /**
   * Gets the column count of this tree.
   *
   * @return the number of columns in the tree
   */
  public int columnCount() throws RemoteException;

  /**
   * Gets the columns of this tree.
   *
   * @return the list of columns in the tree.
   */
  public List<String> columns() throws RemoteException;

  /**
   * This method is suitable for shell widget.
   *
   * @param itemText name of the tree item.
   * @return<tt>true</tt>, if the treeItem in the bot.tree() exists.
   * @throws RemoteException
   */
  public boolean existsSubItem(String itemText) throws RemoteException;

  public boolean existsSubItemWithRegex(String regex) throws RemoteException;

  /**
   * This method is suitable for shell widget.
   *
   * @return all the treeItem'name of the bot.tree().
   * @throws RemoteException
   */
  public List<String> getTextOfItems() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>wait until
   *
   * <p>********************************************
   */
  public void waitUntilItemExists(String nodeName) throws RemoteException;

  public void waitUntilItemNotExists(final String itemText) throws RemoteException;
}
