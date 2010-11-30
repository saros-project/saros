package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;

/**
 * This interface contains convenience API to perform a action using widgets in
 * the roster view. then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Musician} object in your junit-test.
 * (How to do it please look at the javadoc in class {@link TestPattern} or read
 * the user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * after then you can use the object rosterV initialized in {@link Musician} to
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

    public void disconnect() throws RemoteException;

    public void waitUntilIsDisConnected() throws RemoteException;

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
     * it using {@link SarosState#createAccount(String, String, String)}</li>
     * <li>if the test account isn't active, then activate it using
     * {@link SarosState#activateAccount(JID)}</li>
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
    public void confirmWizardCreateXMPPAccount(String xmppServer, String jid,
        String password) throws RemoteException;

    /**
     * 
     * @return <tt>true</tt>, if both {@link SarosState#isConnected()} and the
     *         GUI {@link RosterViewComponent#isConnectedGUI()} return true.
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
     * waits until the xmpp connection is really created
     * 
     * @throws RemoteException
     */
    public void waitUntilIsConnected() throws RemoteException;

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
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void addANewContact(JID jid) throws RemoteException;

    /**
     * confirm the popup window which should be activated by cliking the
     * toolbarbutton "add a new contact". so this method should be called by
     * {@link RosterViewComponent#addANewContact(JID)}
     * 
     * @param baseJID
     *            the base JID needed for creating a new contact
     * @throws RemoteException
     */
    public void confirmNewContactWindow(String baseJID) throws RemoteException;

    /**
     * 
     * @param buddyJID
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return <tt>true</tt>, if you have already contained the contact
     *         specified by the given jid.
     * @throws RemoteException
     */
    public boolean hasBuddy(JID buddyJID) throws RemoteException;

    /**
     * click only the tool bar button with the tooltip text "Add a new contact"
     * on the roster view.This method is different as the
     * {@link RosterViewComponent#addANewContact(JID)},which should treat the
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
     * 
     * @return <tt>true</tt>, is the popup window with the title
     *         "Contact lookup failed" is active
     * @throws RemoteException
     */
    public boolean isWindowContactLookupFailedActive() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, is the popup window with the title
     *                       "Contact already added" is active
     * @throws RemoteException
     */
    public boolean isWindowContactAlreadyAddedActive() throws RemoteException;

    /**********************************************
     * 
     * select buddy on the roster view
     * 
     **********************************************/
    /**
     * select the buddy showed under Buddies tree specified with the given
     * baseJID on the roster view.
     * 
     * @param baseJID
     *            the base JID of the contact showed under Buddies which you
     *            want to select
     * @return
     * @throws RemoteException
     */
    public SWTBotTreeItem selectBuddy(String baseJID) throws RemoteException;

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
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void deleteBuddy(JID jid) throws RemoteException;

    /**
     * This popup window should be appeared by you, after someone else deleted
     * your contact from his buddies.
     * 
     * This method should be called by
     * {@link Musician#deleteBuddyDone(Musician)}
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

    public void waitUntilContactLookupFailedIsActive() throws RemoteException;

    public void waitUntilWindowContactAlreadyAddedIsActive()
        throws RemoteException;

    public void closeWindowContactAlreadyAdded() throws RemoteException;

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException;
}
