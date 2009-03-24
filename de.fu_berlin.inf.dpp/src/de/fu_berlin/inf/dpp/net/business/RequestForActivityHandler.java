/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.IXMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class RequestForActivityHandler extends RequestActivityExtension {

    @Inject
    protected IXMPPTransmitter transmitter;

    public RequestForActivityHandler(XMPPChatReceiver receiver) {
        receiver.addPacketListener(this, this.getFilter());
    }

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensions
            .getInSessionFilter());
    }

    private final Logger log = Logger.getLogger(RequestForActivityHandler.class
        .getName());

    @Override
    public void requestForResendingActivitiesReceived(JID fromJID,
        int timeStamp, boolean andUp) {

        ISessionManager sessionManager = Saros.getDefault().getSessionManager();
        ISharedProject sharedProject = sessionManager.getSharedProject();

        if (sharedProject == null
            || sharedProject.getParticipant(fromJID) == null) {
            return;
        }

        List<TimedActivity> activities = sharedProject.getSequencer()
            .getActivityHistory(fromJID, timeStamp, andUp);

        if (activities.size() > 0) {
            PacketExtension extension = new ActivitiesPacketExtension(
                sessionManager.getSessionID(), activities);

            transmitter.sendMessage(fromJID, extension);
        }

        String info = String.format(
            "Received request for resending of timestamp%s %d%s.", andUp ? "s"
                : "", timeStamp, andUp ? " (andup)" : "");

        if (activities.size() > 0) {
            info += String.format(" I sent back %s activities.", activities
                .size());
        } else {
            info += String.format(" I did not find any matching activities.");
        }

        log.info(info);
    }
}