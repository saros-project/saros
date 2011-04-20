/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity.Type;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ActivityUtils;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The ActivitySequencer is responsible for making sure that activityDataObjects
 * are sent and received in the right order. There is one ActivitySequencer for
 * each session participant.<br>
 * <br>
 * TODO Remove the dependency of this class on the ConcurrentDocumentManager,
 * push all responsibility up a layer into the SarosSession
 * 
 * @author rdjemili
 * @author coezbek
 * @author marrin
 */
public class ActivitySequencer {

    private static Logger log = Logger.getLogger(ActivitySequencer.class
        .getName());

    /**
     * Number of milliseconds between each flushing and sending of outgoing
     * activityDataObjects, and testing for too old queued incoming
     * activityDataObjects.
     */
    protected int MILLIS_UPDATE;

    public static class DataObjectQueueItem {

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

    /** Buffer for outgoing activityDataObjects. */
    protected final BlockingQueue<DataObjectQueueItem> outgoingQueue = new LinkedBlockingQueue<DataObjectQueueItem>();

    /** Long time buffer for outgoing activityDataObjects. */
    protected Map<User, List<IActivityDataObject>> queuedOutgoingActivitiesOfUsers;

    /**
     * A priority queue for timed activityDataObjects. For each buddy there is
     * one ActivityQueue, in which received events from those are stored.<br>
     * TODO "Timestamps" are treated more like consecutive sequence numbers, so
     * maybe all names and documentation should be changed to reflect this.
     */
    protected class ActivityQueue {

        /**
         * How long to wait until ignore missing activityDataObjects in
         * milliseconds.
         */
        protected static final long ACTIVITY_TIMEOUT = 30 * 1000;

        /**
         * Sequence numbers for outgoing and incoming activityDataObjects start
         * with this value.
         */
        protected static final int FIRST_SEQUENCE_NUMBER = 0;

        /**
         * This {@link ActivityQueue} is for received activities from this
         * remote-user.
         */
        protected final JID jid;

        /** The next sequence number we're going to send to this user. */
        protected int nextSequenceNumber = FIRST_SEQUENCE_NUMBER;

        /**
         * Sequence number expected from the next activityDataObject received
         * from this user.
         */
        protected int expectedSequenceNumber = FIRST_SEQUENCE_NUMBER;

        /**
         * Oldest local timestamp for the queued activityDataObjects or 0 if
         * there are no activityDataObjects queued.
         * 
         * TODO Is this documentation correct?
         */
        protected long oldestLocalTimestamp = Long.MAX_VALUE;

        /** Queue of activityDataObjects received. */
        protected final PriorityQueue<TimedActivityDataObject> queuedActivities = new PriorityQueue<TimedActivityDataObject>();

        /**
         * History of activityDataObjects sent.
         * 
         * TODO Not really used at the moment. File creation activityDataObjects
         * don't store the content at the time they were sent, so they can't be
         * re-send.
         */
        protected final List<TimedActivityDataObject> history = new LinkedList<TimedActivityDataObject>();

        public ActivityQueue(JID jid) {
            this.jid = jid;
        }

        /**
         * Create a {@link TimedActivityDataObject} to send to the user
         * corresponding to this ActivityQueue and add it to the history of
         * created activityDataObjects.
         **/
        public TimedActivityDataObject createTimedActivity(
            IActivityDataObject activityDataObject) {

            TimedActivityDataObject result = new TimedActivityDataObject(
                activityDataObject, localJID, nextSequenceNumber++);
            history.add(result);
            return result;
        }

