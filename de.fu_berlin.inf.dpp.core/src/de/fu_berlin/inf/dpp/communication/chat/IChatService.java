package de.fu_berlin.inf.dpp.communication.chat;

import java.util.Set;

/** This interface is used for handling of the {@link IChat}s life cycle. */
public interface IChatService {

  /**
   * Register chat service listener for events.
   *
   * @param chatServiceListener chat service listener to register for events
   */
  public void addChatServiceListener(IChatServiceListener chatServiceListener);

  /**
   * Unregister chat service listener from events.
   *
   * @param chatServiceListener chat service listener to unregister from events
   */
  public void removeChatServiceListener(IChatServiceListener chatServiceListener);

  /**
   * Destroy and disconnect chat object.
   *
   * @param chat chat to destroy and disconnect
   */
  public void destroyChat(IChat chat);

  /**
   * Returns all chats that belong to the chat service.
   *
   * @return all chats that belong to the chat service
   */
  public Set<IChat> getChats();
}
