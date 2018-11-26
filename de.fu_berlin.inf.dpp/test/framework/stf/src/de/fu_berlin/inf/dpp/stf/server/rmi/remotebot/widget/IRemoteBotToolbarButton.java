package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotToolbarButton extends Remote {

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
  public abstract boolean isEnabled() throws RemoteException;

  public abstract boolean isVisible() throws RemoteException;

  public abstract boolean isActive() throws RemoteException;

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
