package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotRadio extends Remote {

  /**
   * ********************************************
   *
   * <p>finders
   *
   * <p>********************************************
   */
  public IRemoteBotMenu contextMenu(String text) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>actions
   *
   * <p>********************************************
   */
  public void click() throws RemoteException;

  public void clickAndWait() throws RemoteException;

  public void setFocus() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public boolean isEnabled() throws RemoteException;

  public boolean isVisible() throws RemoteException;

  public boolean isActive() throws RemoteException;

  public String getText() throws RemoteException;

  public String getToolTipText() throws RemoteException;

  public boolean isSelected() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>waits until
   *
   * <p>********************************************
   */
  public void waitUntilIsEnabled() throws RemoteException;
}
