package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotTableItem extends Remote {

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public abstract IRemoteBotMenu contextMenu(String text) throws RemoteException;

  public abstract void select() throws RemoteException;

  public abstract void check() throws RemoteException;

  public abstract void uncheck() throws RemoteException;

  public abstract void toggleCheck() throws RemoteException;

  public abstract void click() throws RemoteException;

  public abstract void clickAndWait() throws RemoteException;

  public abstract void setFocus() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public abstract boolean existsContextMenu(String contextName) throws RemoteException;

  public abstract boolean isEnabled() throws RemoteException;

  public abstract boolean isVisible() throws RemoteException;

  public abstract boolean isActive() throws RemoteException;

  public abstract boolean isChecked() throws RemoteException;

  public abstract boolean isGrayed() throws RemoteException;

  public abstract String getText(int index) throws RemoteException;

  public abstract String getText() throws RemoteException;

  public abstract String getToolTipText() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>waits until
   *
   * <p>********************************************
   */
  public abstract void waitUntilIsEnabled() throws RemoteException;
}
