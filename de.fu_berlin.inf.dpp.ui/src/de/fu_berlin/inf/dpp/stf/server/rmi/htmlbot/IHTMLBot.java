package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotDialog;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLButton;
import de.fu_berlin.inf.dpp.ui.view_parts.BrowserPage;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This interface is part of the HTML GUI test framework.
 * It provides methods to remotely emulate user input by
 * executing Javascript and to query the currently rendered state.
 */
public interface IHTMLBot extends Remote {

    /**
     * Get a remote representation of a HTML dialog.
     *
     * @param pageClass the class of the dialog's browser page
     * @return an instance of {@link IRemoteBotDialog}
     * @throws RemoteException
     */
    IRemoteBotDialog getDialogWindow(Class<? extends BrowserPage> pageClass) throws RemoteException;

    /**
     * Gets a remote representation of the HTML button with the given ID.
     *
     * @param id the value of the ID attribute of the button
     *
     * @return an instance of {@link IRemoteHTMLButton}
     * @throws RemoteException
     */
    IRemoteHTMLButton buttonWithId(String id) throws RemoteException;

    /**
     * Returns the currently displayed list of accounts.
     *
     * @return a list of strings in the form 'user@domain'
     * @throws RemoteException
     */
    List<String> getAccountList() throws RemoteException;
}
