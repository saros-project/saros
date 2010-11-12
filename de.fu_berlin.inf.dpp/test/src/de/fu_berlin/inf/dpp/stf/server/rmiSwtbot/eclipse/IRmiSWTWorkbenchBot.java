package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObject;

/**
 * This Interface is the stub for remote {@link SWTWorkbenchBot}. The
 * implementation is called {@link RmiSWTWorkbenchBot} and is implemented using
 * delegation.
 */
public interface IRmiSWTWorkbenchBot extends Remote {

    public SarosPopUpWindowObject getPopUpWindowObject() throws RemoteException;

    public EclipseEditorObject getEclipseEditorObject() throws RemoteException;
}
