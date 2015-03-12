package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represents dialogs of the HTML version of Saros.
 * It is used to offer their functionality via RMI.
 */
public interface IRemoteBotDialog extends Remote {

    /**
     * Fills the HTML input field with the given ID
     *
     * @param id    the ID of the input field to be filled
     * @param value the value to fill in
     * @throws RemoteException
     */
    void fillInputField(String id, String value) throws RemoteException;

    /**
     * Submits the dialog by clicking Okay.
     *
     * @throws RemoteException
     */
    void submit() throws RemoteException;

    /**
     * Cancels the dialog.
     *
     * @throws RemoteException
     */
    void cancel() throws RemoteException;
}
