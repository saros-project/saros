package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChatroom extends Remote {

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException;

    /**
     * Sends the current content of the chat input to the chat server by
     * pressing the ENTER key.
     * 
     * @throws RemoteException
     */
    public void sendChatMessage() throws RemoteException;

    /**
     * Sends a message to the chat server.
     * 
     * @param message
     *            the message to send
     * @throws RemoteException
     */
    public void sendChatMessage(String message) throws RemoteException;

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException;

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException;

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException;

    public String getTextOfFirstChatLine() throws RemoteException;

    public String getTextOfChatLine(int index) throws RemoteException;

    public String getTextOfLastChatLine() throws RemoteException;

    public String getTextOfChatLine(String regex) throws RemoteException;

    public void waitUntilGetChatMessage(String jid, String message)
        throws RemoteException;

}