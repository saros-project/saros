package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.ISarosContextMenuWrapper;

/**
 * This interface contains convenience API to perform a action using widgets in
 * the Saros Buddies view. then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link AbstractTester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * after then you can use the object rosterV initialized in {@link AbstractTester} to
 * access the API :), e.g.
 * 
 * <pre>
 * alice.rosterV.openRosterView();
 * </pre>
 * 
 * </li>
 * 
 * @author lchen
 */
public interface IBuddiesView extends Remote {

    /**********************************************
     * 
     * Actions
     * 
     **********************************************/
    /**
     * Connect with the given ID.
     * 
     * @param jid
     *            see {@link JID}
     * @param password
     * @throws RemoteException
     */
    public void connectWith(JID jid, String password) throws RemoteException;

    /**
     * Connect with the current active ID.
     * 
     * @throws RemoteException
     */
    public void connectWithActiveAccount() throws RemoteException;

    /**
     * Click the toolBarbotton "Disconnect".
     * 
     * @throws RemoteException
     */
    public void disconnect() throws RemoteException;

    /**
     * Performs the action "add a new buddy" which should be activated by
     * clicking the tool bar button with the toolTip text "Add a new buddy".
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the toolBar button.
     * i.e, after clicking the button the following popUp window would be
     * handled.</li>
     * </ol>
     * 
     * @param jid
     *            see {@link JID}.
     * @throws RemoteException
     */
    public void addANewBuddy(JID jid) throws RemoteException;

    /**
     * Select the buddy specified with the given baseJID which is located under
     * node "Buddies".
     * 
     * @param buddyJID
     *            see {@link JID}
     * @throws RemoteException
     */
    public ISarosContextMenuWrapper selectBuddy(JID buddyJID)
        throws RemoteException;

    /**********************************************
     * 
     * States
     * 
     **********************************************/

    /**
     * 
     * @return<tt>true</tt>, if the toolBarbutton with the toolTip text
     *                       "Disconnect.*" is visible.
     * @throws RemoteException
     */
    public boolean isConnected() throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            see {@link JID}
     * @return <tt>true</tt>, if the buddy specified with the given buddyJID
     *         exists.
     * @throws RemoteException
     */
    public boolean hasBuddy(JID buddyJID) throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            see {@link JID}
     * @return the nickname of the given user, if he has one.
     * @throws RemoteException
     */
    public String getNickName(JID buddyJID) throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            see {@link JID}
     * @return <tt>true</tt>, if the given user has a nickname.
     * @throws RemoteException
     */
    public boolean hasNickName(JID buddyJID) throws RemoteException;

    /**
     * 
     * @return all buddies name
     * @throws RemoteException
     */
    public List<String> getAllBuddies() throws RemoteException;

    /**********************************************
     * 
     * Wait until
     * 
     **********************************************/

    /**
     * Wait until the XMPP connection is connected.
     * 
     * @throws RemoteException
     */
    public void waitUntilIsConnected() throws RemoteException;

    /**
     * Wait until the connection is disconnected.
     * 
     * @throws RemoteException
     */
    public void waitUntilDisConnected() throws RemoteException;

}
