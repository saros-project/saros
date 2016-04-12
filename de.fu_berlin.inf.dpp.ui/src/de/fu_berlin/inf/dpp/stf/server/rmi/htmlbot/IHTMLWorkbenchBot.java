package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is part of the GUI test framework and contains methods
 * concerning the opening of the Saros HTML view.
 */
public interface IHTMLWorkbenchBot extends Remote {

    /**
     * Opens the HTML view of Saros inside the IDE.
     * 
     * @throws RemoteException
     */
    void openSarosBrowserView() throws RemoteException;

    /**
     * Tests if the HTML view is already open.
     * 
     * @return true if it is open, false otherwise
     * @throws RemoteException
     */
    boolean isSarosBrowserViewOpen() throws RemoteException;
}
