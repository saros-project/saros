package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProgressViewComponent extends Remote {

    public void openProgressView() throws RemoteException;

    public void activateProgressView() throws RemoteException;

    public boolean existPorgress() throws RemoteException;

    public void removeProgress() throws RemoteException;

    public boolean isProgressViewOpen() throws RemoteException;

    // public void cancelInvitation() throws RemoteException;

    public void removeProcess(int index) throws RemoteException;

    /**
     * For some tests a host need to invite many peers concurrently and some
     * operations should not be performed if the invitation processes aren't
     * finished yet. In this case, you can use this method to guarantee, that
     * host wait so long until all the invitation Processes are finished.
     */
    public void waitUntilNoInvitationProgress() throws RemoteException;
}
