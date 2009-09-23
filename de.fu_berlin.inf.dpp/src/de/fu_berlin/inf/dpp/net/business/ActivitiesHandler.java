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

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.UserCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Handler for all {@link TimedActivities}
 */
@Component(module = "net")
public class ActivitiesHandler {

    private static Logger log = Logger.getLogger(ActivitiesHandler.class
        .getName());

    @Inject
    protected SharedProjectObservable sharedProject;

    @Inject
    protected DispatchThreadContext dispatchThread;

    protected ExecutorService activityDownloadThreadPool = Executors
        .newCachedThreadPool(new NamedThreadFactory(
            "ActivitiesHandler-receiver-"));

    public ActivitiesHandler(XMPPChatReceiver receiver,
        final ActivitiesExtensionProvider provider,
        final IncomingTransferObjectExtensionProvider incomingExtProv,
        final SessionIDObservable sessionID) {

        /**
         * Add a PacketListener for all TimedActivity packets
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
                    List<TimedActivity> timedActivities = payload
                        .getTimedActivities();

                    if (!ObjectUtils.equals(sessionID.getValue(), payload
                        .getSessionID())) {
                        log
                            .warn("Rcvd ("
                                + String.format("%03d", timedActivities.size())
                                + ") " + Util.prefix(from)
                                + "from an old/unknown session: "
                                + timedActivities);
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

        receiver.addPacketListener(new PacketListener() {

            public void processPacket(final Packet packet) {

                activityDownloadThreadPool.execute(Util.wrapSafe(log,
                    new Runnable() {
                        public void run() {
                            receiveActivities(provider, incomingExtProv, packet);
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
        } catch (UserCancellationException e) {
            log.error("User canceled. This is unexpected", e);
            return;
        } catch (IOException e) {
            log.error("Could not deserialize incoming "
                + "activities or an connection error occurred", e);
            return;
        }

        final TimedActivities content;
        try {
            content = provider.parseString(IOUtils.toString(
                new ByteArrayInputStream(data), "UTF-8"));
        } catch (IOException e) {
            log.error("Could not parse incoming activities:", e);
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

        final ISharedProject project = sharedProject.getValue();

        if (project == null || project.getUser(fromJID) == null) {
            log.warn("Rcvd (" + String.format("%03d", timedActivities.size())
                + ") " + Util.prefix(fromJID) + " but User is no participant: "
                + timedActivities);
            return;
        } else {
            log.debug("Rcvd (" + String.format("%03d", timedActivities.size())
                + ") " + Util.prefix(fromJID) + ": " + timedActivities);
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