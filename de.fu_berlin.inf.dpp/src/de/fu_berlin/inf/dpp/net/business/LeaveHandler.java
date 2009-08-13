package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.WarningMessageDialog;

/**
 * Business logic for handling Leave Message
 * 
 */
@Component(module = "net")
public class LeaveHandler {

    private static final Logger log = Logger.getLogger(LeaveHandler.class
        .getName());

    protected SessionManager sessionManager;

    protected Handler handler;

    public LeaveHandler(SessionManager sessionManager,
        XMPPChatReceiver receiver, SessionIDObservable sessionIDObservable) {

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
            return new AndFilter(super.getFilter(), PacketExtensionUtils
                .getSessionIDPacketFilter(sessionID));
        }

        @Override
        public void leaveReceived(JID fromJID) {

            ISharedProject project = sessionManager.getSharedProject();

            User user = project.getUser(fromJID);
            if (user == null) {
                log.warn("Received leave message from user which"
                    + " is not part of our shared project session: " + fromJID);
                return;
            }

            // FIXME LeaveEvents need to be Activities, otherwise
            // RaceConditions can occur when two users leave a the "same" time

            if (user.isHost()) {
                sessionManager.stopSharedProject();

                WarningMessageDialog.showWarningMessage("Closing the Session",
                    "Closing the session because the host left.");
            } else {
                // Client
                project.removeUser(user);
            }
        }
    }
}