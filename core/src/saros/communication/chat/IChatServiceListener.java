package saros.communication.chat;

import java.util.Optional;

/** This listener is invoked with notifications related to an {@link IChat}s life cycle. */
public interface IChatServiceListener {

  /**
   * Gets called whenever an {@link IChat} was created.
   *
   * @param chat {@link IChat} which has been created
   * @param createdLocally <code>true</code> if the chat has been created by the local user, <code>
   *     false</code> if the chat has been created by some remote action
   */
  public void chatCreated(IChat chat, boolean createdLocally);

  /**
   * Gets called whenever an {@link IChat} was destroyed.
   *
   * @param chat {@link IChat} which has been destroyed
   */
  public void chatDestroyed(IChat chat);

  /**
   * Gets called whenever an {@link IChat} has been aborted through errors in the XMPP/network
   * layer.
   *
   * @param chat the {@link IChat} that has been aborted
   * @param errorMessage Optional an describing error message, always empty if the chat has not been
   *     created yet
   */
  public void chatAborted(IChat chat, Optional<String> errorMessage);
}
