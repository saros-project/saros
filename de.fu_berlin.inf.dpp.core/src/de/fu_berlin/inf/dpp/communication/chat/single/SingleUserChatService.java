package de.fu_berlin.inf.dpp.communication.chat.single;

import de.fu_berlin.inf.dpp.communication.chat.AbstractChatService;
import de.fu_berlin.inf.dpp.communication.chat.ChatElement;
import de.fu_berlin.inf.dpp.communication.chat.IChat;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.ChatStateManager;

public class SingleUserChatService extends AbstractChatService {

  private static final Logger LOG = Logger.getLogger(SingleUserChatService.class);

  private IConnectionListener connectionLister =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection, ConnectionState newState) {
          synchronized (SingleUserChatService.this) {
            if (newState == ConnectionState.CONNECTING) {
              chatManager = connection.getChatManager();
              chatManager.addChatListener(chatManagerListener);
            } else if (newState == ConnectionState.CONNECTED) {
              chatStateManager = ChatStateManager.getInstance(connection);

              connected = true;
              userJID = connection.getUser();
              updateChats();

              for (SingleUserChat chat : currentChats.values()) chat.setConnected(true);

            } else if (newState == ConnectionState.DISCONNECTING
                || newState == ConnectionState.ERROR) {
              if (chatManager != null) chatManager.removeChatListener(chatManagerListener);

              userJID = null;
              chatManager = null;
              chatStateManager = null;
              connected = false;

              for (SingleUserChat chat : currentChats.values()) {
                chat.setConnected(false);
              }
            }
          }
        }
      };

  private ChatManagerListener chatManagerListener =
      new ChatManagerListener() {

        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
          synchronized (SingleUserChatService.this) {
            chat.addMessageListener(messageListener);
          }
        }
      };

  private MessageListener messageListener =
      new MessageListener() {

        @Override
        public void processMessage(Chat chat, Message message) {
          if (!message.getBodies().isEmpty()) {

            LOG.info(
                "chat created between "
                    + userJID
                    + " <->"
                    + chat.getParticipant()
                    + ", created local: "
                    + false);

            JID jid = new JID(chat.getParticipant());
            SingleUserChat singleUserChat = currentChats.get(jid);

            boolean exists = true;
            if (singleUserChat == null) {
              exists = false;
              singleUserChat = createChat(chat);

              /*
               * The SUC has registered for messages now. As this happened
               * after the notification of listeners started, it won't
               * receive the current, first message. Thus, we have to add
               * it manually.
               */
              singleUserChat.addHistoryEntry(new ChatElement(message, new Date()));
            }

            // do not inform the listener because the chat is reused if
            if (exists) {
              LOG.info("skipping notification of listeners because the chat already exists");
              return;
            }

            notifyChatCreated(singleUserChat, false);
          }
        }
      };

  private Map<JID, SingleUserChat> currentChats = new HashMap<JID, SingleUserChat>();

  private ChatManager chatManager;
  private ChatStateManager chatStateManager;

  private String userJID;
  private boolean connected;

  public SingleUserChatService(XMPPConnectionService connectionService) {
    this.connected = false;
    connectionService.addListener(connectionLister);
  }

  /**
   * Create a new {@link SingleUserChat} with the given {@link JID} as participant.
   *
   * @param toUserJID participant of the new chat
   * @return a new {@link SingleUserChat} if it has not been created yet, otherwise the existing
   *     {@link SingleUserChat} is returned
   */
  public synchronized SingleUserChat createChat(JID toUserJID) {
    if (!connected) throw new IllegalStateException("not connected to a xmpp server");

    SingleUserChat chat = currentChats.get(toUserJID);

    if (chat == null) {
      chat = createChat(chatManager.createChat(toUserJID.getBase(), null));
    }

    return chat;
  }

  /**
   * Creates a new {@link SingleUserChat} with the given {@link Chat}.
   *
   * @param chat {@link Chat} which specifies the participant
   * @return a new {@link SingleUserChat} if it has not been created yet, otherwise the existing
   *     {@link SingleUserChat} is returned.
   */
  public synchronized SingleUserChat createChat(Chat chat) {

    JID jid = new JID(chat.getParticipant());

    SingleUserChat createdChat = currentChats.get(jid);

    if (createdChat == null) {
      LOG.trace("creating new chat between " + userJID + " <->" + jid);

      createdChat = new SingleUserChat();

      createdChat.initChat(userJID, chat, chatStateManager);
      createdChat.setConnected(true);
      currentChats.put(jid, createdChat);
    }

    return createdChat;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized Set<IChat> getChats() {
    return new HashSet<IChat>(currentChats.values());
  }

  private synchronized void updateChats() {
    for (SingleUserChat chat : currentChats.values()) updateChat(chat);
  }

  private synchronized void updateChat(SingleUserChat chat) {
    LOG.trace(
        "updating chat between " + userJID + " <->" + chat.getParticipants().iterator().next());

    chat.initChat(
        userJID,
        chatManager.createChat(
            chat.getParticipants().iterator().next().getBase(), chat.getMessageListener()),
        chatStateManager);
  }

  /** Disconnect the {@link IChat} */
  @Override
  public void destroyChat(IChat chat) {
    chat.disconnect();
  }
}
