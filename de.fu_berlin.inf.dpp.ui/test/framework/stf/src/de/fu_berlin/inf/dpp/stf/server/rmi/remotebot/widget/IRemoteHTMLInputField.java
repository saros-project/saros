package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represent an HTML input field and makes it controllable via RMI.
 */
public interface IRemoteHTMLInputField extends Remote {

    /**
     * enter text to the field.
     */
    public void enter(String text) throws RemoteException;
}
