package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewObject;

/**
 * This interface contains convenience API to perform a action using widgets in
 * roster view. then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Musician} object in your junit-test.
 * (How to do it please look at the javadoc in class {@link TestPattern} or read
 * the user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * after then you can use the object sessionV initialized in {@link Musician} to
 * access the API :), e.g.
 * 
 * <pre>
 * alice.sessionV.openRosterView();
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface ExRosterViewObject extends Remote {

    /**
     * @throws RemoteException
     * @see ViewObject#openViewById(String)
     */
    public void openRosterView() throws RemoteException;

    public boolean isRosterViewOpen() throws RemoteException;

    public void setFocusOnRosterView() throws RemoteException;

    public void closeRosterView() throws RemoteException;

    public void xmppDisconnect() throws RemoteException;

    public SWTBotTreeItem selectBuddy(String contact) throws RemoteException;

    public boolean isBuddyExist(String contact) throws RemoteException;

    public boolean isConnectedByXmppGuiCheck() throws RemoteException;

    public boolean isConnectedByXMPP() throws RemoteException;

    public void clickTBAddANewContactInRosterView() throws RemoteException;

    public void clickTBConnectInRosterView() throws RemoteException;

    public boolean clickTBDisconnectInRosterView() throws RemoteException;

    public void waitUntilConnected() throws RemoteException;

    public void waitUntilDisConnected() throws RemoteException;

    public void addContact(JID jid) throws RemoteException;

    public boolean hasContactWith(JID jid) throws RemoteException;

    public void deleteContact(JID jid) throws RemoteException;

    public void renameContact(String contact, String newName)
        throws RemoteException;

    public void xmppConnect(JID jid, String password) throws RemoteException;
}
