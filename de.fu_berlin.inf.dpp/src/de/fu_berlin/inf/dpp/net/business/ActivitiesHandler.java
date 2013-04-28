package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;
import de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ActivityUtils;

/**
 * Handler for all {@link TimedActivities}
 */

// FIMXE move to *project.internal package
// FIMXE this component uses the network but is not a net component !

// TODO maybe it is better to integrate this code into the ActivitySequencer
@Component(module = "net")
public class ActivitiesHandler implements Startable {

    private static final Logger LOG = Logger.getLogger(ActivitiesHandler.class
        .getName());

    private final SessionIDObservable sessionID;

    private final ISarosSession session;

    private final IReceiver receiver;

    private final ActivitySequencer sequencer;

    private String currentSessionID;

    private final PacketListener activitiesPacketListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            handleActivities(packet);
        }
    };

    public ActivitiesHandler(ISarosSession session,
        ActivitySequencer sequencer, IReceiver receiver,
        SessionIDObservable sessionID) {

        this.session = session;
        this.sequencer = sequencer;
        this.receiver = receiver;
        this.sessionID = sessionID;
    }

    @Override
    public void start() {
        currentSessionID = sessionID.getValue();
        // FIXME: sessionID filter
        receiver.addPacketListener(activitiesPacketListener,
            ActivitiesExtension.PROVIDER.getPacketFilter());

    }

    @Override
    public void stop() {
        receiver.removePacketListener(activitiesPacketListener);
    }

    private void handleActivities(Packet packet) {

        TimedActivities payload = ActivitiesExtension.PROVIDER
            .getPayload(packet);

        if (payload == null) {
            LOG.warn("activities packet payload is corrupted");
            return;
        }

        JID from = new JID(packet.getFrom());

        List<TimedActivityDataObject> timedActivities = payload
            .getTimedActivities();

        /*
         * FIXME the session.getUser() should not be handled here but in the
         * SarosSession class
         */
        if (!currentSessionID.equals(payload.getSessionID())
            || session.getUser(from) == null) {
            LOG.warn("received activities from user " + from
                + " who is not part of the current session");
            return;
        }

        String msg = "rcvd (" + String.format("%03d", timedActivities.size())
            + ") " + from + ": " + timedActivities;

        if (ActivityUtils.containsChecksumsOnly(timedActivities))
            LOG.trace(msg);
        else
            LOG.debug(msg);

        for (TimedActivityDataObject timedActivity : timedActivities) {

            assert timedActivity.getActivity().getSource() != null : "received activity without source"
                + timedActivity.getActivity();

            sequencer.exec(timedActivity);
        }
    }
}