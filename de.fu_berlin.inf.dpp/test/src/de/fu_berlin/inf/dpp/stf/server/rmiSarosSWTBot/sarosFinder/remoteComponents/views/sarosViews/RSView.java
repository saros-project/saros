package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponent;

public interface RSView extends SarosComponent {

    public void clickTBChangeModeOfImageSource() throws RemoteException;

    public void clickTBStopRunningSession() throws RemoteException;

    public void clickTBResume() throws RemoteException;

    public void clickTBPause() throws RemoteException;

    public void waitUntilRemoteScreenViewIsActive() throws RemoteException;
}
