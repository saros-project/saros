package de.fu_berlin.inf.dpp.net.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * A priority queue for timed activityDataObjects. For each user there is one
 * ActivityQueue, in which received events from those are stored.<br>
 * TODO "Timestamps" are treated more like consecutive sequence numbers, so
 * maybe all names and documentation should be changed to reflect this.
 * 
 * @Note this class is <b>NOT</b> thread safe
 */
public final class ActivityQueue {

    private static final Logger LOG = Logger.getLogger(ActivityQueue.class);
    /**
     * How long to wait until ignore missing activityDataObjects in
     * milliseconds.
     */
    private static final long ACTIVITY_TIMEOUT = 60 * 1000;

    /**
     * Sequence numbers for outgoing and incoming activityDataObjects start with
     * this value.
     */
    private static final int FIRST_SEQUENCE_NUMBER = 0;

    /**
     * This {@link ActivityQueue} is for received activities from this
     * remote-user.
     */
    private final JID jid;

    private final JID localJID;

    /** The next sequence number we're going to send to this user. */
    private int nextSequenceNumber = FIRST_SEQUENCE_NUMBER;

    /**
     * Sequence number expected from the next activityDataObject received from
     * this user.
     */
    private int expectedSequenceNumber = FIRST_SEQUENCE_NUMBER;

    /**
     * Oldest local timestamp for the queued activityDataObjects or 0 if there
     * are no activityDataObjects queued.
     * 
     * TODO Is this documentation correct?
     */
    private long oldestLocalTimestamp = Long.MAX_VALUE;

    /** Queue of activityDataObjects received. */
    private final PriorityQueue<TimedActivityDataObject> queuedActivities = new PriorityQueue<TimedActivityDataObject>();

    public ActivityQueue(JID localJID, JID jid) {
        this.localJID = localJID;
        this.jid = jid;
    }

    /**
     * Create a {@link TimedActivityDataObject} to send to the user
     * corresponding to this ActivityQueue and add it to the history of created
     * activityDataObjects.
     **/
    // seems to be a misplaced method
    public TimedActivityDataObject createTimedActivity(
        IActivityDataObject activityDataObject) {

        TimedActivityDataObject result = new TimedActivityDataObject(
            activityDataObject, localJID, nextSequenceNumber++);
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
            LOG.warn("Ignored activityDataObject. Expected Nr. "
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
            LOG.debug("For " + jid + " there are " + size
                + " activityDataObjects queued. First queued: "
                + queuedActivities.peek() + ", expected nr: "
                + expectedSequenceNumber);
        }

        queuedActivities.add(activity);
    }

    /**
     * Set {@link ActivityQueue#oldestLocalTimestamp} to the oldest local
     * timestamp of the queued activityDataObjects or 0 if the queue is empty.
     */
    private void updateOldestLocalTimestamp() {
        oldestLocalTimestamp = Long.MAX_VALUE;
        for (TimedActivityDataObject timedActivity : queuedActivities) {
            long localTimestamp = timedActivity.getLocalTimestamp();
            if (localTimestamp < oldestLocalTimestamp) {
                oldestLocalTimestamp = localTimestamp;
            }
        }
    }

    /**
     * @return The next activityDataObject if there is one and it carries the
     *         expected sequence number, otherwise <code>null</code>.
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
     * {@link ActivityQueue#ACTIVITY_TIMEOUT} milliseconds or twice as long if
     * there is a file transfer for the JID of this queue, and skip an expected
     * sequence number.
     */
    private void checkForMissingActivities() {

        if (queuedActivities.isEmpty())
            return;

        int firstQueuedSequenceNumber = queuedActivities.peek()
            .getSequenceNumber();

        // Discard all activityDataObjects which we are no longer waiting
        // for
        while (firstQueuedSequenceNumber < expectedSequenceNumber) {

            TimedActivityDataObject activity = queuedActivities.remove();

            LOG.error("Expected activityDataObject #" + expectedSequenceNumber
                + " but an older activityDataObject is still in the queue"
                + " and will be dropped (#" + firstQueuedSequenceNumber + "): "
                + activity);

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

        if (age < ACTIVITY_TIMEOUT)
            return;

        int skipCount = firstQueuedSequenceNumber - expectedSequenceNumber;
        LOG.warn("Gave up waiting for activityDataObject # "
            + expectedSequenceNumber
            + ((skipCount == 1) ? "" : " to " + (firstQueuedSequenceNumber - 1))
            + " from " + jid);
        expectedSequenceNumber = firstQueuedSequenceNumber;
        // TODO: Umut: Why do we need to recompute this here?
        updateOldestLocalTimestamp();

    }

    /**
     * Returns all activityDataObjects which can be executed. If there are none,
     * an empty List is returned.
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

    /**
     * Returns the currently expected sequence number.
     * 
     * @return
     */
    public int getExpectedSequenceNumber() {
        return expectedSequenceNumber;
    }

    /**
     * Returns the size of the currently queued activities.
     * 
     * @return
     */
    public int getQueuedActivitiesSize() {
        return queuedActivities.size();
    }

    /**
     * Returns the JID for the queue.
     * 
     * @return
     */
    public JID getJID() {
        return jid;
    }
}
