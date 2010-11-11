package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.IEclipseState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseWindowObject;

/**
 * This Interface is the stub for remote {@link SWTWorkbenchBot}. The
 * implementation is called {@link RmiSWTWorkbenchBot} and is implemented using
 * delegation.
 */
public interface IRmiSWTWorkbenchBot extends Remote {

    public SWTBotShell getEclipseShell() throws RemoteException;

    public void resetWorkbench() throws RemoteException;

    public void activateEclipseShell() throws RemoteException;

    public IEclipseWindowObject getEclipseWindowObject() throws RemoteException;

    public IEclipseState getEclipseState() throws RemoteException;

    public IEclipseEditorObject getEclipseEditorObject() throws RemoteException;
}
