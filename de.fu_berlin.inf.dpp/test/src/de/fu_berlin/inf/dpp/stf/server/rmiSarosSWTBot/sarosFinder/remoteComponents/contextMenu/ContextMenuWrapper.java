package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ContextMenuWrapper extends Remote {
    public ShareWithC shareWith() throws RemoteException;

    public void open() throws RemoteException;

    public void openWith(String editorType) throws RemoteException;

    public void delete() throws RemoteException;

    public TeamC team() throws RemoteException;

    public NewC newC() throws RemoteException;

    public RefactorC refactor() throws RemoteException;

    public void copy() throws RemoteException;

    public void paste(String target) throws RemoteException;

    public boolean existsWithRegex(String name) throws RemoteException;

    public boolean exists(String name) throws RemoteException;

    /**
     * Delete all the projects existed in the package explorer view.
     * 
     * @throws RemoteException
     */
    public void deleteAllProjects() throws RemoteException;

    // public void deleteAllItems() throws RemoteException;

}