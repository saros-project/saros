package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

public interface SarosContextMenuWrapper extends ContextMenuWrapper {

    public SarosC saros() throws RemoteException;

    public void grantWriteAccess() throws RemoteException;

    public void restrictToReadOnlyAccess() throws RemoteException;

    public void followThisBuddy() throws RemoteException;

    public void stopFollowingThisBuddy() throws RemoteException;

    public void jumpToPositionOfSelectedBuddy() throws RemoteException;
}
