package saros.communication.chat.muc;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ChatState;
import saros.annotations.Component;
import saros.communication.chat.AbstractChatService;
import saros.communication.chat.IChat;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.XMPPConnectionService;

/** This class manages the creation and destruction of {@link MultiUserChat}s. */
@Component(module = "communication")
public class MultiUserChatService extends AbstractChatService {
  private static final Logger log = Logger.getLogger(MultiUserChatService.class);

  private Set<MultiUserChat> chats = new HashSet<MultiUserChat>();

  private AtomicReference<Connection> connection = new AtomicReference<Connection>(null);

  private final IConnectionListener listener =
      new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection, ConnectionState state) {

          if (state == ConnectionState.CONNECTED)
            MultiUserChatService.this.connection.set(connection);
          else MultiUserChatService.this.connection.set(null);
        }
      };

  /**
   * Construct a new MultiUserChat.
   *
   * @param connectionService
   */
  public MultiUserChatService(XMPPConnectionService connectionService) {
    connectionService.addListener(listener);
  }

  /**
   * Connects to a {@link MultiUserChat}. Automatically (if necessary) created and joins the {@link
   * MultiUserChat} based on the {@link MultiUserChatPreferences}.
   *
   * @param preferences
   * @return an {@link IChat} interface for the created chat or <code>null</code> if the chat
   *     creation failed
   */
  /*
   * TODO connectMUC should be split into create and join; bkahlert 2010/11/23
   */
  public IChat createChat(MultiUserChatPreferences preferences) {
    Connection currentConnection = this.connection.get();

    if (currentConnection == null) {
      log.error("Can't join chat: Not connected.");
      return null;
    }

    MultiUserChat chat = new MultiUserChat(currentConnection, preferences);

    log.debug("Joining MUC...");

    if (preferences.getService() == null) {
      log.warn("MUC service is not available, aborting connection request");
      notifyChatAborted(chat, null);
      return null;
    }

    boolean createdRoom = false;

    try {
      createdRoom = chat.connect();
    } catch (XMPPException e) {
      notifyChatAborted(chat, e.getMessage());
      log.error("Couldn't join chat: " + preferences.getRoom(), e);
      return null;
    }

    chats.add(chat);
    chat.setCurrentState(ChatState.active);
    notifyChatCreated(chat, createdRoom);
    chat.notifyJIDConnected(chat.getJID());

    return chat;
  }

  /**
   * Disconnects from a {@link MultiUserChat}. Automatically destroys the {@link MultiUserChat} if
   * the participant created it.
   *
   * @param chat
   */
  @Override
  public void destroyChat(IChat chat) {
    assert chat != null;
    log.debug("leaving multi user chat " + chat);

    chats.remove(chat);

    chat.disconnect();
    notifyChatDestroyed(chat);
  }

  /** {@inheritDoc} */
  @Override
  public Set<IChat> getChats() {
    return new HashSet<IChat>(chats);
  }
}
