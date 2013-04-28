package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;
import de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ActivityUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Handler for all {@link TimedActivities}
 */

// FIXME: move to session scope
@Component(module = "net")
public class ActivitiesHandler {

    private static final Logger LOG = Logger.getLogger(ActivitiesHandler.class
        .getName());

    private final SarosSessionObservable sarosSessionObservable;

    private final SessionIDObservable sessionID;

    public ActivitiesHandler(IReceiver receiver,
        SarosSessionObservable sarosSessionObservable,
        SessionIDObservable sessionID) {

        this.sarosSessionObservable = sarosSessionObservable;
        this.sessionID = sessionID;

        /**
         * Add a PacketListener for all TimedActivityDataObject packets
         */
        receiver.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                receiveActivities(new JID(packet.getFrom()), packet);
            }
        }, ActivitiesExtension.PROVIDER.getPacketFilter());
    }

    private void receiveActivities(JID fromJID, Packet packet) {

        TimedActivities payload = ActivitiesExtension.PROVIDER
            .getPayload(packet);
        if (payload == null) {
            LOG.warn("Invalid ActivitiesExtensionPacket"
                + " does not contain a payload: " + packet);
            return;
        }
        JID from = new JID(packet.getFrom());
        List<TimedActivityDataObject> timedActivities = payload
            .getTimedActivities();

        if (!ObjectUtils.equals(sessionID.getValue(), payload.getSessionID())) {
            LOG.warn("rcvd (" + String.format("%03d", timedActivities.size())
                + ") " + Utils.prefix(from) + "from an old/unknown session: "
                + timedActivities);
            return;
        }

        final ISarosSession session = sarosSessionObservable.getValue();

        if (session == null || session.getUser(fromJID) == null) {
            LOG.warn("rcvd (" + String.format("%03d", timedActivities.size())
                + ") " + Utils.prefix(fromJID)
                + " but user is no participant: " + timedActivities);
            return;
        } else {
            String msg = "rcvd ("
                + String.format("%03d", timedActivities.size()) + ") "
                + Utils.prefix(fromJID) + ": " + timedActivities;

            if (ActivityUtils.containsChecksumsOnly(timedActivities))
                LOG.trace(msg);
            else
                LOG.debug(msg);
        }

        for (TimedActivityDataObject timedActivity : timedActivities) {

            assert timedActivity.getActivity().getSource() != null : "Received activity  without source"
                + timedActivity.getActivity();

            session.getSequencer().exec(timedActivity);
        }
    }

}