        /**
         * Add a received activityDataObject to the priority queue of the sender
         * maintained by the recipient.
         */
        public void add(TimedActivityDataObject activity) {

            // Ignore activityDataObjects with sequence numbers we have already
            // seen or
            // don't expect anymore.
            if (activity.getSequenceNumber() < expectedSequenceNumber) {
                log.warn("Ignored activityDataObject. Expected Nr. "
                    + expectedSequenceNumber + ", got: " + activity);
                return;
            }

            long now = System.currentTimeMillis();
            activity.setLocalTimestamp(now);
            if (oldestLocalTimestamp == Long.MAX_VALUE) {
                oldestLocalTimestamp = now;
            }

            // Log debug message if there are queued activityDataObjects.
            int size = queuedActivities.size();
            if (size > 0) {
                log.debug("For " + jid + " there are " + size
                    + " activityDataObjects queued. First queued: "
                    + queuedActivities.peek() + ", expected nr: "
                    + expectedSequenceNumber);
            }

            queuedActivities.add(activity);
        }

        /**
         * Set {@link ActivityQueue#oldestLocalTimestamp} to the oldest local
         * timestamp of the queued activityDataObjects or 0 if the queue is
         * empty.
         */
        protected void updateOldestLocalTimestamp() {
            oldestLocalTimestamp = Long.MAX_VALUE;
            for (TimedActivityDataObject timedActivity : queuedActivities) {
                long localTimestamp = timedActivity.getLocalTimestamp();
                if (localTimestamp < oldestLocalTimestamp) {
                    oldestLocalTimestamp = localTimestamp;
                }
            }
        }

        /**
         * @return The next activityDataObject if there is one and it carries
         *         the expected sequence number, otherwise <code>null</code>.
         */
        public TimedActivityDataObject removeNext() {

            if (!queuedActivities.isEmpty()
                && queuedActivities.peek().getSequenceNumber() == expectedSequenceNumber) {

                expectedSequenceNumber++;
                TimedActivityDataObject result = queuedActivities.remove();
                updateOldestLocalTimestamp();
                return result;
            }
            return null;
        }

        /**
         * Check for activityDataObjects that are missing for more than
         * {@link ActivityQueue#ACTIVITY_TIMEOUT} milliseconds or twice as long
         * if there is a file transfer for the JID of this queue, and skip an
         * expected sequence number.
         */
        protected void checkForMissingActivities() {

            if (queuedActivities.isEmpty())
                return;

            int firstQueuedSequenceNumber = queuedActivities.peek()
                .getSequenceNumber();

            // Discard all activityDataObjects which we are no longer waiting
            // for
            while (firstQueuedSequenceNumber < expectedSequenceNumber) {

                TimedActivityDataObject activity = queuedActivities.remove();

                log.error("Expected activityDataObject #"
                    + expectedSequenceNumber
                    + " but an older activityDataObject is still in the queue"
                    + " and will be dropped (#" + firstQueuedSequenceNumber
                    + "): " + activity);

                if (queuedActivities.isEmpty())
                    return;

                firstQueuedSequenceNumber = queuedActivities.peek()
                    .getSequenceNumber();
            }

            if (firstQueuedSequenceNumber == expectedSequenceNumber)
                return; // Next Activity is ready to be executed

            /*
             * Last case: firstQueuedSequenceNumber > expectedSequenceNumber
             * 
             * -> Check for time-out
             */
            long age = System.currentTimeMillis() - oldestLocalTimestamp;
            if (age > ACTIVITY_TIMEOUT) {
                if (age < ACTIVITY_TIMEOUT * 2) {
                    // Early exit if there is a file transfer running.
                    if (transferManager.isReceiving(jid)) {
                        // TODO SS need to be more flexible
                        return;
                    }
                }

                int skipCount = firstQueuedSequenceNumber
                    - expectedSequenceNumber;
                log.warn("Gave up waiting for activityDataObject # "
                    + expectedSequenceNumber
                    + ((skipCount == 1) ? "" : " to "
                        + (firstQueuedSequenceNumber - 1)) + " from " + jid);
                expectedSequenceNumber = firstQueuedSequenceNumber;
                // TODO: Umut: Why do we need to recompute this here?
                updateOldestLocalTimestamp();
            }
        }

