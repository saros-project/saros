package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RSViewComponent extends Remote {

    public void activateRemoteScreenView() throws RemoteException;

    public boolean isRemoteScreenViewOpen() throws RemoteException;

    public boolean isRemoteScreenViewActive() throws RemoteException;

    public void openRemoteScreenView() throws RemoteException;

    public void closeRemoteScreenView() throws RemoteException;

    public void clickTBChangeModeOfImageSource() throws RemoteException;

    public void clickTBStopRunningSession() throws RemoteException;

    public void clickTBResume() throws RemoteException;

    public void clickTBPause() throws RemoteException;

    public void waitUntilRemoteScreenViewIsActive() throws RemoteException;
}
