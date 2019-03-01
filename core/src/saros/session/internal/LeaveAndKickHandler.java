package de.fu_berlin.inf.dpp.session.internal;

import de.fu_berlin.inf.dpp.communication.extensions.KickUserExtension;
import de.fu_berlin.inf.dpp.communication.extensions.LeaveSessionExtension;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.Startable;

/**
 * A session component which listens for leave and kick messages from the session's host and stops
 * the session in response.
 */
public class LeaveAndKickHandler implements Startable {

  private static final Logger LOG = Logger.getLogger(LeaveAndKickHandler.class.getName());

  private final ISarosSession session;
  private final ISarosSessionManager sessionManager;
  private final IReceiver receiver;

  private final PacketListener leaveExtensionListener =
      new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
          leaveReceived(new JID(packet.getFrom()));
        }
      };

  private final PacketListener kickExtensionListener =
      new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
          kickReceived(new JID(packet.getFrom()));
        }
      };

  /**
   * Initialize a LeaveAndKickHandler.
   *
   * @param session the currently running session
   * @param sessionManager the session manager
   * @param receiver the receiver for listening to messages
   */
  public LeaveAndKickHandler(
      ISarosSession session, ISarosSessionManager sessionManager, IReceiver receiver) {
    this.session = session;
    this.sessionManager = sessionManager;
    this.receiver = receiver;
  }

  @Override
  public void start() {
    receiver.addPacketListener(
        leaveExtensionListener, LeaveSessionExtension.PROVIDER.getPacketFilter(session.getID()));

    receiver.addPacketListener(
        kickExtensionListener, KickUserExtension.PROVIDER.getPacketFilter(session.getID()));
  }

  @Override
  public void stop() {
    receiver.removePacketListener(leaveExtensionListener);
    receiver.removePacketListener(kickExtensionListener);
  }

  private void kickReceived(JID from) {
    final User user = session.getUser(from);

    if (user.equals(session.getLocalUser())) {
      LOG.warn("the local user cannot kick itself out of the session");
      return;
    }

    stopSession(user, SessionEndReason.KICKED);
  }

  private void leaveReceived(JID from) {
    final User user = session.getUser(from);
    if (user == null) {
      LOG.warn(
          "received leave message from user who" + " is not part of the current session: " + from);
      return;
    }

    if (user.isHost()) {
      stopSession(user, SessionEndReason.HOST_LEFT);
      return;
    }

    if (!session.isHost()) {
      LOG.warn(
          "Received leave message from user " + user + " who is not the current session's host");
      return;
    }

    /*
     * Must be run asynchronously. Otherwise the user list synchronization
     * will time out as we block the packet receiving thread here.
     */
    ThreadUtils.runSafeAsync(
        "dpp-remove-" + user,
        LOG,
        new Runnable() {
          @Override
          public void run() {
            session.removeUser(user);
          }
        });
  }

  private void stopSession(final User user, final SessionEndReason reason) {
    ThreadUtils.runSafeAsync(
        "dpp-stop-host",
        LOG,
        new Runnable() {
          @Override
          public void run() {
            sessionManager.stopSession(reason);
          }
        });
  }
}
