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
import de.fu_berlin.inf.dpp.net.internal.IXMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * This class is responsible for parsing RequestForActivities and sending the
 * requested activities back.
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class RequestForActivityHandler extends RequestActivityExtension {

    @Inject
    protected IXMPPTransmitter transmitter;

    @Inject
    protected SessionIDObservable sessionID;

    protected SessionManager sessionManager;

    public RequestForActivityHandler(SessionManager sessionManager,
        XMPPChatReceiver receiver) {
        this.sessionManager = sessionManager;
        receiver.addPacketListener(this, this.getFilter());
    }

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensionUtils
            .getInSessionFilter(sessionManager));
    }

    private final Logger log = Logger.getLogger(RequestForActivityHandler.class
        .getName());

    @Override
    public void requestForResendingActivitiesReceived(JID fromJID,
        int sequenceNumber, boolean andUp) {

        ISharedProject sharedProject = sessionManager.getSharedProject();

        if (sharedProject == null
            || sharedProject.getParticipant(fromJID) == null) {
            return;
        }

        List<TimedActivity> activities = sharedProject.getSequencer()
            .getActivityHistory(fromJID, sequenceNumber, andUp);

        log.info(String.format(
            "Received request for resending of timestamp%s %d%s.", andUp ? "s"
                : "", sequenceNumber, andUp ? " (andup)" : ""));

        if (activities.size() > 0) {
            PacketExtension extension = new ActivitiesPacketExtension(sessionID
                .getValue(), activities);

            transmitter.sendMessage(fromJID, extension);
            log.info("I sent back " + activities.size() + " activities.");
        } else {
            log.error("No matching activities found");
        }
    }
}