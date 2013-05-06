/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ActivityUtils;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The ActivitySequencer is responsible for making sure that transformed
 * {@linkplain IActivityDataObject activities} are sent and received in the
 * right order.
 * 
 * @author rdjemili
 * @author coezbek
 * @author marrin
 */
public class ActivitySequencer implements Startable {

    private static final Logger LOG = Logger.getLogger(ActivitySequencer.class
        .getName());

    /**
     * Holder class containing the transformed {@linkplain IActivityDataObject
     * activity} and the recipients it should be send to.
     */
    private static class DataObjectQueueItem {

        public final List<User> recipients;
        public final IActivityDataObject activityDataObject;

        public DataObjectQueueItem(List<User> recipients,
            IActivityDataObject activityDataObject) {
            this.recipients = recipients;
            this.activityDataObject = activityDataObject;
        }

        public DataObjectQueueItem(User host,
            IActivityDataObject transformedActivity) {
            this(Collections.singletonList(host), transformedActivity);
        }
    }

    private final PacketListener activitiesPacketListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            receiveTimedActivities(packet);
        }
    };

    private final Runnable outgoingActivitiesQueueFlushTask = new Runnable() {

        @Override
        public void run() {
            try {
                boolean abort = false;
                while (!abort)
                    abort = flush();
            } catch (InterruptedException e) {
                LOG.error("Flush got interrupted.", e);
            }
        }

        private boolean flush() throws InterruptedException {

            List<DataObjectQueueItem> pendingActivities = new ArrayList<DataObjectQueueItem>(
                outgoingActivitiesQueue.size());

            pendingActivities.add(outgoingActivitiesQueue.take());

            // If there was more than one ADO waiting, get the rest now.
            outgoingActivitiesQueue.drainTo(pendingActivities);

            boolean abort = false;

            int idx = pendingActivities.indexOf(POISON);

            if (idx != -1) {
                abort = true;

                /*
                 * remove the poison pill and all activities after the pill
                 */
                while (pendingActivities.size() != idx)
                    pendingActivities.remove(idx);
            }

            Map<User, List<IActivityDataObject>> toSend = AutoHashMap
                .getListHashMap();

            for (DataObjectQueueItem item : pendingActivities) {
                for (User recipient : item.recipients) {
                    toSend.get(recipient).add(item.activityDataObject);
                }
            }

            for (Entry<User, List<IActivityDataObject>> e : toSend.entrySet()) {
                sendTimedActivities(
                    e.getKey(),
                    createTimedActivities(e.getKey(),
                        ActivityUtils.optimize(e.getValue())));
            }

            return abort;
        }
    };

    /** Special queue item that terminates the processing */
    private static final DataObjectQueueItem POISON = new DataObjectQueueItem(
        (User) null, null);

    /**
     * Queue (buffer) for outgoing transformed {@linkplain IActivityDataObject
     * activities} that needs to be send to remote session users.
     */
    private final BlockingQueue<DataObjectQueueItem> outgoingActivitiesQueue = new LinkedBlockingQueue<DataObjectQueueItem>();

    private final ActivityQueueManager activityQueueManager;

    private boolean started = false;

    private String currentSessionID;

    private Thread activitySendThread;

    private final ISarosSession sarosSession;

    private final SessionIDObservable sessionIDObservable;

    private final ITransmitter transmitter;

    private final IReceiver receiver;

    private final JID localJID;

    private final DispatchThreadContext dispatchThread;

    public ActivitySequencer(final ISarosSession sarosSession,
        final ITransmitter transmitter, final IReceiver receiver,
        final DispatchThreadContext threadContext,
        final SessionIDObservable sessionIDObservable) {

        this.dispatchThread = threadContext;
        this.sarosSession = sarosSession;
        this.transmitter = transmitter;
        this.receiver = receiver;
        this.sessionIDObservable = sessionIDObservable;

        this.localJID = sarosSession.getLocalUser().getJID();

        this.activityQueueManager = new ActivityQueueManager(localJID);
    }

    /**
     * Used by the unit test
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Starts the sequencer. After the sequencer is started activities can be
     * send and received.
     * 
     * @throws IllegalStateException
     *             if the sequencer is already started
     * 
     * @see #stop()
     */
    @Override
    public void start() {

        if (started)
            throw new IllegalStateException();

        currentSessionID = sessionIDObservable.getValue();
        // FIXME: sessionID filter
        receiver.addPacketListener(activitiesPacketListener,
            ActivitiesExtension.PROVIDER.getPacketFilter());

        started = true;

        activitySendThread = Utils.runSafeAsync("ActivitySender", LOG,
            outgoingActivitiesQueueFlushTask);
    }

    /**
     * Stops the sequencer. After the sequencer is stopped activities can no
     * longer be send and received.
     * 
     * @throws IllegalStateException
     *             if the sequencer is already stopped
     * @see #start()
     */
    @Override
    public void stop() {

        if (!started)
            throw new IllegalStateException();

        receiver.removePacketListener(activitiesPacketListener);

        /*
         * Try to poison the flush task using the "Poison Pill" as known from
         * the Java Concurrency in Practice book.
         */
        while (true) {
            try {
                outgoingActivitiesQueue.put(POISON);
                activitySendThread.join();
                break;
            } catch (InterruptedException e) {
                //
            }
        }

        activitySendThread = null;
        started = false;
    }

    private void executeActivity(TimedActivityDataObject activity) {

        assert activity != null;

        activityQueueManager.add(activity);

        if (!started) {
            LOG.debug("received activity but "
                + "ActivitySequencer has not yet been started: " + activity);
            return;
        }

        flushIncomingQueues();
    }

    /**
     * Removes all activities that can be executed now from the incoming
     * activities queues (buffers) and passes them to the upper layer (current
     * Saros Session).
     */
    private void flushIncomingQueues() {
        List<IActivityDataObject> serializedActivities = new ArrayList<IActivityDataObject>();
        for (TimedActivityDataObject timedActivity : activityQueueManager
            .removeActivities()) {
            serializedActivities.add(timedActivity.getActivity());
        }
        sarosSession.exec(serializedActivities);
    }

    /**
     * Sends an activity to the given recipients.
     */
    public void sendActivity(List<User> recipients,
        final IActivityDataObject activity) {

        /*
         * Short cut all messages directed at local user
         */

        ArrayList<User> toSendViaNetwork = new ArrayList<User>();
        for (User user : recipients) {

            if (!user.isLocal()) {
                toSendViaNetwork.add(user);
                continue;
            }

            LOG.trace("dispatching activity " + activity
                + " to the local user: " + user.getJID());

            dispatchThread.executeAsDispatch(new Runnable() {
                @Override
                public void run() {
                    sarosSession.exec(Collections.singletonList(activity));
                }
            });
        }

        if (toSendViaNetwork.isEmpty())
            return;

        // ActivitySender thread is flushing this queue
        outgoingActivitiesQueue.add(new DataObjectQueueItem(toSendViaNetwork,
            activity));
    }

    /**
     * Removes the queued activities for the given user.
     * 
     * @param user
     *            the user that left
     */
    public void userLeft(User user) {
        activityQueueManager.removeQueue(user.getJID());
    }

    private List<TimedActivityDataObject> createTimedActivities(User recipient,
        List<IActivityDataObject> activityDataObjects) {

        assert !activityDataObjects.contains(null) : "activity must not be null";

        JID recipientJID = recipient.getJID();

        List<TimedActivityDataObject> timedActivities = activityQueueManager
            .createTimedActivities(recipientJID, activityDataObjects);

        return timedActivities;
    }

    private void sendTimedActivities(User user,
        List<TimedActivityDataObject> timedActivities) {

        assert !user.isLocal() : "recipient must not be the local user";

        // FIXME SarosSession must handle this !
        if (!user.isInSarosSession()) {
            LOG.warn("activities for peer not in session are dropped.");
            return;
        }

        if (timedActivities.size() == 0)
            return;

        JID recipient = user.getJID();

        PacketExtension timedActivitiesPacket = ActivitiesExtension.PROVIDER
            .create(currentSessionID, timedActivities);

        String msg = "send (" + String.format("%03d", timedActivities.size())
            + ") " + Utils.prefix(recipient) + timedActivities;

        // only log on debug level if there is more than a checksum
        if (ActivityUtils.containsChecksumsOnly(timedActivities))
            LOG.trace(msg);
        else
            LOG.debug(msg);

        try {
            transmitter.sendToSessionUser(recipient, timedActivitiesPacket);
        } catch (IOException e) {
            LOG.error("failed to sent timed activities: " + timedActivities, e);
        }
    }

    private void receiveTimedActivities(Packet timedActivitiesPacket) {

        TimedActivities payload = ActivitiesExtension.PROVIDER
            .getPayload(timedActivitiesPacket);

        if (payload == null) {
            LOG.warn("timed activities packet payload is corrupted");
            return;
        }

        JID from = new JID(timedActivitiesPacket.getFrom());

        List<TimedActivityDataObject> timedActivities = payload
            .getTimedActivities();

        /*
         * FIXME the session.getUser() should not be handled here but in the
         * SarosSession class
         */
        if (!currentSessionID.equals(payload.getSessionID())
            || sarosSession.getUser(from) == null) {
            LOG.warn("received timed activities from user " + from
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

            executeActivity(timedActivity);
        }
    }
}
