package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Business logic for handling Leave Message
 * 
 */
@Component(module = "net")
public class LeaveHandler {

    private static final Logger log = Logger.getLogger(LeaveHandler.class
        .getName());

    protected SarosSessionManager sessionManager;

    protected Handler handler;

    public LeaveHandler(SarosSessionManager sessionManager,
        XMPPReceiver receiver, SessionIDObservable sessionIDObservable) {

        this.sessionManager = sessionManager;
        this.handler = new Handler(sessionIDObservable);
        receiver.addPacketListener(handler, handler.getFilter());
    }

    protected class Handler extends LeaveExtension {

        public Handler(SessionIDObservable sessionID) {
            super(sessionID);
        }

        @Override
        public PacketFilter getFilter() {
            return new AndFilter(super.getFilter(),
                PacketExtensionUtils.getSessionIDPacketFilter(sessionID));
        }

        @Override
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

                SarosView.showNotification(
                    "Closing the session",
                    "Session was closed by inviter "
                        + user.getHumanReadableName() + ".");
            } else {
                // Client
                Utils.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        // FIXME see above...
                        sarosSession.removeUser(user);
                    }
                });
            }
        }
    }
}