        /**
         * Returns all activityDataObjects which can be executed. If there are
         * none, an empty List is returned.
         * 
         * This method also checks for missing activityDataObjects and discards
         * out-dated or unwanted activityDataObjects.
         */
        public List<TimedActivityDataObject> removeActivities() {

            checkForMissingActivities();

            ArrayList<TimedActivityDataObject> result = new ArrayList<TimedActivityDataObject>();

            TimedActivityDataObject activity;
            while ((activity = removeNext()) != null) {
                result.add(activity);
            }

            return result;
        }
    }

    /**
     * This class manages a {@link ActivityQueue} for each other user of a
     * session.
     */
    protected class ActivityQueuesManager {
        protected final Map<JID, ActivityQueue> jid2queue = new ConcurrentHashMap<JID, ActivityQueue>();

        /**
         * Get the {@link ActivityQueue} for the given {@link JID}.
         * 
         * If there is no queue for the {@link JID}, a new one is created.
         * 
         * @param jid
         *            {@link JID} to get the queue for.
         * @return the {@link ActivityQueue} for the given {@link JID}.
         */
        protected synchronized ActivityQueue getActivityQueue(JID jid) {
            ActivityQueue queue = jid2queue.get(jid);
            if (queue == null) {
                queue = new ActivityQueue(jid);
                jid2queue.put(jid, queue);
            }
            return queue;
        }

        /**
         * @see ActivitySequencer#createTimedActivities(JID, List)
         */
        public synchronized List<TimedActivityDataObject> createTimedActivities(
            JID recipient, List<IActivityDataObject> activityDataObjects) {

            ArrayList<TimedActivityDataObject> result = new ArrayList<TimedActivityDataObject>(
                activityDataObjects.size());
            ActivityQueue queue = getActivityQueue(recipient);
            for (IActivityDataObject activityDataObject : activityDataObjects) {
                result.add(queue.createTimedActivity(activityDataObject));
            }
            return result;
        }

        /**
         * Adds a received {@link TimedActivityDataObject}. There must be a
         * source set on the activityDataObject.
         * 
         * @param timedActivity
         *            to add to the queues.
         * 
         * @throws IllegalArgumentException
         *             if the source of the activityDataObject is
         *             <code>null</code>.
         */
        public void add(TimedActivityDataObject timedActivity) {
            getActivityQueue(timedActivity.getSender()).add(timedActivity);
        }

        /**
         * Remove the queue for a given user.
         * 
         * @param jid
         *            of the user to remove.
         */
        public void removeQueue(JID jid) {
            jid2queue.remove(jid);
        }

        /**
         * @return all activityDataObjects that can be executed. If there are
         *         none, an empty List is returned.
         * 
         *         This method also checks for missing activityDataObjects and
         *         discards out-dated or unwanted activityDataObjects.
         */
        public List<TimedActivityDataObject> removeActivities() {
            ArrayList<TimedActivityDataObject> result = new ArrayList<TimedActivityDataObject>();
            for (ActivityQueue queue : jid2queue.values()) {
                result.addAll(queue.removeActivities());
            }
            return result;
        }

        /**
         * @see ActivitySequencer#getActivityHistory(JID, int, boolean)
         */
        public List<TimedActivityDataObject> getHistory(JID user,
            int fromSequenceNumber, boolean andUp) {

            LinkedList<TimedActivityDataObject> result = new LinkedList<TimedActivityDataObject>();
            for (TimedActivityDataObject activity : getActivityQueue(user).history) {
                if (activity.getSequenceNumber() >= fromSequenceNumber) {
                    result.add(activity);
                    if (!andUp) {
                        break;
                    }
                }
            }
            return result;
        }

        /**
         * @see ActivitySequencer#getExpectedSequenceNumbers()
         */
        public Map<JID, Integer> getExpectedSequenceNumbers() {
            HashMap<JID, Integer> result = new HashMap<JID, Integer>();
            for (ActivityQueue queue : jid2queue.values()) {
                if (queue.queuedActivities.size() > 0) {
                    result.put(queue.jid, queue.expectedSequenceNumber);
                }
            }
            return result;
        }
    }

