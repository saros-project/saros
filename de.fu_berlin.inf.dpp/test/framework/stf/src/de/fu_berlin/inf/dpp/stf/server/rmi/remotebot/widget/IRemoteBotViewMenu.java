package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotViewMenu extends Remote {

  /**
   * ********************************************
   *
   * <p>actions
   *
   * <p>********************************************
   */
  public void click() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public String getToolTipText() throws RemoteException;

  public String getText() throws RemoteException;

  public boolean isChecked() throws RemoteException;
}
