package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * Handler for all {@link ActivitiesPacketExtension}s
 */
@Component(module = "net")
public class ActivitiesHandler {

    private static Logger log = Logger.getLogger(ActivitiesHandler.class
        .getName());

    @Inject
    protected SharedProjectObservable sharedProject;

    public ActivitiesHandler(XMPPChatReceiver receiver,
        SessionIDObservable sessionID) {

        receiver.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                try {
                    Message message = (Message) packet;

                    JID fromJID = new JID(message.getFrom());

                    ActivitiesPacketExtension activitiesPacket = PacketExtensionUtils
                        .getActvitiesExtension(message);

                    if (activitiesPacket == null) {
                        return;
                    }
                    receiveActivities(fromJID, activitiesPacket.getActivities());

                } catch (Exception e) {
                    log.error(
                        "An internal error occurred while processing packets",
                        e);
                }
            }
        }, PacketExtensionUtils.getSessionIDPacketFilter(sessionID));
    }

    /**
     * This method is called from all the different transfer methods, when an
     * activity arrives. This method puts the activity into the
     * ActivitySequencer which will execute it.
     * 
     * @param fromJID
     *            The JID which sent these activities (the source in the
     *            activities might be different!)
     * @param timedActivities
     *            The received activities including sequence numbers.
     * 
     * @sarosThread must be called from the Dispatch Thread
     */
    public void receiveActivities(JID fromJID,
        List<TimedActivity> timedActivities) {
        String source = fromJID.toString();

        final ISharedProject project = sharedProject.getValue();

        if ((project == null) || (project.getUser(fromJID) == null)) {
            log.warn("Received activities from " + source
                + " but User is no participant: " + timedActivities);
            return;
        } else {
            log.debug("Rcvd [" + fromJID.getName() + "]: " + timedActivities);
        }

        for (TimedActivity timedActivity : timedActivities) {

            IActivity activity = timedActivity.getActivity();

            /*
             * Some activities save space in the message by not setting the
             * source and the XML parser needs to provide the source
             */
            assert activity.getSource() != null : "Received activity without source:"
                + activity;

            try {
                // Ask sequencer to execute or queue until missing activities
                // arrive
                project.getSequencer().exec(timedActivity);
            } catch (Exception e) {
                log.error("Internal error", e);
            }
        }
    }

}