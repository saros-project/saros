package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.SarosComponent;

public interface ChatView extends SarosComponent {

    public void openChatView() throws RemoteException;

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException;

    public void activateChatView() throws RemoteException;

    public void closeChatView() throws RemoteException;

    public boolean isChatViewOpen() throws RemoteException;

    public void sendChatMessage(String message) throws RemoteException;

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException;

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException;

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException;

    public String getTextOfChatLine() throws RemoteException;

    public String getTextOfChatLine(int index) throws RemoteException;

    public String getTextOfLastChatLine() throws RemoteException;

    public String getTextOfChatLine(String regex) throws RemoteException;

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException;
}
