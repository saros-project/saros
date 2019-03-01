package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRemoteBotTreeItem extends Remote {
  /**
   * ********************************************
   *
   * <p>finder
   *
   * <p>********************************************
   */
  public void clickContextMenu(String... texts) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>actions
   *
   * <p>********************************************
   */
  public void toggleCheck() throws RemoteException;

  public void uncheck() throws RemoteException;

  public IRemoteBotTreeItem select(String... items) throws RemoteException;

  public IRemoteBotTreeItem select() throws RemoteException;

  public IRemoteBotTreeItem doubleClick() throws RemoteException;

  public IRemoteBotTreeItem expand() throws RemoteException;

  public IRemoteBotTreeItem expandNode(String... nodes) throws RemoteException;

  public void check() throws RemoteException;

  public IRemoteBotTreeItem collapse() throws RemoteException;

  public IRemoteBotTreeItem collapseNode(String nodeText) throws RemoteException;

  public IRemoteBotTreeItem select(String item) throws RemoteException;

  public void click() throws RemoteException;

  public void setFocus() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public boolean isSelected() throws RemoteException;

  public boolean isChecked() throws RemoteException;

  public boolean isExpanded() throws RemoteException;

  public int rowCount() throws RemoteException;

  public IRemoteBotTreeItem getNode(int row) throws RemoteException;

  public IRemoteBotTreeItem getNode(String nodeText) throws RemoteException;

  public IRemoteBotTreeItem getNode(String nodeText, int index) throws RemoteException;

  public IRemoteBotTreeItem getNodeWithRegex(String regex) throws RemoteException;

  public List<String> getNodes() throws RemoteException;

  public List<IRemoteBotTreeItem> getNodes(String nodeText) throws RemoteException;

  public List<String> getTextOfItems() throws RemoteException;

  // public STFBotTreeItem[] getItems() throws RemoteException;

  public boolean existsSubItem(String text) throws RemoteException;

  public boolean existsSubItemWithRegex(String regex) throws RemoteException;

  public boolean isContextMenuEnabled(String... contextNames) throws RemoteException;

  public boolean existsContextMenu(String... contextNames) throws RemoteException;

  public boolean isEnabled() throws RemoteException;

  public boolean isVisible() throws RemoteException;

  public boolean isActive() throws RemoteException;

  public String getText() throws RemoteException;

  public String getToolTipText() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>wait until
   *
   * <p>********************************************
   */
  public void waitUntilSubItemExists(final String subItemText) throws RemoteException;

  public void waitUntilContextMenuExists(final String... contextNames) throws RemoteException;
}
