package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.sarosViewComponents;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuComponent;

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
public interface RosterViewComponent extends Remote {

    /**
     * @throws RemoteException
     * @see ViewPart#openViewById(String)
     */
    public void openRosterView() throws RemoteException;

    public boolean isRosterViewOpen() throws RemoteException;

    public void setFocusOnRosterView() throws RemoteException;

    public boolean isRosterViewActive() throws RemoteException;

    public void closeRosterView() throws RemoteException;

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
     * it using
     * {@link SarosMainMenuComponent#createAccount(String, String, String)}</li>
     * <li>if the test account isn't active, then activate it using
     * {@link SarosMainMenuComponent#activateAccount(JID)}</li>
     * <li>Waits until the connection is really done. It guarantee that the
     * following action is running successfully.</li>
     * </ol>
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @param password
     *            the password of the given jid
     * @throws RemoteException
     */
    public void connect(JID jid, String password) throws RemoteException;

    /**
     * Fill up the configuration wizard to create a new account.
     * 
     * TODO how/when to activate the configuration wizard is still not clear to
     * me, i will complete the comments when the problems are fixed by
     * developers
     * 
     * @param xmppServer
     *            the xmpp server used by the given jid
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @param password
     *            the password of the given jid
     * @throws RemoteException
     */
    public void confirmWindowCreateXMPPAccount(String xmppServer, String jid,
        String password, boolean usesThisAccountNow) throws RemoteException;

    /**
     * @return <tt>true</tt>, if Saros is connected to a XMPP server.
     * @throws RemoteException
     */
    public boolean isConnected() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if the toolbarbutton with the tooltip text
     *                       "Disconnect.*" is visible.
     * @throws RemoteException
     */
    public boolean isConnectedGUI() throws RemoteException;

    /**
     * waits until the xmpp connection is really created without GUI
     * 
     * @throws RemoteException
     */
    public void waitUntilConnected() throws RemoteException;

    /**
     * waits until the xmpp connection is really created
     * 
     * @throws RemoteException
     */
    public void waitUntilConnectedGUI() throws RemoteException;

    /**
     * disconnect without GUI
     * 
     * @throws RemoteException
     */
    public void disconnect() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if Saros is not connected to a XMPP Server
     * @throws RemoteException
     */
    public boolean isDisConnected() throws RemoteException;

    /**
     * disconnect using GUI variant
     * 
     * @throws RemoteException
     */
    public void disconnectGUI() throws RemoteException;

    /**
     * Waits until the connection is disconnected without GUI
     * 
     * @throws RemoteException
     */
    public void waitUntilDisConnected() throws RemoteException;

    /**
     * Waits until the connection is disconnected using GUI
     * 
     * @throws RemoteException
     */
    public void waitUntilDisConnectedGUI() throws RemoteException;

    /**********************************************
     * 
     * toolbar buttons on the view: add a new contact
     * 
     **********************************************/

    /**
     * performs the action "add a new contact" using GUI
     */
    public void addANewContact(JID jid) throws RemoteException, XMPPException;

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
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void addANewContactGUI(JID jid) throws RemoteException;

    /**
     * After adding a new contact the added user should get this popup window
     * with the title "Request of subscription received", which should be run by
     * {@link Tester#addBuddyGUIDone(Tester)}
     * 
     * @throws RemoteException
     */
    public void confirmShellRequestOfSubscriptionReceived()
        throws RemoteException;

    /**
     * confirm the popup window which should be activated by cliking the
     * toolbarbutton "add a new contact". so this method should be called by
     * {@link RosterViewComponent#addANewContactGUI(JID)}
     * 
     * @param baseJID
     *            the base JID needed for creating a new contact
     * @throws RemoteException
     */
    public void confirmNewContactWindow(String baseJID) throws RemoteException;

    /**
     * click only the tool bar button with the tooltip text "Add a new contact"
     * on the roster view.This method is different as the
     * {@link RosterViewComponent#addANewContactGUI(JID)},which should treat the
     * activated popup window.
     * 
     * @throws RemoteException
     */
    public void clickAddANewContactToolbarButton() throws RemoteException;

    /**
     * this popup window should be activated by adding new contact if the new
     * contact is invalid.
     * 
     * @param buttonType
     *            YES or NO
     * @throws RemoteException
     */
    public void confirmContactLookupFailedWindow(String buttonType)
        throws RemoteException;

    /**
     * This popup window is only activated if man try to add a invalid contact
     * using the {@link RosterViewComponent#addANewContactGUI}
     * 
     * @return <tt>true</tt>, is the popup window with the title
     *         "Contact lookup failed" is active
     * @throws RemoteException
     */
    public boolean isWindowContactLookupFailedActive() throws RemoteException;

    /**
     * waits until the popup window with the title "Contact lookup failed" is
     * active. This popup window is only activated if man try to add a invalid
     * contact using the {@link RosterViewComponent#addANewContactGUI}
     * 
     * @throws RemoteException
     */
    public void waitUntilContactLookupFailedIsActive() throws RemoteException;

    /**
     * This popup window is only activated if man try to add a already existed
     * contact using the {@link RosterViewComponent#addANewContactGUI}
     * 
     * @return<tt>true</tt>, is the popup window with the title
     *                       "Contact already added" is active
     * @throws RemoteException
     */
    public boolean isWindowContactAlreadyAddedActive() throws RemoteException;

    /**
     * waits until the popup window with the title "Contact already added" is
     * active. This popup window is only activated if man try to add a already
     * existed contact using the {@link RosterViewComponent#addANewContactGUI}
     * 
     * @throws RemoteException
     */
    public void waitUntilWindowContactAlreadyAddedIsActive()
        throws RemoteException;

    /**
     * close the popup window with the title "Contact already added"
     * 
     * @throws RemoteException
     */
    public void closeWindowContactAlreadyAdded() throws RemoteException;

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
    public void selectBuddyGUI(String baseJID) throws RemoteException;

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
     * @return the BaseJID list of all the buddies existed in the roster view.
     * @throws RemoteException
     */
    public List<String> getAllBuddies() throws RemoteException;

    /**
     * 
     * @param buddyNickName
     *            the base JID of the contact showed under Buddies which you
     *            want to select
     * @return <tt>true</tt>, if the buddy specified with the given baseJID
     *         exists.
     * @throws RemoteException
     */
    public boolean hasBuddyGUI(String buddyNickName) throws RemoteException;

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

    /**********************************************
     * 
     * context menu of a contact on the view: delete Contact
     * 
     **********************************************/
    /**
     * performs the action "Delete contact" without GUI
     * 
     * @param buddyJID
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     */
    public void deleteBuddy(JID buddyJID) throws RemoteException, XMPPException;

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
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void deleteBuddyGUI(JID buddyJID) throws RemoteException;

    /**
     * This popup window should be appeared by you, after someone else deleted
     * your contact from his buddies.
     * 
     * This method should be called by {@link Tester#deleteBuddyGUIDone(Tester)}
     * 
     * @throws RemoteException
     */
    public void confirmRemovelOfSubscriptionWindow() throws RemoteException;

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
    public void renameBuddy(JID buddyJID, String newBuddyName)
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
    public void renameBuddy(String buddyBaseJID, String newBuddyName)
        throws RemoteException;

    /**
     * reset all buddies's nickname to their baseJID.
     * 
     * @throws RemoteException
     */
    public void resetAllBuddyName() throws RemoteException;

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
    public void renameBuddyGUI(JID buddyJID, String newBuddyName)
        throws RemoteException;

    /**********************************************
     * 
     * context menu of a contact on the view: invite user
     * 
     **********************************************/
    public void inviteUserGUI(JID buddyJID) throws RemoteException;

    /**
     * click the toolbar button specified with the given tooltip.
     * 
     * @param tooltipText
     *            the tooltip text of the toolbar button which you want to
     *            click.
     */
    public void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException;
}
