package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.WarningMessageDialog;

/**
 * Business logic for handling Leave Message
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class LeaveHandler extends LeaveExtension {

    private static final Logger log = Logger.getLogger(LeaveHandler.class
        .getName());

    protected SessionManager sessionManager;

    public LeaveHandler(SessionManager sessionManager, XMPPChatReceiver receiver) {
        this.sessionManager = sessionManager;
        receiver.addPacketListener(this, this.getFilter());
    }

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensionUtils
            .getInSessionFilter(sessionManager));
    }

    @Override
    public void leaveReceived(JID fromJID) {

        ISharedProject project = sessionManager.getSharedProject();

        User user = project.getParticipant(fromJID);

        if (user == null) {
            log.warn("Received leave Message from user which"
                + " is not part of our shared project session: " + fromJID);
            return;
        }

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