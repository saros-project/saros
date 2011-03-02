package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface ContextMenuWrapper extends EclipseComponent {

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

}