package saros.communication.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class describes the history of incoming messages of a chat. It saves entries in the form of
 * {@link ChatElement} instances.
 */
public class ChatHistory {
  private List<ChatElement> history = Collections.synchronizedList(new LinkedList<ChatElement>());

  /**
   * Adds a new {@link ChatElement}
   *
   * @param entry that describes the chat message
   */
  public void addEntry(ChatElement entry) {
    history.add(entry);
  }

  /**
   * Returns all added {@link ChatElement}s
   *
   * @return
   */
  public List<ChatElement> getEntries() {
    synchronized (history) {
      return new ArrayList<ChatElement>(history);
    }
  }

  /** Removes all added {@link ChatElement}s from the {@link ChatHistory} */
  public void clear() {
    history.clear();
  }
}
