package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotList extends Remote {

  public IRemoteBotMenu contextMenu(String text) throws RemoteException;

  public String itemAt(int index) throws RemoteException;

  public int itemCount() throws RemoteException;

  public int indexOf(String item) throws RemoteException;

  public void select(String item) throws RemoteException;

  public void select(int... indices) throws RemoteException;

  public void select(int index) throws RemoteException;

  public void select(String... items) throws RemoteException;

  /**
   * Gets the arrray of selected items.
   *
   * @return the selected items in the list.
   * @throws RemoteException
   */
  public String[] selection() throws RemoteException;

  public int selectionCount() throws RemoteException;

  public void unselect() throws RemoteException;

  public void setFocus() throws RemoteException;

  public String[] getItems() throws RemoteException;

  public boolean isEnabled() throws RemoteException;

  public boolean isVisible() throws RemoteException;

  public boolean isActive() throws RemoteException;

  public String getText() throws RemoteException;

  public String getToolTipText() throws RemoteException;
}
