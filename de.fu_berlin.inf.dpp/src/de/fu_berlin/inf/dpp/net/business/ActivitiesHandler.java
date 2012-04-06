package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IPacketListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;
import de.fu_berlin.inf.dpp.net.packet.Packet;
import de.fu_berlin.inf.dpp.net.packet.PacketType;
import de.fu_berlin.inf.dpp.net.packet.TimedActivitiesPacket;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ActivityUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Handler for all {@link TimedActivities}
 */
@Component(module = "net")
public class ActivitiesHandler implements IPacketListener {

    private static final Logger log = Logger.getLogger(ActivitiesHandler.class
        .getName());

    private SarosSessionObservable sarosSessionObservable;
    private SessionIDObservable sessionIDObervable;

    public ActivitiesHandler(DataTransferManager dataTransfermanager,
        SarosSessionObservable sarosSessionObservable,
        SessionIDObservable sessionIDObervable) {

        this.sarosSessionObservable = sarosSessionObservable;
        this.sessionIDObervable = sessionIDObervable;

        dataTransfermanager.getDispatcher().addPacketListener(this,
            PacketType.TIMED_ACTIVITIES);
    }

    @Override
    public void processPacket(Packet packet) {

        TimedActivities activites = ((TimedActivitiesPacket) packet)
            .getTimedActivities();

        JID from = packet.getSender();

        List<TimedActivityDataObject> timedActivities = activites
            .getTimedActivities();

        if (!activites.getSessionID().equals(sessionIDObervable.getValue())) {
            log.warn("Rcvd (" + String.format("%03d", timedActivities.size())
                + ") " + Utils.prefix(from) + "from an old/unknown session: "
                + timedActivities);
            return;
        }

        receiveActivities(from, timedActivities);
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
     */
    private void receiveActivities(JID fromJID,
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