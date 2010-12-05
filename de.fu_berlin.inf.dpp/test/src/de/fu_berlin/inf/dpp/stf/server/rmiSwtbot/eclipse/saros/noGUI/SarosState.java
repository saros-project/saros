package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
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
     * @return <tt>true</tt>, if the local user is a host.
     * @throws RemoteException
     */
    public boolean isHost() throws RemoteException;

    /**
     * 
     * @param jid
     *            the JID of the user.
     * @return <tt>true</tt>, if the user specified by the given jid is a host.
     * @throws RemoteException
     */
    public boolean isHost(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the local user is a observer in this
     *         {@link SharedProject}.
     */
    public boolean isObserver() throws RemoteException;

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
     * 
     * @return <tt>true</tt>, if the local user is a participant of the
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean isParticipant() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the given {@link JID} is a participant of the
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
