package de.fu_berlin.inf.dpp.core.net.business;

import de.fu_berlin.inf.dpp.communication.extensions.KickUserExtension;
import de.fu_berlin.inf.dpp.communication.extensions.LeaveSessionExtension;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.NullSarosSessionListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * Business logic for handling Leave Message
 */
public class LeaveAndKickHandler {

    private static final Logger log = Logger
        .getLogger(LeaveAndKickHandler.class.getName());

    private final ISarosSessionManager sessionManager;

    private final IReceiver receiver;
    private final PacketListener leaveExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            leaveReceived(new JID(packet.getFrom()));
        }
    };
    private final PacketListener kickExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            kickReceived(new JID(packet.getFrom()));
        }
    };
    private final ISarosSessionListener sessionListener = new NullSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            receiver.addPacketListener(leaveExtensionListener,
                LeaveSessionExtension.PROVIDER.getPacketFilter(session.getID())
            );

            receiver.addPacketListener(kickExtensionListener,
                KickUserExtension.PROVIDER.getPacketFilter(session.getID()));
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            receiver.removePacketListener(leaveExtensionListener);
            receiver.removePacketListener(kickExtensionListener);
        }
    };

    public LeaveAndKickHandler(IReceiver receiver,
        ISarosSessionManager sessionManager) {

        this.receiver = receiver;

        this.sessionManager = sessionManager;

        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    private void kickReceived(JID from) {
        final ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession == null)
            return;

        final User user = sarosSession.getUser(from);

        if (user.equals(sarosSession.getLocalUser())) {
            log.warn("the local user cannot kick itself out of the session");
            return;
        }

        stopSession(sarosSession, "Removed from the session",
            user.getNickname() + " removed you from the current session.");
    }

    private void leaveReceived(JID from) {

        final ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession == null) {
            log.warn("Received leave message but shared"
                + " project has already ended: " + from);
            return;
        }

        final User user = sarosSession.getUser(from);
        if (user == null) {
            log.warn("received leave message from user who"
                + " is not part of the current session: " + from);
            return;
        }

        /*
         * FIXME LeaveEvents need to be Activities, otherwise RaceConditions can
         * occur when two users leave a the "same" time
         * 
         * srossbach: it is not possible that multiple users can leave at the
         * same time because this code is executed by the dispatch thread
         * context which executes all incoming packets sequentially
         */
        if (user.isHost()) {
            stopSession(sarosSession, "Closing the session",
                "Session was closed by inviter " + user.getNickname() + ".");

        }

        // host will send us an update
        if (!sarosSession.isHost()) {
            log.warn("received leave message from user " + user
                + " which is not the host of the current session");
            return;
        }

        /*
         * must be run async. otherwise the user list synchronization will time
         * out as we block the packet receive thread here
         */
        ThreadUtils.runSafeAsync("RemoveUser-" + user, log, new Runnable() {
            @Override
            public void run() {
                sarosSession.removeUser(user);
            }
        });

    }

    private void stopSession(final ISarosSession session, final String topic,
        final String reason) {
        ThreadUtils.runSafeAsync("StopSessionOnHostLeave", log, new Runnable() {
            @Override
            public void run() {
                sessionManager.stopSarosSession();
                NotificationPanel.showNotification(topic, reason);
            }
        });
    }
}