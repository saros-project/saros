package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBotList extends EclipseComponent {

    public String[] getListItemsInGroup(String inGroup) throws RemoteException;

    public void selectListItemInGroup(String item, String inGroup)
        throws RemoteException;
}
