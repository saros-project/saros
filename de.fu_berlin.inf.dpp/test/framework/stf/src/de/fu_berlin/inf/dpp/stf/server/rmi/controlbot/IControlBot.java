package de.fu_berlin.inf.dpp.stf.server.rmi.controlbot;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.INetworkManipulator;

/**
 * The Saros Control robot allows to change how Saros behaves during execution.
 * It does <b>NOT</b> perform any GUI operations but manipulate Saros via
 * reflection and other hooks. This robot should only be used in very few cases.
 */
public interface IControlBot extends Remote {

    /**
     * Returns an interface to manipulate the Saros network layer
     * 
     * @return
     * @throws RemoteException
     */
    public INetworkManipulator getNetworkManipulator() throws RemoteException;
}
