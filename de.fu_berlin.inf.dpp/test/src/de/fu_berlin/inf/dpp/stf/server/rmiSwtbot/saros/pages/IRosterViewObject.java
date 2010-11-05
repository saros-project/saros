package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

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
}
