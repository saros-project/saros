package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteBot;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IRemoteBotShell extends Remote {

  /**
   * ********************************************
   *
   * <p>finders
   *
   * <p>********************************************
   */
  public IRemoteBot bot() throws RemoteException;

  public IRemoteBotMenu contextMenu(String text) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>actions
   *
   * <p>********************************************
   */
  public void setFocus() throws RemoteException;

  public boolean activate() throws RemoteException;

  public void close() throws RemoteException;

  public void confirm() throws RemoteException;

  /**
   * confirm a pop-up window.
   *
   * @param buttonText text of the button in the shell.
   */
  public void confirm(String buttonText) throws RemoteException;

  /**
   * confirm a pop-up window with a tree. You should first select a tree node and then confirm with
   * button.
   *
   * @param buttonText text of the button
   * @param nodes node path to expand. Attempts to expand all nodes along the path specified by the
   *     node array parameter.
   */
  public void confirmWithTree(String buttonText, String... nodes) throws RemoteException;

  public void confirmWithTextField(String textLabel, String text, String buttonText)
      throws RemoteException;

  public void confirmWithTextFieldAndWait(Map<String, String> labelsAndTexts, String buttonText)
      throws RemoteException;

  public void confirmWithTreeWithWaitingExpand(String buttonText, String... nodes)
      throws RemoteException;

  /**
   * confirm a pop-up window with a checkbox.
   *
   * @param buttonText text of the button
   * @param isChecked if the checkbox selected or not.
   * @throws RemoteException
   */
  public void confirmWithCheckBox(String buttonText, boolean isChecked) throws RemoteException;

  /**
   * confirm a pop-up window with more than one checkbox.
   *
   * @param buttonText text of the button
   * @param itemNames the labels of the checkboxs, which you want to select.
   */
  public void confirmWithCheckBoxs(String buttonText, String... itemNames) throws RemoteException;

  /**
   * confirm a pop-up window with a table. You should first select a table item and then confirm
   * with button.
   *
   * @param buttonText text of the button
   * @param itemName the name of the table item, which you want to select.
   * @throws RemoteException
   */
  public void confirmWithTable(String itemName, String buttonText) throws RemoteException;

  /**
   * confirm a pop-up window with a tree using filter text. You should first input a filter text in
   * the text field and then select a tree node, confirm with button.
   *
   * @param buttonText text of the button
   * @param teeNode tree node, which you want to select.
   * @param rootOfTreeNode root of the tree node.
   * @throws RemoteException
   */
  public void confirmWithTreeWithFilterText(
      String rootOfTreeNode, String teeNode, String buttonText) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public boolean isActive() throws RemoteException;

  public boolean isEnabled() throws RemoteException;

  public boolean isVisible() throws RemoteException;

  public String getText() throws RemoteException;

  public String getToolTipText() throws RemoteException;

  public String getErrorMessage() throws RemoteException;

  public String getMessage() throws RemoteException;

  /**
   * @param tableItemName the name of the tableItem.
   * @return <tt>true</tt>, if the given tableItem is existed in the shell.
   * @throws RemoteException
   */
  public boolean existsTableItem(String tableItemName) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>wait until
   *
   * <p>********************************************
   */
  /**
   * waits until the given STFBotShell is active.
   *
   * @throws RemoteException
   */
  public void waitUntilActive() throws RemoteException;

  public void waitShortUntilIsClosed() throws RemoteException;

  /**
   * waits until the given STFBotShell is closed.
   *
   * @throws RemoteException
   */
  public void waitLongUntilIsClosed() throws RemoteException;
}
