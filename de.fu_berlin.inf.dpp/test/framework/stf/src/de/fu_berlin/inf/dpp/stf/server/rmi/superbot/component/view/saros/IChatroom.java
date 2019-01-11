package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChatroom extends Remote {

  public boolean compareChatMessage(String jid, String message) throws RemoteException;

  /**
   * Enters the given message at the current cursor position of the chat input.
   *
   * @param message the message to insert
   * @throws RemoteException
   */
  public void enterChatMessage(String message) throws RemoteException;

  /**
   * Clears the chat input.
   *
   * @throws RemoteException
   */
  public void clearChatMessage() throws RemoteException;

  /**
   * Sends the current content of the chat input to the chat server by pressing the ENTER key.
   *
   * @throws RemoteException
   */
  public void sendChatMessage() throws RemoteException;

  /**
   * Sends a message to the chat server.
   *
   * @param message the message to send
   * @throws RemoteException
   */
  public void sendChatMessage(String message) throws RemoteException;

  /**
   * Returns the current unsent chat message of the chat input.
   *
   * @return the current unsent chat message
   * @throws RemoteException
   */
  public String getChatMessage() throws RemoteException;

  /**
   * Returns the title of the current chat (as displayed on the chat tab).
   *
   * @return the title of the current chat
   * @throws RemoteException
   */
  public String getTitle() throws RemoteException;

  /**
   * Returns the selection state of the chat.
   *
   * @return <code>true</code> if the chat is selected, <code>false</code> otherwise
   * @throws RemoteException
   */
  public boolean isActive() throws RemoteException;

  /**
   * Selects the chat, making it active and gaining the focus.
   *
   * @throws RemoteException
   */
  public void activate() throws RemoteException;

  public String getUserNameOnChatLinePartnerChangeSeparator() throws RemoteException;

  public String getUserNameOnChatLinePartnerChangeSeparator(int index) throws RemoteException;

  public String getUserNameOnChatLinePartnerChangeSeparator(String plainID) throws RemoteException;

  public String getTextOfFirstChatLine() throws RemoteException;

  public String getTextOfChatLine(int index) throws RemoteException;

  public String getTextOfLastChatLine() throws RemoteException;

  public String getTextOfChatLine(String regex) throws RemoteException;

  public void waitUntilGetChatMessage(String jid, String message) throws RemoteException;
}
