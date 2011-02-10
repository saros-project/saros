package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.SarosComponent;

public interface RSView extends SarosComponent {

    public boolean isRemoteScreenViewOpen() throws RemoteException;

    public boolean isRemoteScreenViewActive() throws RemoteException;

    public void clickTBChangeModeOfImageSource() throws RemoteException;

    public void clickTBStopRunningSession() throws RemoteException;

    public void clickTBResume() throws RemoteException;

    public void clickTBPause() throws RemoteException;

    public void waitUntilRemoteScreenViewIsActive() throws RemoteException;
}
