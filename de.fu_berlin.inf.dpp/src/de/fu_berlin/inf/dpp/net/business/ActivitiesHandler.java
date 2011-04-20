package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ActivityUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Handler for all {@link TimedActivities}
 */
@Component(module = "net")
public class ActivitiesHandler {

    private static Logger log = Logger.getLogger(ActivitiesHandler.class
        .getName());

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    @Inject
    protected DispatchThreadContext dispatchThread;

    public ActivitiesHandler(XMPPReceiver receiver,
        final ActivitiesExtensionProvider provider,
        final IncomingTransferObjectExtensionProvider incomingExtProv,
        final SessionIDObservable sessionID) {

        /**
         * Add a PacketListener for all TimedActivityDataObject packets
         */
        receiver.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                try {
                    TimedActivities payload = provider.getPayload(packet);
                    if (payload == null) {
                        log.warn("Invalid ActivitiesExtensionPacket"
                            + " does not contain a payload: " + packet);
                        return;
                    }
                    JID from = new JID(packet.getFrom());
                    List<TimedActivityDataObject> timedActivities = payload
                        .getTimedActivities();

                    if (!ObjectUtils.equals(sessionID.getValue(),
                        payload.getSessionID())) {
                        log.warn("Rcvd ("
                            + String.format("%03d", timedActivities.size())
                            + ") " + Utils.prefix(from)
                            + "from an old/unknown session: " + timedActivities);
                        return;
                    }

                    receiveActivities(from, timedActivities);

                } catch (Exception e) {
                    log.error(
                        "An internal error occurred while processing packets",
                        e);
                }
            }
        }, provider.getPacketFilter());
    }

    /**
     * This method is called from all the different transfer methods, when an
     * activityDataObject arrives. This method puts the activityDataObject into
     * the ActivitySequencer which will execute it.
     * 
     * @param fromJID
     *            The JID which sent these activityDataObjects (the source in
     *            the activityDataObjects might be different!)
     * @param timedActivities
     *            The received activityDataObjects including sequence numbers.
     * 
     * @sarosThread must be called from the Dispatch Thread
     */
    public void receiveActivities(JID fromJID,
        List<TimedActivityDataObject> timedActivities) {

        final ISarosSession session = sarosSessionObservable.getValue();

        if (session == null || session.getUser(fromJID) == null) {
            log.warn("Rcvd (" + String.format("%03d", timedActivities.size())
                + ") " + Utils.prefix(fromJID)
                + " but buddy is no participant: " + timedActivities);
            return;
        } else {
            String msg = "Rcvd ("
                + String.format("%03d", timedActivities.size()) + ") "
                + Utils.prefix(fromJID) + ": " + timedActivities;

            // only log on debug level if there is more than a checksum
            if (ActivityUtils.containsChecksumsOnly(timedActivities))
                log.trace(msg);
            else
                log.debug(msg);
        }

        for (TimedActivityDataObject timedActivity : timedActivities) {

            IActivityDataObject activityDataObject = timedActivity
                .getActivity();

            /*
             * Some activityDataObjects save space in the message by not setting
             * the source and the XML parser needs to provide the source
             */
            assert activityDataObject.getSource() != null : "Received activityDataObject without source:"
                + activityDataObject;

            try {
                // Ask sequencer to execute or queue until missing
                // activityDataObjects
                // arrive
                session.getSequencer().exec(timedActivity);
            } catch (Exception e) {
                log.error("Internal error", e);
            }
        }
    }

}