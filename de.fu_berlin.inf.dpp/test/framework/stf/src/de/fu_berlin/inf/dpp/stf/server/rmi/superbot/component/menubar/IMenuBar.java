package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.ISarosMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.IWindowMenu;

public interface IMenuBar extends Remote {

    public ISarosMenu saros() throws RemoteException;

    public IWindowMenu window() throws RemoteException;

}