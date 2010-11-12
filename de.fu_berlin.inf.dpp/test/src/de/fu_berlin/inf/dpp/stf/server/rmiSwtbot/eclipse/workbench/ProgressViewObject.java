package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProgressViewObject extends Remote {

    public void openProgressView() throws RemoteException;

    public void activateProgressView() throws RemoteException;

    public boolean existPorgress() throws RemoteException;

    public void removeProgress() throws RemoteException;

    public boolean isProgressViewOpen() throws RemoteException;

    public void cancelInvitation() throws RemoteException;

    public void cancelInvitation(int index) throws RemoteException;

    public void waitUntilNoInvitationProgress() throws RemoteException;
}
