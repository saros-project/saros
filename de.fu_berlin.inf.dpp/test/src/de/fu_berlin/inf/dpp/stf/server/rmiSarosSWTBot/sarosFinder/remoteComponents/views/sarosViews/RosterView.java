package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.SarosM;

/**
 * This interface contains convenience API to perform a action using widgets in
 * the roster view. then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * after then you can use the object rosterV initialized in {@link Tester} to
 * access the API :), e.g.
 * 
 * <pre>
 * alice.rosterV.openRosterView();
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface RosterView extends SarosComponent {

    /**********************************************
     * 
     * toolbar buttons on the view: connect/disconnect
     * 
     **********************************************/
    /**
     * performs the action "connect" which should be activated by clicking the
     * tool bar button with the tooltip text "Connect" on the roster view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the roster view is open and active.</li>
     * <li>if the test account specified by the given jid doesn't exists, create
     * it using {@link SarosM#createAccountNoGUI(String, String, String)}</li>
     * <li>if the test account isn't active, then activate it using
     * {@link SarosM#activateAccountNoGUI(JID)}</li>
     * <li>Waits until the connection is really done. It guarantee that the
     * following action is running successfully.</li>
     * </ol>
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @param password
     *            the password of the given jid
     * @throws RemoteException
     */
    public void connectNoGUI(JID jid, String password) throws RemoteException;

    /**
     * connect using GUI-variant.
     * <p>
     * <b>Note</b>: This method isn't completely implemented yet, because
     * GUI-people will make big change on the roster view.
     * 
     * @param jid
     * @param password
     * @throws RemoteException
     */
    public void connectWith(JID jid, String password) throws RemoteException;

    /**
     * @return <tt>true</tt>, if Saros is connected to a XMPP server.
     * @throws RemoteException
     */
    public boolean isConnectedNoGUI() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if the toolbarbutton with the tooltip text
     *                       "Disconnect.*" is visible.
     * @throws RemoteException
     */
    public boolean isConnected() throws RemoteException;

    /**
     * waits until the xmpp connection is really created without GUI
     * 
     * @throws RemoteException
     */
    public void waitUntilIsConnectedNoGUI() throws RemoteException;

    /**
     * waits until the xmpp connection is really created
     * 
     * @throws RemoteException
     */
    public void waitUntilIsConnected() throws RemoteException;

    /**
     * disconnect without GUI
     * 
     * @throws RemoteException
     */
    public void disconnectNoGUI() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if Saros is not connected to a XMPP Server
     * @throws RemoteException
     */
    public boolean isDisConnectedNoGUI() throws RemoteException;

    /**
     * disconnect using GUI variant
     * 
     * @throws RemoteException
     */
    public void disconnect() throws RemoteException;

    /**
     * Waits until the connection is disconnected without GUI
     * 
     * @throws RemoteException
     */
    public void waitUntilDisConnectedNoGUI() throws RemoteException;

    /**
     * Waits until the connection is disconnected using GUI
     * 
     * @throws RemoteException
     */
    public void waitUntilDisConnected() throws RemoteException;

    /**********************************************
     * 
     * toolbar buttons on the view: add a new contact
     * 
     **********************************************/

    /**
     * performs the action "add a new contact" which should be activated by
     * clicking the tool bar button with the tooltip text "Add a new contact" on
     * the roster view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the roster view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the toolbar button. I
     * mean, after clicking the button you need to treat the following popup
     * window too.</li>
     * </ol>
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void addANewBuddy(JID jid) throws RemoteException;

    /**********************************************
     * 
     * operations about buddy on the roster view
     * 
     **********************************************/
    /**
     * select the buddy showed under Buddies tree specified with the given
     * baseJID on the roster view.
     * 
     * @param baseJID
     *            the base JID of the contact showed under Buddies which you
     *            want to select
     * @throws RemoteException
     */
    public void selectBuddy(String baseJID) throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            the JID of the user, which should be located in your buddy
     *            list, if you already have added the user.
     * @return<tt>true</tt>, if {@link Roster#getEntries()} contains this given
     *                       buddyJID
     * @throws RemoteException
     */
    public boolean hasBuddyNoGUI(JID buddyJID) throws RemoteException;

    /**
     * 
     * @return the BaseJID list of all the buddies existed in the roster view.
     * @throws RemoteException
     */
    public List<String> getAllBuddiesNoGUI() throws RemoteException;

    /**
     * 
     * @param buddyNickName
     *            the base JID of the contact showed under Buddies which you
     *            want to select
     * @return <tt>true</tt>, if the buddy specified with the given baseJID
     *         exists.
     * @throws RemoteException
     */
    public boolean hasBuddy(String buddyNickName) throws RemoteException;

    /**********************************************
     * 
     * context menu of a contact on the view: delete Contact
     * 
     **********************************************/
    /**
     * performs the action "Delete contact" without GUI
     * 
     * @param buddyJID
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     */
    public void deleteBuddyNoGUI(JID buddyJID) throws RemoteException,
        XMPPException;

    /**
     * performs the action "Delete contact" which should be activated by
     * clicking the context menu with the text "Delete contact" on the roster
     * view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the roster view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the context menu. I
     * mean, after clicking the menu you need to treat the following popup
     * window too.</li>
     * </ol>
     * 
     * @param buddyJID
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void deleteBuddy(JID buddyJID) throws RemoteException;

    /**
     * This popup window should be appeared by you, after someone else deleted
     * your contact from his buddies.
     * 
     * This method should be called by {@link Tester#deleteBuddyGUIDone(Tester)}
     * 
     * @throws RemoteException
     */
    public void confirmShellRemovelOfSubscription() throws RemoteException;

    /**********************************************
     * 
     * context menu of a contact on the view: rename Contact
     * 
     **********************************************/

    /**
     * rename the buddy'name specified with the given baseJID without GUI
     * 
     * @param buddyJID
     *            the baseJID of the user, whose contact under the buddies you
     *            want to change
     * @param newBuddyName
     *            the new name , to which the contact should be changed
     */
    public void renameBuddyNoGUI(JID buddyJID, String newBuddyName)
        throws RemoteException;

    /**
     * rename the buddy'name specified with the given baseJID without GUI
     * 
     * @param buddyBaseJID
     *            the baseJID of the user, whose contact under the buddies you
     *            want to change
     * @param newBuddyName
     *            the new name , to which the contact should be changed
     */
    public void renameBuddyNoGUI(String buddyBaseJID, String newBuddyName)
        throws RemoteException;

    /**
     * reset all buddies's nickname to their baseJID.
     * 
     * @throws RemoteException
     */
    public void resetAllBuddyNameNoGUI() throws RemoteException;

    public void resetBuddiesName(JID... jids) throws RemoteException;

    /**
     * rename the buddy'name specified with the given baseJID to the given
     * newName
     * 
     * @param buddyJID
     *            the baseJID of the user, whose contact under the buddies you
     *            want to change
     * @param newBuddyName
     *            the new name , to which the contact should be changed
     * @throws RemoteException
     */
    public void renameBuddy(JID buddyJID, String newBuddyName)
        throws RemoteException;

    /**********************************************
     * 
     * context menu of a contact on the view: invite user
     * 
     **********************************************/
    public void inviteBuddy(JID buddyJID) throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            the JID of the user, which should be located in your buddy
     *            list, if you already have added the user.
     * @return the nickname of the given user.
     * @throws RemoteException
     */
    public String getBuddyNickNameNoGUI(JID buddyJID) throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            the JID of the user, which should be located in your buddy
     *            list, if you already have added the user.
     * @return <tt>true</tt>, if you've given the given user a nickname.
     * @throws RemoteException
     */
    public boolean hasBuddyNickNameNoGUI(JID buddyJID) throws RemoteException;

}
