package de.fu_berlin.inf.dpp.communication.chat;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.util.Date;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.ChatState;

/** Chat element class representing entries in the {@link ChatHistory}. */
public class ChatElement {
  /**
   * Represents the type of a chat element. For {@link ChatElement}s, those types change their
   * textual representation.
   */
  public enum ChatElementType {
    /** Used to represent a chat message. */
    MESSAGE,
    /** Used to represent a 'user joined'-notification. */
    JOIN,
    /** Used to represent a 'user left'-notification. */
    LEAVE,
    /** Used to represent a confirmation for received messages. */
    MESSAGERECEPTION,
    /** Used to represent a change of the {@link ChatState}. */
    STATECHANGE
  }

  private String message;
  private Date date;
  private JID jid;
  private ChatElementType type;
  private ChatState state;

  /**
   * Create a chat element with the given chat element type. The message will be empty. This can be
   * for example leave- and join-notifications.
   *
   * @param jid JID of the user to whom the element refers to, e.g. which user joined or left
   * @param date date of the occurrence, e.g. the joining or leaving
   * @param type type of the new chat element
   */
  public ChatElement(JID jid, Date date, ChatElementType type) {
    this.jid = jid;
    this.date = date;
    this.type = type;
  }

  /**
   * Construct a chat element with the given message body. The type of this element will be {@link
   * ChatElementType#MESSAGE}.
   *
   * @param message message body of the chat element
   * @param jid sender of the message
   * @param date date the message has been received
   */
  public ChatElement(String message, JID jid, Date date) {
    this.message = message;
    this.jid = jid;
    this.date = date;
    this.type = ChatElementType.MESSAGE;
  }

  /**
   * Construct a chat element that represents a normal chat message. The type of this element will
   * be {@link ChatElementType#MESSAGE}.
   *
   * @param message The message from which the body and the sender will be extracted
   * @param date date the message has been received
   */
  public ChatElement(Message message, Date date) {
    this.message = message.getBody();
    this.jid = new JID(message.getFrom());
    this.date = date;
    this.type = ChatElementType.MESSAGE;
  }

  /**
   * Construct a chat element that represents a chat state change (type {@link
   * ChatElementType#STATECHANGE}). The message will be empty.
   *
   * @param jid JID of the user whose chat state has changed
   * @param date date of the chat state change
   * @param state new chat state
   */
  public ChatElement(JID jid, Date date, ChatState state) {
    this.jid = jid;
    this.date = date;
    this.state = state;
    this.type = ChatElementType.STATECHANGE;
  }

  /**
   * Returns the sender/causer of the entry.
   *
   * @return the sender/causer of the entry
   */
  public JID getSender() {
    return jid;
  }

  /**
   * Returns the date the entry has been created.
   *
   * @return the date the entry has been created.
   */
  public Date getDate() {
    return date;
  }

  /**
   * Returns the message of the entry.
   *
   * @return the message of the entry or <code>null</code> if the entry does not contain a message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the {@link ChatElementType}.
   *
   * @return the {@link ChatElementType}
   */
  public ChatElementType getChatElementType() {
    return type;
  }

  /**
   * Returns the {@link ChatState}.
   *
   * @return the {@link ChatState}
   */
  public ChatState getChatState() {
    return state;
  }

  /** Return typical representation of chat element. */
  @Override
  public String toString() {
    return type.toString();
  }
}
