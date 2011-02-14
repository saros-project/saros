package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBotPerspective extends EclipseComponent {

    public void activate() throws RemoteException;

    public String getLabel() throws RemoteException;

    public boolean isActive() throws RemoteException;
}
