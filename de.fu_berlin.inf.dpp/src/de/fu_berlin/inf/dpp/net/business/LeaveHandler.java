/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.WarningMessageDialog;

/**
 * Business logic for handling Leave Message
 */
public class LeaveHandler extends LeaveExtension {

    public LeaveHandler(XMPPReceiver receiver) {
        receiver.addPacketListener(this, this.getFilter());
    }

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensions
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
        } else {// Client
            project.removeUser(project.getParticipant(fromJID));
        }
    }
}