package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IInternal extends Remote {

    /**
     * Changes the current Saros version to the given version
     * 
     * @param version
     *            the version that Saros will be set to e.g
     *            <code>2.6.2.11</code>
     * @throws RemoteException
     */
    public void changeSarosVersion(String version) throws RemoteException;

    /**
     * Resets the current Saros version to its default state as the plugin was
     * started
     * 
     * @throws RemoteException
     */
    public void resetSarosVersion() throws RemoteException;

}
