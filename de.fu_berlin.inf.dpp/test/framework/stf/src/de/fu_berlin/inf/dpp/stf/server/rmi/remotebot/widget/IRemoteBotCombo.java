package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotCombo extends Remote {

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
  public void typeText(String text) throws RemoteException;

  public void typeText(String text, int interval) throws RemoteException;

  public void setFocus() throws RemoteException;

  public void setText(String text) throws RemoteException;

  public void setSelection(String text) throws RemoteException;

  public void setSelection(int index) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public int itemCount() throws RemoteException;

  public String[] items() throws RemoteException;

  public String selection() throws RemoteException;

  public int selectionIndex() throws RemoteException;

  public boolean isEnabled() throws RemoteException;

  public boolean isVisible() throws RemoteException;

  public boolean isActive() throws RemoteException;

  public String getText() throws RemoteException;

  public String getToolTipText() throws RemoteException;
}
