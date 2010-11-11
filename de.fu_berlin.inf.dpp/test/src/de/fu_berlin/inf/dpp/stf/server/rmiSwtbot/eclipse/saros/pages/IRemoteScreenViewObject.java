package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteScreenViewObject extends Remote {

    public void activateRemoteScreenView() throws RemoteException;

    public boolean isRemoteScreenViewOpen() throws RemoteException;

    public void openRemoteScreenView() throws RemoteException;

    public void closeRemoteScreenView() throws RemoteException;

    public void changeModeOfImageSource() throws RemoteException;

    public void stopRunningSession() throws RemoteException;

    public void resume() throws RemoteException;

    public void pause() throws RemoteException;
}
