package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateImp;
import de.fu_berlin.inf.dpp.ui.RosterView;

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

    public SWTBotTreeItem selectBuddy(String contact) throws RemoteException;

    public boolean isBuddyExist(String contact) throws RemoteException;

    public boolean isConnectedGUI() throws RemoteException;

    /**
     * This method returns true if {@link SarosStateImp} and the GUI
     * {@link RosterView} having the connected state.
     */
    public boolean isConnected() throws RemoteException;

    public void clickAddANewContactToolbarButton() throws RemoteException;

    public void waitUntilIsConnected() throws RemoteException;

    public void waitUntilIsDisConnected() throws RemoteException;

    public void addANewContact(JID jid) throws RemoteException;

    public boolean hasContactWith(JID jid) throws RemoteException;

    public void deleteContact(JID jid) throws RemoteException;

    public void renameContact(String contact, String newName)
        throws RemoteException;

    public void connect(JID jid, String password) throws RemoteException;

    /**
     * Fill up the configuration wizard with title "Saros Configuration".
     */
    public void confirmWizardCreateXMPPAccount(String xmppServer, String jid,
        String password) throws RemoteException;

    public void clickContextMenuOfBuddy(String context, String baseJID)
        throws RemoteException;

    public void confirmContactLookupFailedWindow(String buttonType)
        throws RemoteException;

    public void confirmRemovelOfSubscriptionWindow() throws RemoteException;

    public boolean isWindowContactLookupFailedActive() throws RemoteException;

    public void waitUntilContactLookupFailedIsActive() throws RemoteException;

    public boolean isWindowContactAlreadyAddedActive() throws RemoteException;

    public void waitUntilWindowContactAlreadyAddedIsActive()
        throws RemoteException;

    public void confirmNewContactWindow(String plainJID) throws RemoteException;

    public void closeWindowContactAlreadyAdded() throws RemoteException;

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException;
}