    protected final ActivityQueuesManager incomingQueues = new ActivityQueuesManager();

    /**
     * Whether this AS currently sends or receives events
     */
    protected boolean started = false;

    protected Timer flushTimer;

    protected final ISarosSession sarosSession;

    protected final ITransmitter transmitter;

    protected final JID localJID;

    protected final DataTransferManager transferManager;

    protected final DispatchThreadContext dispatchThread;

    @Inject
    protected ProjectNegotiationObservable projectExchangeProcesses;

    public ActivitySequencer(final ISarosSession sarosSession,
        ITransmitter transmitter, DataTransferManager transferManager,
        DispatchThreadContext threadContext, int updateInterval) {

        this.dispatchThread = threadContext;
        this.sarosSession = sarosSession;
        this.transmitter = transmitter;
        this.transferManager = transferManager;

        this.localJID = sarosSession.getLocalUser().getJID();
        this.MILLIS_UPDATE = updateInterval;

        this.queuedOutgoingActivitiesOfUsers = Collections
            .synchronizedMap(new HashMap<User, List<IActivityDataObject>>());

        this.transferManager.getTransferModeDispatch().add(
            new ITransferModeListener() {

                public void clear() {
                    // do nothing
                }

                public void transferFinished(JID jid, NetTransferMode newMode,
                    boolean incoming, long sizeTransferred,
                    long sizeUncompressed, long transmissionMillisecs) {

                    // trigger flushing with nulled DataObjectQueueItem
                    outgoingQueue.add(new DataObjectQueueItem((User) null,
                        (IActivityDataObject) null));
                }

                public void connectionChanged(JID jid,
                    IBytestreamConnection connection) {
                    // do nothing
                }
            });

        SarosPluginContext.initComponent(this);
    }

    /**
     * Start periodical flushing and sending of outgoing activityDataObjects and
     * checking for received activityDataObjects that are queued for too long.
     * 
     * @throws IllegalStateException
     *             if this method is called on an already started
     *             {@link ActivitySequencer}
     * 
     * @see #stop()
     */
    public void start() {

        if (started) {
            throw new IllegalStateException();
        }

        this.flushTimer = new Timer(true);

        started = true;

        /*
         * Since our task is waiting for the next item, we have to use
         * schedule() here, not scheduleAtFixedRate().
         */
        this.flushTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Utils.runSafeSync(log, new Runnable() {
                    public void run() {
                        flushTask();
                    }
                });
            }

            private void flushTask() {
                // Just to assert that after stop() no task is executed anymore
                if (!started)
                    return;

                List<DataObjectQueueItem> activities = new ArrayList<DataObjectQueueItem>(
                    outgoingQueue.size());

                try {
                    /*
                     * Wait until the queue is not empty.
                     * 
                     * If the timer is cancelled, we don't want our TimerTask to
                     * wait indefinitely. To get a chance to cancel the task, we
                     * use poll with a timeout here instead of take.
                     */
                    DataObjectQueueItem item = outgoingQueue.poll(30,
                        TimeUnit.SECONDS);
                    if (item == null) {
                        // poll() timed out, nothing to do.
                        return;
                    }
                    activities.add(item);
                } catch (InterruptedException e1) {
                    if (!started)
                        return;
                    log.error("Interrupted while waiting for outgoing object",
                        e1);
                }
                // If there was more than one ADO waiting, get the rest now.
                outgoingQueue.drainTo(activities);

                Map<User, List<IActivityDataObject>> toSend = AutoHashMap
                    .getListHashMap();

                boolean doFlushQueues = false;
                for (DataObjectQueueItem item : activities) {
                    if (item.activityDataObject == null) {
                        doFlushQueues = true;
                    } else
                        for (User recipient : item.recipients) {
                            toSend.get(recipient).add(item.activityDataObject);
                        }
                }

                for (Entry<User, List<IActivityDataObject>> e : toSend
                    .entrySet()) {
                    sendActivities(e.getKey(), optimize(e.getValue()), false);
                }

                if (doFlushQueues)
                    flushQueues();

                /*
                 * Periodically execQueues() because waiting activityDataObjects
                 * might have timed-out
                 */
                dispatchThread.executeAsDispatch(new Runnable() {
                    public void run() {
                        execQueue();
                    }
                });
            }

