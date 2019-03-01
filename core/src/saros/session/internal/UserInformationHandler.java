package saros.session.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.Startable;
import saros.activities.IResourceActivity;
import saros.annotations.Component;
import saros.communication.extensions.UserFinishedProjectNegotiationExtension;
import saros.communication.extensions.UserListExtension;
import saros.communication.extensions.UserListExtension.UserListEntry;
import saros.communication.extensions.UserListReceivedExtension;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.PacketCollector;
import saros.net.xmpp.JID;
import saros.preferences.PreferenceStore;
import saros.session.ISarosSession;
import saros.session.User;

/**
 * Business Logic for receiving and sending updates of the invitation state of users. Also handles
 * sending and responding to userLists after when a user joined the session
 */
@Component(module = "core")
public class UserInformationHandler implements Startable {

  private static final Logger log = Logger.getLogger(UserInformationHandler.class.getName());

  private static final long USER_LIST_SYNCHRONIZE_TIMEOUT = 10000L;

  private final ITransmitter transmitter;

  private final IReceiver receiver;

  private final ISarosSession session;

  private String currentSessionID;

  private volatile boolean isRunning;

  private final PacketListener userListListener =
      new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
          handleUserListUpdate(packet);
        }
      };

  private final PacketListener userFinishedProjectNegotiations =
      new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
          handleUserFinishedProjectNegotiationPacket(packet);
        }
      };

  public UserInformationHandler(
      ISarosSession session, ITransmitter transmitter, IReceiver receiver) {
    this.session = session;
    this.currentSessionID = session.getID();
    this.transmitter = transmitter;
    this.receiver = receiver;
  }

  @Override
  public void start() {

    receiver.addPacketListener(
        userListListener, UserListExtension.PROVIDER.getPacketFilter(currentSessionID));

    receiver.addPacketListener(
        userFinishedProjectNegotiations,
        UserFinishedProjectNegotiationExtension.PROVIDER.getPacketFilter(currentSessionID));

    isRunning = true;
  }

  @Override
  public void stop() {
    receiver.removePacketListener(userListListener);
    receiver.removePacketListener(userFinishedProjectNegotiations);
    isRunning = false;
  }

  /**
   * Synchronizes a user list with the given remote users.
   *
   * @param usersAdded collection containing the users that are added to the current session or
   *     <code>null</code>
   * @param usersRemoved collection containing the users that are removed from the current session
   *     or <code>null</code>
   * @param remoteUsers collection containing the users that will receive the user list
   * @return a list of users that did not reply when synchronizing the user list
   * @throws IllegalStateException if the local user of the session is not the host
   * @throws IllegalArgumentException if remoteUsers collection is empty<br>
   *     if usersAdded and usersRemoved are either both empty or <code>null</code>
   */
  public synchronized List<User> synchronizeUserList(
      Collection<User> usersAdded, Collection<User> usersRemoved, Collection<User> remoteUsers) {

    if (!session.isHost())
      throw new IllegalStateException("only the host can synchronize the user list");

    if (remoteUsers.isEmpty()) throw new IllegalArgumentException("remoteUser collection is empty");

    final List<User> notReplied = new ArrayList<User>();
    final List<User> awaitReply = new ArrayList<User>(remoteUsers);

    final UserListExtension extension = new UserListExtension(currentSessionID);

    if (usersAdded == null) usersAdded = Collections.emptyList();

    if (usersRemoved == null) usersRemoved = Collections.emptyList();

    if (usersAdded.isEmpty() && usersRemoved.isEmpty())
      throw new IllegalArgumentException("usersAdded and usersRemoved collections are empty");

    for (User user : usersAdded) extension.addUser(user, UserListEntry.USER_ADDED);

    for (User user : usersRemoved) extension.addUser(user, UserListEntry.USER_REMOVED);

    log.debug(
        "synchronizing user list (A)"
            + usersAdded
            + ", (R) "
            + usersRemoved
            + " with user(s) "
            + remoteUsers);

    final PacketCollector collector =
        receiver.createCollector(
            UserListReceivedExtension.PROVIDER.getPacketFilter(currentSessionID));

    try {
      for (User user : remoteUsers) {
        try {
          transmitter.send(
              ISarosSession.SESSION_CONNECTION_ID,
              user.getJID(),
              UserListExtension.PROVIDER.create(extension));
        } catch (IOException e) {
          log.error("failed to send user list to user: " + user, e);
          notReplied.add(user);
          awaitReply.remove(user);
        }
      }

      long synchronizeStart = System.currentTimeMillis();

      // see BUG #3544930 , the confirmation is useless

      while ((System.currentTimeMillis() - synchronizeStart) < USER_LIST_SYNCHRONIZE_TIMEOUT) {

        // remove users that left the session in the meantime
        List<User> currentRemoteUsers = session.getRemoteUsers();

        for (Iterator<User> it = awaitReply.iterator(); it.hasNext(); ) {
          User user = it.next();
          if (!currentRemoteUsers.contains(user)) {
            log.debug(
                "no longer waiting for user list confirmation of user " + user + " [left session]");
            it.remove();
          }
        }

        if (awaitReply.isEmpty() || !isRunning) break;

        Packet result = collector.nextResult(100);

        if (result == null) continue;

        JID jid = new JID(result.getFrom());

        if (!remove(awaitReply, jid)) {
          log.warn("received user list confirmation from unknown user: " + jid);
        } else {
          log.debug("received user list confirmation from: " + jid);
        }
      }

      notReplied.addAll(awaitReply);

      if (notReplied.isEmpty()) log.debug("synchronized user list with user(s) " + remoteUsers);
      else log.warn("failed to synchronize user list with user(s) " + notReplied);

      return notReplied;

    } finally {
      collector.cancel();
    }
  }

  /**
   * Informs all clients about the fact that a user now has projects and is able to process {@link
   * IResourceActivity}s.
   *
   * @param remoteUsers The users to be informed
   * @param jid The JID of the user this message is about
   */
  public void sendUserFinishedProjectNegotiation(Collection<User> remoteUsers, JID jid) {

    PacketExtension packet =
        UserFinishedProjectNegotiationExtension.PROVIDER.create(
            new UserFinishedProjectNegotiationExtension(currentSessionID, jid));

    for (User user : remoteUsers) {
      try {
        transmitter.send(ISarosSession.SESSION_CONNECTION_ID, user.getJID(), packet);
      } catch (IOException e) {
        log.error("failed to send userFinishedProjectNegotiation-message: " + user, e);
        // TODO remove user from session
      }
    }
  }

  /**
   * Handles incoming UserHasProjects-Packets and forwards the information to the session
   *
   * @param packet
   */
  private void handleUserFinishedProjectNegotiationPacket(Packet packet) {

    JID fromJID = new JID(packet.getFrom());

    UserFinishedProjectNegotiationExtension payload =
        UserFinishedProjectNegotiationExtension.PROVIDER.getPayload(packet);

    if (payload == null) {
      log.warn("UserFinishedProjectNegotiation-payload is corrupted");
      return;
    }

    User fromUser = session.getUser(fromJID);

    if (fromUser == null) {
      log.warn(
          "received UserFinishedProjectNegotiationPacket from "
              + fromJID
              + " who is not part of the current session");
      return;
    }

    /*
     * TODO This call needs a review as it is invoking listeners in another
     * thread context and thus blocking the dispatching for network packets
     * for an unknown time.
     */
    session.userFinishedProjectNegotiation(fromUser);
  }

  private void handleUserListUpdate(Packet packet) {
    /*
     * maybe it is better to execute all the code in a new thread to prevent
     * blocking the listener callback thread
     */
    JID fromJID = new JID(packet.getFrom());

    log.debug("received user list from " + fromJID);

    UserListExtension userListExtension = UserListExtension.PROVIDER.getPayload(packet);

    if (userListExtension == null) {
      log.warn("user list payload is corrupted");
      return;
    }

    User fromUser = session.getUser(fromJID);

    if (fromUser == null) {
      log.warn("received user list from " + fromJID + " who is not part of the current session");
      return;
    }

    for (UserListEntry userEntry : userListExtension.getEntries()) {
      User user = null;
      if ((userEntry.flags & UserListEntry.USER_ADDED) != 0) {
        user = session.getUser(userEntry.jid);

        if (user != null) {
          log.debug("updating permissions for user: " + user + " [" + userEntry.permission + "]");
          // FIXME this should be properly synchronized
          user.setPermission(userEntry.permission);
          continue;
        }

        user = new User(userEntry.jid, false, false, userEntry.colorID, userEntry.favoriteColorID);

        user.setPermission(userEntry.permission);
        session.addUser(user, new PreferenceStore());

      } else if ((userEntry.flags & UserListEntry.USER_REMOVED) != 0) {
        user = session.getUser(userEntry.jid);

        if (user == null) {
          log.warn("cannot remove user " + userEntry.jid + ", user is not in the session");
          continue;
        }

        session.removeUser(user);
      }
    }

    sendUserListConfirmation(fromJID);
  }

  private void sendUserListConfirmation(JID to) {
    log.debug("sending user list received confirmation to " + to);
    try {
      transmitter.send(
          ISarosSession.SESSION_CONNECTION_ID,
          to,
          UserListReceivedExtension.PROVIDER.create(
              new UserListReceivedExtension(currentSessionID)));
    } catch (IOException e) {
      log.error("failed to send user list received confirmation to: " + to, e);
    }
  }

  private boolean remove(Collection<User> users, JID jid) {
    for (Iterator<User> it = users.iterator(); it.hasNext(); ) {
      User user = it.next();

      if (user.getJID().strictlyEquals(jid)) {
        it.remove();
        return true;
      }
    }

    return false;
  }
}
