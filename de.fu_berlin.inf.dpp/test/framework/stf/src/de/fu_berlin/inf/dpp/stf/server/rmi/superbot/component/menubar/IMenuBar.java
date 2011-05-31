package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.ISarosM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.IWindowM;

public interface IMenuBar extends Remote {

    public ISarosM saros() throws RemoteException;

    public IWindowM window() throws RemoteException;

}