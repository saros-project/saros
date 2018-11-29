package de.fu_berlin.inf.dpp.communication.chat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jivesoftware.smack.XMPPException;

/**
 * Parent class for chat services. It provides convenience methods for notifying registered
 * listeners.
 */
public abstract class AbstractChatService implements IChatService {
  private List<IChatServiceListener> chatServiceListeners =
      new CopyOnWriteArrayList<IChatServiceListener>();

  /** {@inheritDoc} */
  @Override
  public void addChatServiceListener(IChatServiceListener chatServiceListener) {
    chatServiceListeners.add(chatServiceListener);
  }

  /** {@inheritDoc} */
  @Override
  public void removeChatServiceListener(IChatServiceListener chatServiceListener) {
    chatServiceListeners.remove(chatServiceListener);
  }

  /**
   * Notify all {@link IChatServiceListener}s that an {@link IChat} has been created.
   *
   * @param chat {@link IChat} which has been created
   * @param createdLocally <code>true</code> if the chat has been created by the local user, <code>
   *     false</code> if the chat has been created by some remote action
   */
  public void notifyChatCreated(IChat chat, boolean createdLocally) {
    for (IChatServiceListener listener : chatServiceListeners) {
      listener.chatCreated(chat, createdLocally);
    }
  }

  /**
   * Notify all {@link IChatServiceListener}s that an {@link IChat} has been destroyed.
   *
   * @param chat {@link IChat} which has been destroyed
   */
  public void notifyChatDestroyed(IChat chat) {
    for (IChatServiceListener listener : chatServiceListeners) {
      listener.chatDestroyed(chat);
    }
  }

  /**
   * Notify all {@link IChatServiceListener}s that an {@link IChat} has been aborted through errors
   * in the XMPP/network layer.
   *
   * @param chat the {@link IChat} that has been aborted
   * @param exception {@link XMPPException} that has been thrown
   */
  public void notifyChatAborted(IChat chat, XMPPException exception) {
    for (IChatServiceListener listener : chatServiceListeners) {
      listener.chatAborted(chat, exception);
    }
  }
}
