package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represent an HTML button and makes it controllable via RMI.
 */
public interface IRemoteHTMLButton extends Remote {

    /**
     * Click on the button.
     */
    public void click() throws RemoteException;
}
