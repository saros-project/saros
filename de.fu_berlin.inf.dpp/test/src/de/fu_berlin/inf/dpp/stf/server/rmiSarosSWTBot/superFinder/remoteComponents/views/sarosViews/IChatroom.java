package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChatroom extends Remote {

    public void sendChatMessage(String message) throws RemoteException;

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException;

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

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException;

}