            /**
             * Sends given activityDataObjects to given recipient.
             * 
             * @private because this method must not be called from somewhere
             *          else than this TimerTask.
             * 
             * @throws IllegalArgumentException
             *             if the recipient is the local user or the
             *             activityDataObjects contain <code>null</code>.
             */
            private void sendActivities(User recipient,
                List<IActivityDataObject> activityDataObjects, boolean dontQueue) {

                if (recipient.isLocal()) {
                    throw new IllegalArgumentException(
                        "Sending a message to the local user is not supported");
                }

                if (activityDataObjects.contains(null)) {
                    throw new IllegalArgumentException(
                        "Cannot send a null activityDataObject");
                }

                // Handle long time queue
                List<IActivityDataObject> userqueue = queuedOutgoingActivitiesOfUsers
                    .get(recipient);

                if (isActivityQueuingSuiteable(recipient, activityDataObjects)
                    && dontQueue == false) {
                    // if new activities can be queued, do so
                    if (userqueue == null) {
                        userqueue = Collections
                            .synchronizedList(new LinkedList<IActivityDataObject>());
                    }
                    userqueue.addAll(activityDataObjects);
                    queuedOutgoingActivitiesOfUsers.put(recipient, userqueue);

                    log.debug("Adding " + activityDataObjects.size()
                        + " activities to queue: "
                        + activityDataObjects.toString());
                    return;
                } else if (userqueue != null) {
                    // send queued activities and new activities, clearing the
                    // queue
                    log.debug("Flushing activity queue, sending "
                        + userqueue.size() + " old and "
                        + activityDataObjects.size() + " new activities");

                    queuedOutgoingActivitiesOfUsers.remove(recipient);
                    userqueue.addAll(activityDataObjects);
                    activityDataObjects = userqueue;
                }

                JID recipientJID = recipient.getJID();

                List<TimedActivityDataObject> timedActivities = createTimedActivities(
                    recipientJID, activityDataObjects);

                log.trace("Sending " + timedActivities.size()
                    + " activities to " + recipientJID + ": " + timedActivities);

                transmitter.sendTimedActivities(recipientJID, timedActivities);
            }

            /**
             * During an project transmission over IBB to the same recipient as
             * these timedActivities, activities that are not time-critical will
             * be queued and send as bundles to reduce message traffic (which in
             * extreme situation could crash IBB connection)
             * 
             * @param recipient
             *            {@link JID} of the user to send activities to
             * @return true if the queued activities can stay queued, false if
             *         they need to be send
             */
            boolean isActivityQueuingSuiteable(User recipient,
                List<IActivityDataObject> usersActivities) {

                JID recipientJID = recipient.getJID();

                if (projectExchangeProcesses
                    .getProjectExchangeProcess(recipientJID) instanceof OutgoingProjectNegotiation
                    && transferManager.getTransferMode(recipientJID) == NetTransferMode.IBB) {

                    // if timedActivities have non-time-critical activities
                    // only, lets queue them
                    if (ActivityUtils
                        .containsQueueableActivitiesOnly(usersActivities))
                        return true;
                }
                return false;
            }

