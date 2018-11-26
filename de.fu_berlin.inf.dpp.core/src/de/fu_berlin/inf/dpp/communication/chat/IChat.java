package de.fu_berlin.inf.dpp.communication.chat;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.util.List;
import java.util.Set;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.ChatState;

/** This interface is used to provide unified access to multiple chat types. */
public interface IChat {

  /**
   * Returns the JID of the local user.
   *
   * @return the JID of the local user
   */
  public JID getJID();

  /**
   * Returns the names of the user the chat is with.
   *
   * @return the names of the user the chat is with.
   */
  public Set<JID> getParticipants();

  /**
   * Returns the thread id associated with this chat, which corresponds to the thread field of XMPP
   * messages.
   *
   * @return the thread id associated with this chat, which corresponds to the thread field of XMPP
   *     messages.
   */
  public String getThreadID();

  /**
   * Sends a message to the other chat participants.
   *
   * @param message the message to send
   * @throws XMPPException
   */
  public void sendMessage(Message message) throws XMPPException;

  /**
   * Sends the specified text as a message to the other chat participants.
   *
   * @param text the specified text as a message to the other chat participant.
   * @throws XMPPException
   */
  public void sendMessage(String text) throws XMPPException;

  /**
   * Set this chat's {@link ChatState} and notify participants of the change.
   *
   * @param newState {@link ChatState} to set
   * @throws XMPPException
   */
  public void setCurrentState(ChatState newState) throws XMPPException;

  /**
   * Add {@link ChatElement} to this chat's history.
   *
   * @param entry chat element to add
   */
  void addHistoryEntry(ChatElement entry);

  /**
   * Returns the chat history for this chat.
   *
   * @return the chat history for this chat
   */
  public List<ChatElement> getHistory();

  /** Clears the current chat history. */
  public void clearHistory();

  /**
   * Add chat listener to receive notifications on events.
   *
   * @param chatListener chat listener to add
   */
  public void addChatListener(IChatListener chatListener);

  /**
   * Remove chat listener so it will not receive notifications on events.
   *
   * @param chatListener
   */
  public void removeChatListener(IChatListener chatListener);

  /**
   * Returns if it is possible to post messages to the chat.
   *
   * @return <code>true</code> if it is possible to post messages, otherwise <code>false</code>
   */
  public boolean isConnected();

  /** For internal use: use {@link IChatService#destroyChat(IChat)} to disconnect the chat */
  public boolean disconnect();
}
