package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import saros.stf.server.rmi.remotebot.widget.impl.RemoteBotMenu;

public interface IRemoteBotMenu extends Remote {

  public void click() throws RemoteException;

  public RemoteBotMenu contextMenu(String text) throws RemoteException;

  public void waitUntilIsEnabled() throws RemoteException;

  public String getToolTipText() throws RemoteException;

  public String getText() throws RemoteException;

  public boolean isChecked() throws RemoteException;

  public boolean isActive() throws RemoteException;

  public boolean isVisible() throws RemoteException;

  public boolean isEnabled() throws RemoteException;

  public void setFocus() throws RemoteException;

  public IRemoteBotMenu menu(String menuName) throws RemoteException;
}