            /**
             * Sends all queued activities
             */
            void flushQueues() {

                for (User user : queuedOutgoingActivitiesOfUsers.keySet()) {
                    List<IActivityDataObject> userQueue = queuedOutgoingActivitiesOfUsers
                        .get(user);
                    sendActivities(user, userQueue, true);
                }
                queuedOutgoingActivitiesOfUsers.clear();
            }
        }, 0, MILLIS_UPDATE);
    }

    /**
     * Stop periodical flushing and sending of outgoing activityDataObjects and
     * checking for received activityDataObjects that are queued for too long.
     * 
     * @see #start()
     */
    public void stop() {
        if (!started) {
            throw new IllegalStateException();
        }

        this.flushTimer.cancel();
        this.flushTimer = null;

        started = false;
    }

    /**
     * The central entry point for receiving Activities from the Network
     * component (either via message or data transfer, thus the following is
     * synchronized on the queue).
     * 
     * The activityDataObjects are sorted (in the queue) and executed in order.
     * 
     * If an activityDataObject is missing, this method just returns and queues
     * the given activityDataObject
     */
    public void exec(TimedActivityDataObject nextActivity) {

        assert nextActivity != null;

        incomingQueues.add(nextActivity);

        if (!started) {
            log.debug("Received activityDataObject but "
                + "ActivitySequencer has not yet been started: " + nextActivity);
            return;
        }

        execQueue();
    }

    /**
     * executes all activityDataObjects that are currently in the queue
     */
    protected void execQueue() {
        List<IActivityDataObject> activityDataObjects = new ArrayList<IActivityDataObject>();
        for (TimedActivityDataObject timedActivity : incomingQueues
            .removeActivities()) {
            activityDataObjects.add(timedActivity.getActivity());
        }
        sarosSession.exec(activityDataObjects);
    }

    /**
     * Sends the given activityDataObject to the given recipients.
     */
    public void sendActivity(List<User> recipients,
        final IActivityDataObject activityDataObject) {
        /**
         * Short cut all messages directed at local user
         */
        ArrayList<User> toSendViaNetwork = new ArrayList<User>();
        for (User user : recipients) {
            if (user.isLocal()) {
                dispatchThread.executeAsDispatch(new Runnable() {
                    public void run() {
                        sarosSession.exec(Collections
                            .singletonList(activityDataObject));
                    }
                });
            } else {
                toSendViaNetwork.add(user);
            }
        }

        if (toSendViaNetwork.isEmpty()) {
            log.trace(null, new StackTrace());
            return;
        }
        this.outgoingQueue.add(new DataObjectQueueItem(toSendViaNetwork,
            activityDataObject));
    }

    /**
     * Create {@link TimedActivityDataObject}s for the given recipient and
     * activityDataObjects and add them to the history of activityDataObjects
     * for the recipient.
     * 
     * This operation is thread safe, i.e. it is guaranteed that all
     * activityDataObjects get increasing, consecutive sequencer numbers, even
     * if this method is called from different threads concurrently.
     */
    protected List<TimedActivityDataObject> createTimedActivities(
        JID recipient, List<IActivityDataObject> activityDataObjects) {
        return incomingQueues.createTimedActivities(recipient,
            activityDataObjects);
    }

    /**
     * Get the activityDataObject history for given user and given timestamp.
     * 
     * If andUp is <code>true</code> all activityDataObjects that are equal or
     * greater than the timestamp are returned, otherwise just the
     * activityDataObject that matches the timestamp exactly.
     * 
     * If no activityDataObject matches the criteria an empty list is returned.
     */
    public List<TimedActivityDataObject> getActivityHistory(JID user,
        int fromSequenceNumber, boolean andUp) {

        return incomingQueues.getHistory(user, fromSequenceNumber, andUp);
    }

    /**
     * Get a {@link Map} that maps the {@link JID} of users with queued
     * activityDataObjects to the first missing sequence number.
     */
    public Map<JID, Integer> getExpectedSequenceNumbers() {
        return incomingQueues.getExpectedSequenceNumbers();
    }

    /**
     * This method tries to reduce the number of activityDataObjects transmitted
     * by removing activityDataObjects that would overwrite each other and
     * joining activityDataObjects that can be send as a single
     * activityDataObject.
     */
    private static List<IActivityDataObject> optimize(
        List<IActivityDataObject> toOptimize) {

        List<IActivityDataObject> result = new ArrayList<IActivityDataObject>(
            toOptimize.size());

        TextSelectionActivityDataObject selection = null;
        LinkedHashMap<SPathDataObject, ViewportActivityDataObject> viewport = new LinkedHashMap<SPathDataObject, ViewportActivityDataObject>();

        for (IActivityDataObject activityDataObject : toOptimize) {

            if (activityDataObject instanceof TextEditActivityDataObject) {
                TextEditActivityDataObject textEdit = (TextEditActivityDataObject) activityDataObject;
                textEdit = joinTextEdits(result, textEdit);
                result.add(textEdit);
            } else if (activityDataObject instanceof TextSelectionActivityDataObject) {
                selection = (TextSelectionActivityDataObject) activityDataObject;
            } else if (activityDataObject instanceof ViewportActivityDataObject) {
                ViewportActivityDataObject viewActivity = (ViewportActivityDataObject) activityDataObject;
                viewport.remove(viewActivity.getPath());
                viewport.put(viewActivity.getPath(), viewActivity);
            } else if (activityDataObject instanceof FolderActivityDataObject) {
                FolderActivityDataObject folderEdit = (FolderActivityDataObject) activityDataObject;
                foldRecursiveDelete(result, folderEdit);
            } else {
                result.add(activityDataObject);
            }
        }

        // only send one selection activityDataObject
        if (selection != null)
            result.add(selection);

        // Add only one viewport per editor
        for (Entry<SPathDataObject, ViewportActivityDataObject> entry : viewport
            .entrySet()) {
            result.add(entry.getValue());
        }

        assert !result.contains(null);

        return result;
    }

    private static void foldRecursiveDelete(List<IActivityDataObject> result,
        FolderActivityDataObject folderEdit) {

        if (folderEdit.getType() != Type.Removed) {
            result.add(folderEdit);
            return;
        }

        int i = result.size() - 1;
        boolean dropNew = false;

        while (i >= 0 && !dropNew) {
            FolderActivityDataObject curr = null;

            if (result.get(i) instanceof FolderActivityDataObject)
                curr = (FolderActivityDataObject) result.get(i);
            else {
                i--;
                continue;
            }

            if (curr.isChildOf(folderEdit))
                result.remove(i);

            else if (curr.getPath().equals(folderEdit.getPath())) {
                result.remove(i);
                dropNew = true;
            }

            i--;
        }

        if (!dropNew)
            result.add(folderEdit);
    }

    private static TextEditActivityDataObject joinTextEdits(
        List<IActivityDataObject> result, TextEditActivityDataObject textEdit) {
        if (result.size() == 0) {
            return textEdit;
        }

        IActivityDataObject lastActivity = result.get(result.size() - 1);
        if (lastActivity instanceof TextEditActivityDataObject) {
            TextEditActivityDataObject lastTextEdit = (TextEditActivityDataObject) lastActivity;

            if (((lastTextEdit.getSource() == null) || lastTextEdit.getSource()
                .equals(textEdit.getSource()))
                && (textEdit.getOffset() == lastTextEdit.getOffset()
                    + lastTextEdit.getText().length())) {
                result.remove(lastTextEdit);
                textEdit = new TextEditActivityDataObject(
                    lastTextEdit.getSource(),
                    lastTextEdit.getOffset(),
                    lastTextEdit.getText() + textEdit.getText(),
                    lastTextEdit.getReplacedText() + textEdit.getReplacedText(),
                    lastTextEdit.getPath());
            }
        }

        return textEdit;
    }

    /**
     * Removes queued activityDataObjects from given user.
     * 
     * TODO Maybe remove outgoing activityDataObjects from
     * {@link #outgoingQueue} too!?
     * 
     * @param jid
     *            of the user that left.
     */
    public void userLeft(JID jid) {
        incomingQueues.removeQueue(jid);
    }
}
