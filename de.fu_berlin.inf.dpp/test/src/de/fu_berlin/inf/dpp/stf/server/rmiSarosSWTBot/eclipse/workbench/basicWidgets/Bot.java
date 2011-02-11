package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;

public interface Bot extends EclipseComponent {

    public Tree tree() throws RemoteException;
}
