package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.ui.part.EditorPart;
import org.jivesoftware.smack.Roster;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.State;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewComponent;

/**
 * The goal of this class is to gather state and perform actions using
 * {@link Saros}, {@link SarosSessionManager}, {@link DataTransferManager},
 * {@link EditorManager} and {@link XMPPAccountStore} from the inside and
 * provide an RMI interface for getting internal states from the outside.
 */
public interface SarosState extends State {

    /**********************************************
     * 
     * gather state and perform actions using {@link SarosSessionManager}
     * 
     **********************************************/

    /**
     * @return <tt>true</tt> if the local client is a current driver of this
     *         shared project. false otherwise.
     * @throws RemoteException
     */
    public boolean isDriver() throws RemoteException;

    /**
     * @return <tt>true</tt> if the given {@link JID} is a driver in this
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean isDriver(JID jid) throws RemoteException;

    /**
     * 
     * @return <tt>true</tt> if the given {@link JID} is a exclusive driver in
     *         this {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean isExclusiveDriver() throws RemoteException;

    /**
     * @return <tt>true</tt>, if all given jids are drivers of this
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean areDrivers(List<JID> jids) throws RemoteException;

    /**
     * 
     * @param jid
     * @return <tt>true</tt>, if the host specifed by the given jid is the
     *         person that initiated this SarosSession and holds all original
     *         files.
     * @throws RemoteException
     */
    public boolean isHost(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the given {@link JID} is a observer in this
     *         {@link SharedProject}.
     */
    public boolean isObserver(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if all given jids are observers of the project.
     */
    public boolean areObservers(List<JID> jids) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the given {@link JID} is a participant of our
     *         {@link SharedProject}.
     */
    public boolean isParticipant(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if all given jids are participants of the project.
     */
    public boolean areParticipants(List<JID> jids) throws RemoteException;

    /**
     * Test if you are now in a session. <br>
     * You can also use another function
     * {@link SessionViewComponent#isInSession()} , which test the session state
     * with GUI.
     * 
     * <p>
     * <b>Attention:</b> <br>
     * Try to use the {@link SessionViewComponent#isInSession()} and
     * {@link SarosState#isInSession()} together in your junittests.
     * 
     * 
     * @return <tt>true</tt> if {@link SarosSessionManager#getSarosSession()} is
     *         not null.
     * 
     * @throws RemoteException
     * @see SarosSessionManager#getSarosSession()
     */
    public boolean isInSession() throws RemoteException;

    /**********************************************
     * 
     * gather state and perform actions using {@link DataTransferManager}
     * 
     **********************************************/
    /**
     * Returns true if the incoming connection from destJid was In-Band
     * Bytestream (XEP-0047).
     */
    public boolean isIncomingConnectionIBB(JID destJid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the incoming connection from destJid was Jingle
     *         TCP.
     */
    public boolean isIncomingConnectionJingleTCP(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the incoming connection from destJid was Jingle
     *         UDP.
     */
    public boolean isIncomingConnectionJingleUDP(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the incoming connection from destJid was Socks5
     *         Bytestream (XEP-0065).
     */
    public boolean isIncomingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the outgoing connection to destJid was In-Band
     *         Bytestream (XEP-0047).
     */
    public boolean isOutgoingConnectionIBB(JID destJid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the outgoing connection to destJid was Jingle
     *         TCP.
     */
    public boolean isOutgoingConnectionJingleTCP(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>,if the outgoing connection to destJid was Jingle
     *         UDP.
     */
    public boolean isOutgoingConnectionJingleUDP(JID destJid)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the outgoing connection to destJid was Socks5
     *         Bytestream (XEP-0065).
     */
    public boolean isOutgoingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException;

    /**********************************************
     * 
     * gather state and perform actions using {@link Saros}
     * 
     **********************************************/
    /**
     * 
     * @return <tt>true</tt>, if Saros is in the process of connecting
     * @throws RemoteException
     */
    public boolean isConnecting() throws RemoteException;

    /**
     * @return <tt>true</tt>, if Saros is connected to a XMPP server.
     * @throws RemoteException
     */
    public boolean isConnected() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if Saros is in the process of disconnecting
     * @throws RemoteException
     */
    public boolean isDisConnecting() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if Saros is not connected to a XMPP Server
     * @throws RemoteException
     */
    public boolean isDisConnected() throws RemoteException;

    /**
     * @return the {@link ConnectionState}. It can be: NOT_CONNECTED,
     *         CONNECTING, CONNECTED, DISCONNECTING or ERROR
     * @throws RemoteException
     */
    public ConnectionState getXmppConnectionState() throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            the JID of the user, which should be located in your buddy
     *            list, if you already have added the user.
     * @return<tt>true</tt>, if {@link Roster#getEntries()} contains this given
     *                       buddyJID
     * @throws RemoteException
     */
    public boolean hasBuddy(JID buddyJID) throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            the JID of the user, which should be located in your buddy
     *            list, if you already have added the user.
     * @return the nickname of the given user.
     * @throws RemoteException
     */
    public String getBuddyNickName(JID buddyJID) throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            the JID of the user, which should be located in your buddy
     *            list, if you already have added the user.
     * @return <tt>true</tt>, if you've given the given user a nickname.
     * @throws RemoteException
     */
    public boolean hasBuddyNickName(JID buddyJID) throws RemoteException;

    /**
     * @param testFilePath
     *            the full path of a test file, e.g. "test/STF/MyClass.java";
     * @return the content of the test file specified with the given
     *         testFilepath, which should be used by the
     *         {@link EditorPart#setTextinEditorWithoutSave(String, String)} and
     *         {@link EditorPart#setTextInEditorWithSave(String, String)} to set
     *         this content to a editor.
     * @throws RemoteException
     */
    public String getTestFileContents(String testFilePath)
        throws RemoteException;

    /**
     * TODO don't work now
     * 
     * @return the path, in which the screenshot located.
     * @throws RemoteException
     */
    public String getPathToScreenShot() throws RemoteException;

    /**
     * Please don't use this method, because {@link Saros#getMyJID()} return
     * null value.
     * 
     * TODO fix the method {@link Saros#getMyJID()}
     * 
     * @return JID of the local user
     * @throws RemoteException
     */
    // public JID getMyJID() throws RemoteException;

    /**********************************************
     * 
     * gather state and perform actions using {@link EditorManager}
     * 
     **********************************************/
    /**
     * @return <tt>true</tt>, if you are currently following another user.
     * @throws RemoteException
     * @see EditorManager#isFollowing
     */
    public boolean isInFollowMode() throws RemoteException;

    /**
     * 
     * @param baseJID
     *            the baseJID of the user, whom you are currently following
     * @return <tt>true</tt>, if you are currently following the given user.
     * @throws RemoteException
     */
    public boolean isFollowingUser(String baseJID) throws RemoteException;

    /**
     * @return the JID of the followed user or null if currently no user is
     *         followed.
     * @throws RemoteException
     */
    public JID getFollowedUserJID() throws RemoteException;

    /**********************************************
     * 
     * gather state and perform actions using {@link XMPPAccountStore}
     * 
     **********************************************/

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

    /**********************************************
     * 
     * infos about local user
     * 
     **********************************************/

    /**
     * set the JID Of the lcoal user
     * 
     * @param localJID
     * @throws RemoteException
     */
    public void setJID(JID localJID) throws RemoteException;

    /**
     * 
     * @return the JID of the local user
     * @throws RemoteException
     */
    public JID getJID() throws RemoteException;

    /**
     * 
     * @param anotherRemoteJID
     *            the JID of a remote user
     * @return <tt></tt>, if the jid of the local user is same as the given
     *         remote JID
     * @throws RemoteException
     */
    public boolean isSameUser(JID anotherRemoteJID) throws RemoteException;
}
