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
package de.fu_berlin.inf.dpp.project.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ActivityUtils;
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
     * Sequence numbers for outgoing and incoming activity data objects start
     * with this value.
     */
    private static final int FIRST_SEQUENCE_NUMBER = 0;

    private static class SequencedActivity {
        private final int sequenceNumber;
        private final IActivityDataObject activity;

        private SequencedActivity(IActivityDataObject activity,
            int sequenceNumber) {
            this.activity = activity;
            this.sequenceNumber = sequenceNumber;
        }
    }

    private static class SequencedActivities {
        private final int sequenceNumber;
        private final List<IActivityDataObject> activites;

        private SequencedActivities(List<IActivityDataObject> activites,
            int sequenceNumber) {
            this.activites = activites;
            this.sequenceNumber = sequenceNumber;
        }
    }

    private static class ActivityBuffer<T> {
        /**
         * Helper flag to signal that there pending data is still send even if
         * the buffer is already empty.
         */
        private boolean isInTransmission;
        private int nextSequenceNumber;
        private final Deque<T> activities = new LinkedList<T>();

        private ActivityBuffer(int firstSequenceNumber) {
            nextSequenceNumber = firstSequenceNumber;
        }
    }

    private final PacketListener activitiesPacketListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            receiveActivities(packet);
        }
    };

    private final Runnable activitySender = new Runnable() {

        @Override
        public void run() {

            Map<JID, SequencedActivities> activitiesToSend = new HashMap<JID, SequencedActivities>();

            send: while (true) {
                activitiesToSend.clear();

                synchronized (bufferedOutgoingActivities) {
                    if (stopSending)
                        return;

                    for (Entry<JID, ActivityBuffer<IActivityDataObject>> entry : bufferedOutgoingActivities
                        .entrySet()) {

                        ActivityBuffer<IActivityDataObject> buffer = entry
                            .getValue();

                        if (buffer == null || buffer.activities.isEmpty())
                            continue;

                        List<IActivityDataObject> optimizedActivities = ActivityUtils
                            .optimize(buffer.activities);

                        buffer.activities.clear();
                        buffer.isInTransmission = true;

                        int currentSequenceNumber = buffer.nextSequenceNumber;
                        buffer.nextSequenceNumber += optimizedActivities.size();

                        activitiesToSend.put(entry.getKey(),
                            new SequencedActivities(optimizedActivities,
                                currentSequenceNumber));
                    }

                    if (activitiesToSend.isEmpty()) {
                        try {
                            bufferedOutgoingActivities.wait();
                            continue send;
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }

                for (Entry<JID, SequencedActivities> e : activitiesToSend
                    .entrySet()) {
                    sendActivities(e.getKey(), e.getValue().activites,
                        e.getValue().sequenceNumber);
                }

                synchronized (bufferedOutgoingActivities) {
                    for (Entry<JID, ActivityBuffer<IActivityDataObject>> entry : bufferedOutgoingActivities
                        .entrySet()) {

                        ActivityBuffer<IActivityDataObject> buffer = entry
                            .getValue();

                        if (buffer == null)
                            continue;

                        buffer.isInTransmission = false;
                    }

                    // notify waiting threads in flush method that we are done
                    bufferedOutgoingActivities.notifyAll();
                }
            }
        }
    };

    private volatile IActivitySequencerCallback callback;

    private boolean started = false;

    private boolean stopSending = false;

    private final String currentSessionID;

    private Thread activitySendThread;

    private final ISarosSession sarosSession;

    private final ITransmitter transmitter;

    private final IReceiver receiver;

    private final DispatchThreadContext dispatchThread;

    private final Map<JID, ActivityBuffer<SequencedActivity>> bufferedIncomingActivities;

    private final Map<JID, ActivityBuffer<IActivityDataObject>> bufferedOutgoingActivities;

    public ActivitySequencer(final ISarosSession sarosSession,
        final ITransmitter transmitter, final IReceiver receiver,
        final DispatchThreadContext threadContext) {

        this.dispatchThread = threadContext;
        this.sarosSession = sarosSession;
        this.transmitter = transmitter;
        this.receiver = receiver;
        this.currentSessionID = sarosSession.getID();

        this.bufferedIncomingActivities = new HashMap<JID, ActivityBuffer<SequencedActivity>>();
        this.bufferedOutgoingActivities = new HashMap<JID, ActivityBuffer<IActivityDataObject>>();
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
            throw new IllegalStateException("sequencer is already started");

        /* *
         * 
         * @JTourBusStop 8, Creating custom network messages, Receiving custom
         * messages - Part 1:
         * 
         * In order to receive custom messages you must install a packet filter
         * to the current implementation of the IReceiver interface.
         * 
         * Remember the provider you created in step 4 ? It will offer you the
         * filter you need.
         * 
         * Please be AWARE that due to inheritance it will offer you MULTIPLE
         * filters. You normally want to use the filter that needed the most
         * arguments. Failing to use the proper filter may let you process
         * messages that you did not wanted to be aware of this behavior !
         * 
         * IMPORTANT: You are ALLOWED to send data during a listener callback
         * although you should avoid it because processing custom message should
         * not block.
         * 
         * Furthermore if you try to send data and then wait for another reply
         * inside the listener callback (e.g with a collector) you can wait
         * FOREVER because you are blocking the thread context in which messages
         * will be dispatched and so it is likely to CRASH the whole
         * application. Please do not do that, you were warned here !
         */

        receiver.addPacketListener(activitiesPacketListener,
            ActivitiesExtension.PROVIDER.getPacketFilter(currentSessionID));

        started = true;

        activitySendThread = Utils.runSafeAsync("ActivitySender", LOG,
            activitySender);
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
            throw new IllegalStateException("sequencer is not started");

        receiver.removePacketListener(activitiesPacketListener);

        while (true) {
            try {
                synchronized (bufferedOutgoingActivities) {
                    stopSending = true;
                    bufferedOutgoingActivities.notifyAll();
                }
                activitySendThread.join();
                break;
            } catch (InterruptedException e) {
                //
            }
        }

        synchronized (bufferedOutgoingActivities) {
            bufferedOutgoingActivities.clear();
            bufferedOutgoingActivities.notifyAll();
        }

        synchronized (bufferedIncomingActivities) {
            bufferedIncomingActivities.clear();
        }

        stopSending = false;
        activitySendThread = null;
        started = false;
    }

    public void setCallback(IActivitySequencerCallback callback) {
        this.callback = callback;
    }

    /*
     * TODO some part of the logic can be removed if we ensure only ONE transmit
     * stream and fail FAST if this stream is broken
     */
    private void executeActivity(JID sender, SequencedActivity sequencedActivity) {

        assert sequencedActivity != null;

        List<IActivityDataObject> serializedActivities = new ArrayList<IActivityDataObject>();

        synchronized (this) {
            ActivityBuffer<SequencedActivity> buffer = bufferedIncomingActivities
                .get(sender);

            if (buffer == null) {
                LOG.warn("dropping received activity from "
                    + sender
                    + " because it is currently not registers, dropped activity: "
                    + sequencedActivity.activity);
                return;
            }

            buffer.activities.add(sequencedActivity);

            /*
             * it is very VERY uncommon to receive an activity with a sequence
             * number that is 2^32 steps apart from the current expected
             * sequencer number, so this algorithm does not check for duplicate
             * sequence numbers
             */
            while (true) {

                sequencedActivity = null;

                for (Iterator<SequencedActivity> it = buffer.activities
                    .iterator(); it.hasNext();) {

                    sequencedActivity = it.next();

                    if (sequencedActivity.sequenceNumber == buffer.nextSequenceNumber) {
                        it.remove();
                        break;
                    }

                    sequencedActivity = null;
                }

                if (sequencedActivity == null) {
                    /*
                     * TODO shut down the session if a activity does not arrive
                     * in a given timeout
                     */
                    break;
                }

                serializedActivities.add(sequencedActivity.activity);
                buffer.nextSequenceNumber++;
            }
        }

        if (!serializedActivities.isEmpty())
            sarosSession.exec(serializedActivities);
    }

    /**
     * Sends an activity to the given recipients.
     */
    public void sendActivity(List<User> recipients,
        final IActivityDataObject activity) {

        ArrayList<User> remoteRecipients = new ArrayList<User>();
        for (User user : recipients) {

            if (!user.isLocal()) {
                remoteRecipients.add(user);
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

        if (remoteRecipients.isEmpty())
            return;

        synchronized (bufferedOutgoingActivities) {
            for (User recipient : remoteRecipients) {
                ActivityBuffer<IActivityDataObject> buffer = bufferedOutgoingActivities
                    .get(recipient.getJID());

                if (buffer == null) {
                    LOG.warn("cannot send activity to "
                        + recipient
                        + " because it is currently not registers, dropped activity: "
                        + activity);
                    continue;
                }
                buffer.activities.add(activity);
            }

            // ActivitySender thread is flushing the buffers
            bufferedOutgoingActivities.notifyAll();
        }
    }

    /**
     * Registers a user with the sequencer allowing the sending and receiving to
     * and from this user. The local user of a session does not need to be
     * registered as it is always allowed to send activities to the local user
     * itself.
     * 
     * @param user
     */
    public void registerUser(User user) {
        synchronized (bufferedOutgoingActivities) {
            if (bufferedOutgoingActivities.get(user.getJID()) == null)
                bufferedOutgoingActivities.put(user.getJID(),
                    new ActivityBuffer<IActivityDataObject>(
                        FIRST_SEQUENCE_NUMBER));
        }

        synchronized (bufferedIncomingActivities) {
            if (bufferedIncomingActivities.get(user.getJID()) == null)
                bufferedIncomingActivities
                    .put(user.getJID(), new ActivityBuffer<SequencedActivity>(
                        FIRST_SEQUENCE_NUMBER));
        }
    }

    /**
     * Unregisters a user from the sequencer disallowing the sending and
     * receiving to and from this user.
     * 
     * @param user
     */
    public void unregisterUser(User user) {
        unregisterUser(user.getJID());
    }

    /**
     * Waits until all buffered activities for the specific user are sent.
     * Calling {@link #sendActivity} at the same time may or may not ignore
     * those new activities.
     * 
     * @param user
     */
    public void flush(User user) {

        synchronized (bufferedOutgoingActivities) {
            while (true) {
                ActivityBuffer<IActivityDataObject> buffer = bufferedOutgoingActivities
                    .get(user.getJID());

                if (buffer == null
                    || (buffer.activities.size() == 0 && !buffer.isInTransmission))
                    break;

                try {
                    bufferedOutgoingActivities.wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private void unregisterUser(JID jid) {
        /*
         * FIXME This stuff is to lazy if called outside the UI-Thread as it is
         * possible that activities may be still send or received. FIX: Ensure
         * proper synchronization and discard incoming or outgoing activities if
         * the user is not present.
         */

        synchronized (bufferedOutgoingActivities) {
            bufferedOutgoingActivities.put(jid, null);
            bufferedOutgoingActivities.notifyAll();
        }

        synchronized (bufferedIncomingActivities) {
            bufferedIncomingActivities.put(jid, null);
        }
    }

    private void sendActivities(JID recipient,
        List<IActivityDataObject> activities, int sequenceNumber) {

        if (activities.size() == 0)
            return;

        PacketExtension activityPacketExtension = ActivitiesExtension.PROVIDER
            .create(new ActivitiesExtension(currentSessionID, activities,
                sequenceNumber));

        String msg = "send (" + String.format("%03d", activities.size()) + ") "
            + Utils.prefix(recipient) + activities;

        // only log on debug level if there is more than a checksum
        if (ActivityUtils.containsChecksumsOnly(activities))
            LOG.trace(msg);
        else
            LOG.debug(msg);

        try {
            transmitter.sendToSessionUser(ISarosSession.SESSION_CONNECTION_ID,
                recipient, activityPacketExtension);
        } catch (IOException e) {
            LOG.error("failed to sent activities: " + activities, e);
            /*
             * as our "wonderful" networklayer will try to establish a new
             * connection (sometimes forever) we will "shutdown" the user here
             */
            unregisterUser(recipient);

            IActivitySequencerCallback currentCallback = callback;

            if (currentCallback != null)
                currentCallback.transmissionFailed(recipient);
        }
    }

    private void receiveActivities(Packet activityPacket) {

        /* *
         * 
         * @JTourBusStop 10, Creating custom network messages, Accessing the
         * data of custom messages:
         * 
         * In order to access the data from a received custom message (packet)
         * you must unmarshall it. As you might already have guessed
         * unmarshalling is also provided by the provider. As you see below you
         * just have to call getPayload on the packet which includes your
         * marshalled data in the packet extension of that packet.
         * 
         * Please note the null check is not really needed as we should ensure
         * that it cannot happen that you receive malformed data. Never less it
         * does not matter and is a good practice as it avoids further
         * exceptions which may be hard to analyze.
         */

        ActivitiesExtension payload = ActivitiesExtension.PROVIDER
            .getPayload(activityPacket);

        if (payload == null) {
            LOG.warn("activity packet payload is corrupted");
            return;
        }

        JID from = new JID(activityPacket.getFrom());

        List<IActivityDataObject> activities = payload.getActivityDataObjects();

        String msg = "rcvd (" + String.format("%03d", activities.size()) + ") "
            + from + ": " + activities;

        if (ActivityUtils.containsChecksumsOnly(activities))
            LOG.trace(msg);
        else
            LOG.debug(msg);

        int sequenceNumber = payload.getSequenceNumber();

        for (IActivityDataObject activity : activities) {

            assert activity.getSource() != null : "received activity without source"
                + activity;

            executeActivity(from, new SequencedActivity(activity,
                sequenceNumber++));
        }
    }

    /**
     * For testing purposes only.
     * 
     * @param user
     * @return
     */
    boolean isUserRegistered(User user) {
        synchronized (bufferedOutgoingActivities) {
            return bufferedOutgoingActivities.get(user.getJID()) != null;
        }
    }
}
