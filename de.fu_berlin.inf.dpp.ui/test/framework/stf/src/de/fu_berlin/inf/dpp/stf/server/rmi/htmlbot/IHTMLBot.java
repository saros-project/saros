package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotDialog;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View;
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage;

/**
 * This interface is part of the HTML GUI test framework. It provides methods to
 * remotely emulate user input by executing Javascript and to query the
 * currently rendered state.
 */
public interface IHTMLBot extends Remote {
    /**
     * Get a remote representation of a conceptual part of the Saros GUI.
     * 
     * @param view
     *            The part of the Saros GUI on which user input should be
     *            emulated and/or of which the current state should be queried.
     * @throws RemoteException
     */
    IRemoteHTMLView view(View view) throws RemoteException;

    /**
     * Get a remote representation of a HTML dialog.
     * 
     * @param pageClass
     *            the class of the dialog's browser page
     * @return an instance of {@link IRemoteBotDialog}
     * @throws RemoteException
     */
    IRemoteBotDialog getDialogWindow(Class<? extends IBrowserPage> pageClass)
        throws RemoteException;

    /**
     * Returns the currently displayed list of accounts.
     * 
     * @return a list of strings in the form 'user@domain'
     * @throws RemoteException
     */
    List<String> getAccountList() throws RemoteException;

    /**
     * Returns the currently displayed list of contacts.
     * 
     * @return a list of diplayNames of contacts
     * @throws RemoteException
     */
    List<String> getContactList() throws RemoteException;

}
