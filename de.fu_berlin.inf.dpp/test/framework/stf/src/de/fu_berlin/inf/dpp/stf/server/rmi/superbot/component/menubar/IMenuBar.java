package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar;

import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.ISarosMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.IWindowMenu;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMenuBar extends Remote {

  public ISarosMenu saros() throws RemoteException;

  public IWindowMenu window() throws RemoteException;
}
