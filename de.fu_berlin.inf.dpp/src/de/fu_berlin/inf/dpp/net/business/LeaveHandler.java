/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.WarningMessageDialog;

/**
 * Business logic for handling Leave Message
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class LeaveHandler extends LeaveExtension {

    public LeaveHandler(XMPPChatReceiver receiver) {
        receiver.addPacketListener(this, this.getFilter());
    }

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensionUtils
            .getInSessionFilter());
    }

    @Override
    public void leaveReceived(JID fromJID) {

        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        if (project.getHost().getJID().equals(fromJID)) {
            // Host
            Saros.getDefault().getSessionManager().stopSharedProject();

            WarningMessageDialog.showWarningMessage("Closing the Session",
                "Closing the session because the host left.");
        } else {
            // Client
            project.removeUser(project.getParticipant(fromJID));
        }
    }
}