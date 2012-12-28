package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.SarosLeaveExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;

/**
 * Business logic for handling Leave Message
 * 
 */

// FIXME move this class into the session context
@Component(module = "net")
public class LeaveHandler {

    private static final Logger log = Logger.getLogger(LeaveHandler.class
        .getName());

    private ISarosSessionManager sessionManager;

    private SessionIDObservable sessionIDObservable;

    private IReceiver receiver;
    private SarosLeaveExtension.Provider provider;

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            receiver.addPacketListener(leaveExtensionListener,
                provider.getPacketFilter(sessionIDObservable.getValue()));
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            receiver.removePacketListener(leaveExtensionListener);
        }
    };

    private PacketListener leaveExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            leaveReceived(new JID(packet.getFrom()));
        }
    };

    public LeaveHandler(IReceiver receiver,
        SarosLeaveExtension.Provider provider,
        ISarosSessionManager sessionManager,
        SessionIDObservable sessionIDObservable) {

        this.provider = provider;
        this.receiver = receiver;

        this.sessionManager = sessionManager;
        this.sessionIDObservable = sessionIDObservable;

        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    public void leaveReceived(JID fromJID) {

        final ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession == null) {
            log.warn("Received leave message but shared"
                + " project has already ended: " + fromJID);
            return;
        }

        final User user = sarosSession.getUser(fromJID);
        if (user == null) {
            log.warn("Received leave message from buddy who"
                + " is not part of our shared project session: " + fromJID);
            return;
        }

        // FIXME LeaveEvents need to be Activities, otherwise
        // RaceConditions can occur when two users leave a the "same" time

        if (user.isHost()) {
            sessionManager.stopSarosSession();

            SarosView.showNotification("Closing the session",
                "Session was closed by inviter " + user.getHumanReadableName()
                    + ".");
        } else {
            // Client
            SWTUtils.runSafeSWTSync(log, new Runnable() {
                @Override
                public void run() {
                    // FIXME see above...
                    sarosSession.removeUser(user);
                }
            });
        }
    }
}