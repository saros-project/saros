package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.ISarosRmiSWTWorkbenchBot;

public interface IRosterViewObject extends Remote {

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

    public void addContact(JID jid, ISarosRmiSWTWorkbenchBot participant)
        throws RemoteException;

    public boolean hasContactWith(JID jid) throws RemoteException;

    public void deleteContact(JID jid, ISarosRmiSWTWorkbenchBot participant)
        throws RemoteException;

    public void renameContact(String contact, String newName)
        throws RemoteException;
}
