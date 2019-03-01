package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteBot;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRemoteBotView extends Remote {

  public void waitUntilIsActive() throws RemoteException;

  public void show() throws RemoteException;

  public boolean isActive() throws RemoteException;

  public void close() throws RemoteException;

  public void setFocus() throws RemoteException;

  public IRemoteBot bot() throws RemoteException;

  public IRemoteBotViewMenu menu(String label) throws RemoteException;

  public IRemoteBotViewMenu menu(String label, int index) throws RemoteException;

  public IRemoteBotToolbarButton toolbarButton(String tooltip) throws RemoteException;

  public boolean existsToolbarButton(String tooltip) throws RemoteException;

  public List<String> getToolTipOfAllToolbarbuttons() throws RemoteException;

  public IRemoteBotToolbarButton toolbarButtonWithRegex(String regex) throws RemoteException;

  public IRemoteBotToolbarDropDownButton toolbarDropDownButton(String tooltip)
      throws RemoteException;

  public IRemoteBotToolbarRadioButton toolbarRadioButton(String tooltip) throws RemoteException;

  public IRemoteBotToolbarPushButton toolbarPushButton(String tooltip) throws RemoteException;

  public IRemoteBotToolbarToggleButton toolbarToggleButton(String tooltip) throws RemoteException;

  public String getTitle() throws RemoteException;

  public List<String> getToolTipTextOfToolbarButtons() throws RemoteException;
}
