/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.net.internal.IXMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class RequestForActivityHandler extends RequestActivityExtension {

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensions
            .getInSessionFilter());
    }

    protected IXMPPTransmitter transmitter;

    public RequestForActivityHandler(IXMPPTransmitter transmitter) {
        this.transmitter = transmitter;
    }

    private final Logger log = Logger.getLogger(RequestForActivityHandler.class
        .getName());

    @Override
    public void requestForResendingActivitiesReceived(JID fromJID,
        int timeStamp, boolean andUp) {

        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        if (project == null || project.getParticipant(fromJID) == null) {
            return;
        }

        List<TimedActivity> tempActivities = ActivitySequencer
            .filterActivityHistory(Saros.getDefault().getSessionManager()
                .getSharedProject().getSequencer().getActivityHistory(),
                timeStamp, andUp);

        if (tempActivities.size() > 0) {
            PacketExtension extension = new ActivitiesPacketExtension(Saros
                .getDefault().getSessionManager().getSessionID(),
                tempActivities);

            transmitter.sendMessage(fromJID, extension);
        }

        String info = String.format(
            "Received request for resending of timestamp%s %d%s.", andUp ? "s"
                : "", timeStamp, andUp ? " (andup)" : "");

        if (tempActivities.size() > 0) {
            info += String.format(" I sent back %s activities.", tempActivities
                .size());
        } else {
            info += String.format(" I did not find any matching activities.");
        }

        log.info(info);
    }
}