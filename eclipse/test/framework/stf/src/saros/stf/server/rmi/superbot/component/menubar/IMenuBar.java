package saros.stf.server.rmi.superbot.component.menubar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import saros.stf.server.rmi.superbot.component.menubar.menu.ISarosMenu;
import saros.stf.server.rmi.superbot.component.menubar.menu.IWindowMenu;

public interface IMenuBar extends Remote {

  public ISarosMenu saros() throws RemoteException;

  public IWindowMenu window() throws RemoteException;
}
