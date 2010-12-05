package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.MainMenuComponent;

public interface SarosMainMenuComponent extends MainMenuComponent {

    public void creatAccountGUI(JID jid, String password) throws RemoteException;

    /**
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt> if the account specified by the given jid exists in
     *         preference store
     * @throws RemoteException
     * @see XMPPAccountStore#getAllAccounts()
     */
    public boolean isAccountExist(JID jid, String password)
        throws RemoteException;

    /**
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt> if the acount specified by the given jid is active
     * @throws RemoteException
     * @see XMPPAccount#isActive()
     */
    public boolean isAccountActive(JID jid) throws RemoteException;

    /**
     * activate the account specified by the given jid
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     * @see XMPPAccountStore#setAccountActive(XMPPAccount)
     */
    public void activateAccount(JID jid) throws RemoteException;

    /**
     * Creates an account.
     * 
     * @param username
     *            the username of the new account.
     * @param password
     *            the password of the new account.
     * @param server
     *            the server of the new account.
     * 
     * @throws RemoteException
     */
    public void createAccount(String username, String password, String server)
        throws RemoteException;

    /**
     * 
     * change the account specified by the given jid
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @param newUserName
     *            the new username
     * @param newPassword
     *            the new password
     * @param newServer
     *            the new server
     * @throws RemoteException
     */
    public void changeAccount(JID jid, String newUserName, String newPassword,
        String newServer) throws RemoteException;

    /**
     * delete the account specified by the given jid
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void deleteAccount(JID jid) throws RemoteException;
}
