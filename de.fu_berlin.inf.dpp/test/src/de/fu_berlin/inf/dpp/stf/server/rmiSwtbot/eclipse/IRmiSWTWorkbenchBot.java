package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.IEclipseState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.IEclipseEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.IEclipseWindowObject;

/**
 * This Interface is the stub for remote {@link SWTWorkbenchBot}. The
 * implementation is called {@link RmiSWTWorkbenchBot} and is implemented using
 * delegation.
 */
public interface IRmiSWTWorkbenchBot extends Remote {

    public IEclipseWindowObject getEclipseWindowObject() throws RemoteException;

    public IEclipseState getEclipseState() throws RemoteException;

    public IEclipseEditorObject getEclipseEditorObject() throws RemoteException;
}
