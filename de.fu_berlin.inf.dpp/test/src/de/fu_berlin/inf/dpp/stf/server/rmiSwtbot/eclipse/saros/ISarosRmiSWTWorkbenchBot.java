package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.IRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.PopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewObject;

/**
 * This is the RMI interface for remoting Saros Eclipse Plugin. Use this from
 * {@link Musician} to write tests.
 */
public interface ISarosRmiSWTWorkbenchBot extends IRmiSWTWorkbenchBot {

    // public void openSarosViews() throws RemoteException;

    // public void resetSaros() throws RemoteException;

    public RosterViewObject getRosterViewObject() throws RemoteException;

    public PopUpWindowObject getPopupWindowObject() throws RemoteException;

    public SessionViewObject getSessionViewObject() throws RemoteException;
}
