package de.fu_berlin.inf.dpp.net.business;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.log.LoggingUtils;

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

    protected ExecutorService activityDownloadThreadPool = Executors
        .newCachedThreadPool(new NamedThreadFactory(
            "ActivitiesHandler-receiver-"));

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
                            + ") " + Util.prefix(from)
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

        receiver.addPacketListener(
            new PacketListener() {

                public void processPacket(final Packet packet) {

                    activityDownloadThreadPool.execute(Util.wrapSafe(log,
                        new Runnable() {
                            public void run() {
                                receiveActivities(provider, incomingExtProv,
                                    packet);
                            }
                        }));
                }

            }, PacketExtensionUtils.getIncomingTransferObjectFilter(
                incomingExtProv, sessionID, null,
                FileTransferType.ACTIVITY_TRANSFER));
    }

    private void receiveActivities(final ActivitiesExtensionProvider provider,
        final IncomingTransferObjectExtensionProvider incomingExtProv,
        final Packet packet) {

        final IncomingTransferObject result = incomingExtProv
            .getPayload(packet);

        // TODO If long running show to user
        SubMonitor monitor = SubMonitor.convert(new NullProgressMonitor());

        byte[] data;
        try {
            data = result.accept(monitor);
        } catch (SarosCancellationException e) {
            log.error("User canceled. This is unexpected", e);
            return;
        } catch (IOException e) {
            log.error("Could not deserialize incoming "
                + "activityDataObjects or an connection error occurred", e);
            return;
        }

        final TimedActivities content;
        try {
            content = provider.parseString(IOUtils.toString(
                new ByteArrayInputStream(data), "UTF-8"));
        } catch (IOException e) {
            log.error("Could not parse incoming activityDataObjects:", e);
            return;
        }

        // When finished execute as dispatch
        dispatchThread.executeAsDispatch(new Runnable() {
            public void run() {
                receiveActivities(result.getTransferDescription().getSender(),
                    content.getTimedActivities());
            }
        });
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
                + ") " + Util.prefix(fromJID)
                + " but buddy is no participant: " + timedActivities);
            return;
        } else {
            String msg = "Rcvd ("
                + String.format("%03d", timedActivities.size()) + ") "
                + Util.prefix(fromJID) + ": " + timedActivities;

            // only log on debug level if there is more than a checksum
            if (LoggingUtils.containsChecksumsOnly(timedActivities))
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