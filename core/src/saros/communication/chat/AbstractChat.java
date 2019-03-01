package de.fu_berlin.inf.dpp.communication.chat;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jivesoftware.smackx.ChatState;

/**
 * Abstract chat class that implements history management and provides basic functionality for
 * adding and removing {@link IChatListener}s.
 */
public abstract class AbstractChat implements IChat {
  private ChatHistory history = new ChatHistory();
  private List<IChatListener> chatListeners = new CopyOnWriteArrayList<IChatListener>();

  /** {@inheritDoc} */
  @Override
  public void addHistoryEntry(ChatElement entry) {
    history.addEntry(entry);
  }

  /** {@inheritDoc} */
  @Override
  public List<ChatElement> getHistory() {
    return history.getEntries();
  }

  /** {@inheritDoc} */
  @Override
  public void clearHistory() {
    history.clear();
  }

  /** {@inheritDoc} */
  @Override
  public void addChatListener(IChatListener chatListener) {
    chatListeners.add(chatListener);
  }

  /** {@inheritDoc} */
  @Override
  public void removeChatListener(IChatListener chatListener) {
    chatListeners.remove(chatListener);
  }

  /** Notify all {@link IChatListener}s about a connected {@link JID}. */
  public void notifyJIDConnected(JID jid) {
    for (IChatListener listener : chatListeners) {
      listener.connected(jid);
    }
  }

  /** Notify all {@link IChatListener}s about a disconnected {@link JID}. */
  public void notifyJIDDisconnected(JID jid) {
    for (IChatListener listener : chatListeners) {
      listener.disconnected(jid);
    }
  }

  /** Notify all {@link IChatListener}s about a received message */
  public void notifyJIDMessageReceived(JID sender, String message) {
    for (IChatListener listener : chatListeners) {
      listener.messageReceived(sender, message);
    }
  }

  /** Notify all {@link IChatListener}s about a changed {@link ChatState} */
  public void notifyJIDStateChanged(JID jid, ChatState state) {
    for (IChatListener listener : chatListeners) {
      listener.stateChanged(jid, state);
    }
  }
}
