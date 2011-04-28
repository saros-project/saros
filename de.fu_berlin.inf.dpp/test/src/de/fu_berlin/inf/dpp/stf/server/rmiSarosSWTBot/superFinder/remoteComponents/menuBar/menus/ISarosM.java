package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.submenus.ISarosPreferences;

public interface ISarosM extends Remote {

    public ISarosPreferences preferences() throws RemoteException;

    /**
     * Creates an account with GUI, which should be done with the following
     * steps:
     * <ol>
     * <li>Click menu "Saros" -> "Create Account"</li>
     * <li>confirm the popup window "Create New User Account" with the given
     * parameters</li>
     * </ol>
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @param password
     *            the password of the new account.
     * 
     *            TODO not implement yet.
     * @throws RemoteException
     */
    public void creatAccount(JID jid, String password) throws RemoteException;

    public void addBuddy(JID jid) throws RemoteException;

    public void addBuddies(String... jidOfInvitees) throws RemoteException;

    public void shareProjects(String projectName, JID... jids)
        throws RemoteException;

    public void addProjects(String... projectNames) throws RemoteException;

    public void stopSession() throws RemoteException;

}
