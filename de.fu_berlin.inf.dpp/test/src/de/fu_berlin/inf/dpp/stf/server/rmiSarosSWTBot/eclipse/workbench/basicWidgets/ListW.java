package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;

public interface ListW extends EclipseComponent {

    public String[] getListItemsInGroup(String inGroup) throws RemoteException;

    public void selectListItemInGroup(String item, String inGroup)
        throws RemoteException;
}
