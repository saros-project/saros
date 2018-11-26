/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.communication.chat.muc;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.chat.AbstractChatService;
import de.fu_berlin.inf.dpp.communication.chat.IChat;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ChatState;

/**
 * This class manages the creation and destruction of {@link MultiUserChat}s.
 *
 * @author rdjemili
 * @author ahaferburg
 * @author bkahlert
 */
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
      notifyChatAborted(chat, e);
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
