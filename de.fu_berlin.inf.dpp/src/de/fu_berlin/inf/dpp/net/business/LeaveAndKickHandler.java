package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.KickUserExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.SarosLeaveExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Business logic for handling Leave Message
 * 
 */

// FIXME move this class into the session context
@Component(module = "net")
public class LeaveAndKickHandler {

    private static final Logger log = Logger
        .getLogger(LeaveAndKickHandler.class.getName());

    private final UISynchronizer synchronizer;

    private final ISarosSessionManager sessionManager;

    private final SessionIDObservable sessionIDObservable;

    private final IReceiver receiver;

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            receiver.addPacketListener(leaveExtensionListener,
                SarosLeaveExtension.PROVIDER
                    .getPacketFilter(sessionIDObservable.getValue()));

            receiver.addPacketListener(kickExtensionListener,
                KickUserExtension.PROVIDER.getPacketFilter(sessionIDObservable
                    .getValue()));
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            receiver.removePacketListener(leaveExtensionListener);
            receiver.removePacketListener(kickExtensionListener);
        }
    };

    private PacketListener leaveExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            leaveReceived(new JID(packet.getFrom()));
        }
    };

    private PacketListener kickExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            kickReceived(new JID(packet.getFrom()));
        }
    };

    public LeaveAndKickHandler(IReceiver receiver,
        ISarosSessionManager sessionManager,
        SessionIDObservable sessionIDObservable, UISynchronizer synchronizer) {

        this.receiver = receiver;

        this.sessionManager = sessionManager;
        this.sessionIDObservable = sessionIDObservable;

        this.synchronizer = synchronizer;

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
            user.getHumanReadableName()
                + " removed you from the current session.");
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
            log.warn("Received leave message from buddy who"
                + " is not part of our shared project session: " + from);
            return;
        }

        /*
         * FIXME LeaveEvents need to be Activities, otherwise RaceConditions can
         * occur when two users leave a the "same" time srossbach: it is not
         * possible that multiple users can leave at the same time because this
         * code is executed by the dispatch thread context which executes all
         * incoming packets sequentially
         */
        if (user.isHost()) {
            stopSession(sarosSession, "Closing the session",
                "Session was closed by inviter " + user.getHumanReadableName()
                    + ".");

        } else {
            synchronizer.asyncExec(Utils.wrapSafe(log, new Runnable() {
                @Override
                public void run() {
                    sarosSession.removeUser(user);
                }
            }));
        }
    }

    // FIXME the session should handle the synchronization
    private void stopSession(final ISarosSession session, final String topic,
        final String reason) {
        synchronizer.asyncExec(Utils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                List<User> currentRemoteSessionUsers = session.getRemoteUsers();

                /*
                 * remove all users so we do not send leave messages as the
                 * other users already receive the message from the host
                 */
                for (User remoteSessionUser : currentRemoteSessionUsers)
                    session.removeUser(remoteSessionUser);

                Utils.runSafeAsync("LeaveSession", log, new Runnable() {
                    @Override
                    public void run() {
                        sessionManager.stopSarosSession();

                        SarosView.showNotification(topic, reason);
                    }
                });
            }
        }));
    }
}