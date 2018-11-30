package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.chat.chatRoom;

import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChat;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatPreferences;
import de.fu_berlin.inf.dpp.ui.widgets.chat.ChatControl;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.IChatControlListener;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.MessageEnteredEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class ChatRoomParticipant {
  protected static String server = "jabber.ccc.de";
  protected static String[] usernames = new String[] {"alice1_fu", "bob1_fu", "carl1_fu"};
  protected static String[] passwords = new String[] {"dddfffggg", "dddfffggg", "dddfffggg"};
  protected static String[] nicknames = new String[] {"Björn", "Maria", "Lin"};
  protected static RGB[] colors =
      new RGB[] {new RGB(141, 206, 231), new RGB(191, 187, 130), new RGB(186, 220, 81)};

  protected XMPPConnection connection;
  protected Chat[] chats;
  protected String username;
  protected String password;
  protected ChatControl chatControl;
  protected String user;
  protected Color color;

  public static ChatRoomParticipant[] create(int num, Composite parent) throws XMPPException {
    if (num > 3 || num < 0) return null;

    Color colorDisplayBackground = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
    Color colorInputBackground = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);

    ChatRoomParticipant[] chatRoomParticipants = new ChatRoomParticipant[num];
    for (int i = 0; i < num; i++) {
      XMPPConnection connection = new XMPPConnection(server);

      String username = usernames[i];
      String password = passwords[i];

      MultiUserChatPreferences preferences = new MultiUserChatPreferences(server, "demo", "");

      ChatControl chatControl =
          new ChatControl(
              null,
              new MultiUserChat(connection, preferences),
              parent,
              SWT.BORDER,
              colorDisplayBackground,
              2);

      String nickname = nicknames[i];

      Color color = new Color(parent.getDisplay(), colors[i]);

      chatRoomParticipants[i] =
          new ChatRoomParticipant(connection, username, password, chatControl, nickname, color);
    }

    return chatRoomParticipants;
  }

  public static ChatRoomParticipant getByUserJID(
      ChatRoomParticipant[] chatRoomParticipants, String userJID) {
    for (ChatRoomParticipant chatRoomParticipant : chatRoomParticipants) {
      try {
        if (chatRoomParticipant.getConnection().getUser().equals(userJID))
          return chatRoomParticipant;
      } catch (Exception e) {
        break;
      }
    }
    return null;
  }

  protected ChatRoomParticipant(
      XMPPConnection connection,
      String username,
      String password,
      ChatControl chatControl,
      String user,
      Color color) {
    super();
    this.connection = connection;
    this.username = username;
    this.password = password;
    this.chatControl = chatControl;
    this.chatControl.addChatControlListener(
        new IChatControlListener() {

          @Override
          public void chatCleared(ChatClearedEvent event) {
            // TODO Auto-generated method stub

          }

          @Override
          public void characterEntered(CharacterEnteredEvent event) {
            // TODO Auto-generated method stub

          }

          @Override
          public void messageEntered(MessageEnteredEvent event) {
            sendMessage(event.getEnteredMessage());
          }
        });

    this.user = user;
    this.color = color;
  }

  public XMPPConnection getConnection() {
    return connection;
  }

  public void connect() {
    try {
      connection.connect();
      connection.login(username, password);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void disconnect() {
    try {
      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates a chat for each participant and the sender.
   *
   * @param to
   * @param messageListener
   * @return
   */
  public Chat[] createChats(ChatRoomParticipant[] to, MessageListener messageListener) {
    chats = new Chat[to.length];
    ChatManager chatManager = connection.getChatManager();
    for (int i = 0; i < to.length; i++) {
      String user = to[i].getConnection().getUser();
      chats[i] = chatManager.createChat(user, messageListener);
    }

    return chats;
  }

  public void sendMessage(String message) {
    try {
      if (chats == null) return;
      for (Chat chat : chats) {
        chat.sendMessage(message);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void destroyChats() {
    this.chats = null;
  }

  public ChatControl getChatControl() {
    return chatControl;
  }

  public String getUser() {
    return user;
  }

  public Color getColor() {
    return color;
  }
}
