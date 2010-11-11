package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IProgressViewObject extends Remote {

    public void openProgressView() throws RemoteException;

    public void activateProgressView() throws RemoteException;

    public boolean existPorgress() throws RemoteException;
}
