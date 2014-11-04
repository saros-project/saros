package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IActivityLog extends Remote {

    /**
     * Returns the title of the activity log tab (as displayed on the activity
     * log tab).
     * 
     * @return the title of the activity log tab
     * @throws RemoteException
     */
    public String getTitle() throws RemoteException;

    /**
     * Returns an array containing all lines of the activity log tab.
     * 
     * @return an array containing all lines of the activity log tab.
     * @throws RemoteException
     * */
    public String[] getLines() throws RemoteException;
}
