package de.fu_berlin.inf.dpp.net.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This class manages a {@link ActivityQueue} for each other user of a session.
 */
public final class ActivityQueueManager {
    protected final Map<JID, ActivityQueue> jid2queue = new ConcurrentHashMap<JID, ActivityQueue>();

    private final JID local;

    public ActivityQueueManager(JID local) {
        this.local = local;
    }

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
            queue = new ActivityQueue(local, jid);
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
     * Adds a received {@link TimedActivityDataObject}. There must be a source
     * set on the activityDataObject.
     * 
     * @param timedActivity
     *            to add to the queues.
     * 
     * @throws IllegalArgumentException
     *             if the source of the activityDataObject is <code>null</code>.
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
     * @return all activityDataObjects that can be executed. If there are none,
     *         an empty List is returned.
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
     * @see ActivitySequencer#getExpectedSequenceNumbers()
     */
    public Map<JID, Integer> getExpectedSequenceNumbers() {
        HashMap<JID, Integer> result = new HashMap<JID, Integer>();
        for (ActivityQueue queue : jid2queue.values()) {
            if (queue.getQueuedActivitiesSize() > 0) {
                result.put(queue.getJID(), queue.getExpectedSequenceNumber());
            }
        }
        return result;
    }
}
