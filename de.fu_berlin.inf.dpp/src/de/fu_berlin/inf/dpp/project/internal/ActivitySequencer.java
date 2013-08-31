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
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;
import de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
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

    private static class ActivityBuffer<T> {
        /**
         * Helper flag to signal that there pending data is still send even if
         * the buffer is already empty.
         */
        private boolean isInTransmission;
        private int nextSequenceNumber;
        private final Deque<T> activities = new LinkedList<T>();

        public ActivityBuffer(int firstSequenceNumber) {
            nextSequenceNumber = firstSequenceNumber;
        }
    }

    private final PacketListener activitiesPacketListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            receiveTimedActivities(packet);
        }
    };

    private final Runnable activitySender = new Runnable() {

        @Override
        public void run() {

            Map<JID, List<TimedActivityDataObject>> activitiesToSend = new HashMap<JID, List<TimedActivityDataObject>>();

            send: while (true) {
                if (stopSending)
                    return;

                activitiesToSend.clear();

                synchronized (bufferedOutgoingActivities) {
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
                        activitiesToSend.put(
                            entry.getKey(),
                            createTimedActivities(optimizedActivities,
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

                for (Entry<JID, List<TimedActivityDataObject>> e : activitiesToSend
                    .entrySet()) {
                    sendTimedActivities(e.getKey(), e.getValue());
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

    private boolean started = false;

    private volatile boolean stopSending = false;

    private String currentSessionID;

    private Thread activitySendThread;

    private final ISarosSession sarosSession;

    private final SessionIDObservable sessionIDObservable;

    private final ITransmitter transmitter;

    private final IReceiver receiver;

    private final JID localJID;

    private final DispatchThreadContext dispatchThread;

    private final Map<JID, ActivityBuffer<TimedActivityDataObject>> bufferedIncomingActivities;

    private final Map<JID, ActivityBuffer<IActivityDataObject>> bufferedOutgoingActivities;

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

        this.bufferedIncomingActivities = new HashMap<JID, ActivityBuffer<TimedActivityDataObject>>();
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

        currentSessionID = sessionIDObservable.getValue();
        // FIXME: sessionID filter
        receiver.addPacketListener(activitiesPacketListener,
            ActivitiesExtension.PROVIDER.getPacketFilter());

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
            stopSending = true;

            try {
                synchronized (bufferedOutgoingActivities) {
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

    /*
     * TODO some part of the logic can be removed if we ensure only ONE transmit
     * stream and fail FAST if this stream is broken
     */
    private void executeActivity(TimedActivityDataObject activity) {

        assert activity != null;

        List<IActivityDataObject> serializedActivities = new ArrayList<IActivityDataObject>();

        JID sender = activity.getSender();

        synchronized (this) {
            ActivityBuffer<TimedActivityDataObject> buffer = bufferedIncomingActivities
                .get(sender);

            if (buffer == null) {
                LOG.warn("dropping received activity from "
                    + sender
                    + " because it is currently not registers, dropped activity: "
                    + activity);
                return;
            }

            buffer.activities.add(activity);

            /*
             * it is very VERY uncommon to receive an activity with a sequence
             * number that is 2^32 steps apart from the current expected
             * sequencer number, so this algorithm does not check for duplicate
             * sequence numbers
             */
            while (true) {

                activity = null;

                for (Iterator<TimedActivityDataObject> it = buffer.activities
                    .iterator(); it.hasNext();) {

                    activity = it.next();

                    if (activity.getSequenceNumber() == buffer.nextSequenceNumber) {
                        it.remove();
                        break;
                    }

                    activity = null;
                }

                if (activity == null) {
                    /*
                     * TODO shut down the session if a activity does not arrive
                     * in a given timeout
                     */
                    break;
                }

                serializedActivities.add(activity.getActivity());
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
                bufferedIncomingActivities.put(user.getJID(),
                    new ActivityBuffer<TimedActivityDataObject>(
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
        /*
         * FIXME This stuff is to lazy if called outside the UI-Thread as it is
         * possible that activities may be still send or received. FIX: Ensure
         * proper synchronization and discard incoming or outgoing activities if
         * the user is not present.
         */

        synchronized (bufferedOutgoingActivities) {
            bufferedOutgoingActivities.put(user.getJID(), null);
            bufferedOutgoingActivities.notifyAll();
        }

        synchronized (bufferedIncomingActivities) {
            bufferedIncomingActivities.put(user.getJID(), null);
        }
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

    private List<TimedActivityDataObject> createTimedActivities(
        List<IActivityDataObject> activityDataObjects, int startSequenceNumber) {

        assert !activityDataObjects.contains(null) : "activity must not be null";

        List<TimedActivityDataObject> timedActivities = new ArrayList<TimedActivityDataObject>(
            activityDataObjects.size());

        for (IActivityDataObject ado : activityDataObjects) {
            timedActivities.add(new TimedActivityDataObject(ado, localJID,
                startSequenceNumber++));
        }

        return timedActivities;
    }

    private void sendTimedActivities(JID recipient,
        List<TimedActivityDataObject> timedActivities) {

        if (timedActivities.size() == 0)
            return;

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
            /*
             * FIMXE kick the user out of session (if host) or just terminate
             * the session(client)
             */
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
        if (!currentSessionID.equals(payload.getSessionID())) {
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
