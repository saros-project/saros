package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.MainMenuComponent;

public interface SarosMainMenuComponent extends MainMenuComponent {

    public void creatAccountWithMenuGUI(JID jid, String password,
        boolean usesThisAccountNow) throws RemoteException;

    /**
     * 
     * @param server
     * @param username
     * @param password
     * @throws RemoteException
     */
    public void createAccountInPeferencesGUI(String server, String username,
        String password) throws RemoteException;

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
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt> if the account specified by the given parameters
     *         exists in preference store
     * @throws RemoteException
     */
    public boolean isAccountExistGUI(JID jid, String password)
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
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt> if the acount specified by the given jid is active
     * @throws RemoteException
     */
    public boolean isAccountActiveGUI(JID jid) throws RemoteException;

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
     * acti.vate the account specified by the given jid
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @param password
     * @throws RemoteException
     */
    public void activateAccountGUI(JID jid, String password)
        throws RemoteException;

    /**
     * Creates an account.
     * 
     * @param server
     *            the server of the new account.
     * @param username
     *            the username of the new account.
     * @param password
     *            the password of the new account.
     * 
     * 
     * @throws RemoteException
     */
    public void createAccount(String server, String username, String password)
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
    public void changeAccountGUI(JID jid, String newUserName,
        String newPassword, String newServer) throws RemoteException;

    /**
     * delete the account specified by the given jid
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void deleteAccount(JID jid) throws RemoteException;

    public void addAccountGUI(JID jid, String password) throws RemoteException;

    public void deleteAccountGUI(JID jid, String password)
        throws RemoteException;

    /**********************************************
     * 
     * setting for screensharing
     * 
     **********************************************/
    /**
     * 
     * @throws RemoteException
     */
    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException;

    /**********************************************
     * 
     * setting for Feedback
     * 
     **********************************************/
    /**
     * Set feeback disabled without GUI.<br/>
     * To simplify Testing you can disable the automatic reminder, so that you
     * will never get the feedback popup window.
     * 
     * @see FeedbackManager#setFeedbackDisabled(boolean)
     * 
     * @throws RemoteException
     */
    public void disableAutomaticReminder() throws RemoteException;

    /**
     * Set feeback disabled using GUI.<br/>
     * To simplify Testing you can disable the automatic reminder, so that you
     * will never get the feedback popup window.
     * <ol>
     * <li>open Preferences dialog by clicking main menu Saros -> preferences</li>
     * <li>click the treenodes: Saros-> Feedback</li>
     * <li>then select the radio button "Disable automatic reminder" in the
     * right page</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void disableAutomaticReminderGUI() throws RemoteException;
}
