package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public interface ExWorkbenchObject extends Remote {

    public void openSarosViews() throws RemoteException;

    public void resetSaros() throws RemoteException;

    public SWTBotShell getEclipseShell() throws RemoteException;

    public void resetWorkbench() throws RemoteException;

    public void activateEclipseShell() throws RemoteException;
}
