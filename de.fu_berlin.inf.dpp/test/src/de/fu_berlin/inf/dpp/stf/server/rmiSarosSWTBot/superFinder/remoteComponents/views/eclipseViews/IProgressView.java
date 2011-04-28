package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.eclipseViews;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IProgressView extends Remote {

    public boolean existsPorgress() throws RemoteException;

    /**
     * remove the progress. ie. Click the gray clubs delete icon.
     * 
     * @throws RemoteException
     */
    public void removeProgress() throws RemoteException;

    public void removeProcess(int index) throws RemoteException;

    /**
     * For some tests a host need to invite many peers concurrently and some
     * operations should not be performed if the invitation processes aren't
     * finished yet. In this case, you can use this method to guarantee, that
     * host wait so long until all the invitation Processes are finished.
     */
    public void waitUntilNotExistsProgress() throws RemoteException;
}
