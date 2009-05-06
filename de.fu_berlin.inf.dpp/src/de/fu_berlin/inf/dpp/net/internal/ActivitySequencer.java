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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The ActivitySequencer is responsible for making sure that activities are sent
 * and received in the right order.
 * 
 * TODO Remove the dependency of this class on the ConcurrentDocumentManager,
 * push all responsibility up a layer into the SharedProject
 * 
 * @author rdjemili
 * @author coezbek
 */
public class ActivitySequencer implements IActivityListener, IActivityManager,
    Disposable {

    private static Logger log = Logger.getLogger(ActivitySequencer.class
        .getName());

    /**
     * Number of milliseconds between each flushing and sending of outgoing
     * activities, and testing for too old queued incoming activities.
     */
    protected static final int MILLIS_UPDATE = 500;

    /** Buffer for outgoing activities. */
    protected final List<IActivity> activities = new LinkedList<IActivity>();

    protected final List<IActivityProvider> providers = new LinkedList<IActivityProvider>();

    /**
     * A priority queue for timed activities.
     * 
     * TODO "Timestamps" are treated more like consecutive sequence numbers, so
     * may be all names and documentation should be changed to reflect this.
     */
    protected class ActivityQueue {

        /** How long to wait until ignore missing activities in milliseconds. */
        protected static final long ACTIVITY_TIMEOUT = 30 * 1000;

        /** This {@link ActivityQueue} is for this user. */
        protected final JID jid;

        /** Sequence number this user sends next. */
        protected int nextSequenceNumber = 0;

        /** Sequence number expected from the next activity. */
        protected int expectedSequenceNumber = 0;

        /**
         * Oldest local timestamp for the queued activities or 0 if there are no
         * activities queued.
         */
        protected long oldestLocalTimestamp = Long.MAX_VALUE;

        /** Queue of activities received. */
        protected final PriorityQueue<TimedActivity> queuedActivities = new PriorityQueue<TimedActivity>();

        /**
         * History of activities sent.
         * 
         * TODO Not really used at the moment. File creation activities don't
         * store the content at the time they were sent, so they can't be
         * re-send.
         */
        protected final List<TimedActivity> history = new LinkedList<TimedActivity>();

        public ActivityQueue(JID jid) {
            this.jid = jid;
        }

        /**
         * Create a {@link TimedActivity} and add it to the history of created
         * activities.
         */
        public TimedActivity createTimedActivity(IActivity activity) {

            TimedActivity result = new TimedActivity(activity,
                nextSequenceNumber++);
            history.add(result);
            return result;
        }

        /**
         * Add a received activity to the priority queue.
         */
        public void add(TimedActivity activity) {

            // Ignore activities with sequence numbers we have already seen or
            // don't expect anymore.
            if (activity.getSequenceNumber() < expectedSequenceNumber) {
                log.warn("Ignored activity. Expected Nr. "
                    + expectedSequenceNumber + ", got: " + activity);
                return;
            }

            long now = System.currentTimeMillis();
            activity.setLocalTimestamp(now);
            if (oldestLocalTimestamp == Long.MAX_VALUE) {
                oldestLocalTimestamp = now;
            }

            // Log debug message if there are queued activities.
            int size = queuedActivities.size();
            if (size > 0) {
                log
                    .debug("There are " + size + " activities queued for "
                        + jid);
                log.debug("first queued: " + queuedActivities.peek()
                    + ", expected nr: " + expectedSequenceNumber);
            }

            queuedActivities.add(activity);
        }

        /**
         * Set {@link ActivityQueue#oldestLocalTimestamp} to the oldest local
         * timestamp of the queued activities or 0 if the queue is empty.
         */
        protected void updateOldestLocalTimestamp() {
            oldestLocalTimestamp = Long.MAX_VALUE;
            for (TimedActivity timedActivity : queuedActivities) {
                long localTimestamp = timedActivity.getLocalTimestamp();
                if (localTimestamp < oldestLocalTimestamp) {
                    oldestLocalTimestamp = localTimestamp;
                }
            }
        }

        /**
         * @return The next activity if there is one and it carries the expected
         *         sequence number, otherwise <code>null</code>.
         */
        public TimedActivity removeNext() {
            if (queuedActivities.size() > 0
                && queuedActivities.peek().getSequenceNumber() == expectedSequenceNumber) {

                expectedSequenceNumber++;
                TimedActivity result = queuedActivities.remove();
                updateOldestLocalTimestamp();
                return result;
            }
            return null;
        }

        /**
         * Check for activities that are missing for more than
         * {@link ActivityQueue#ACTIVITY_TIMEOUT} milliseconds or twice as long
         * if there is a file transfer for the JID of this queue, and skip an
         * expected sequence number.
         */
        public void checkForMissingActivities() {
            if (queuedActivities.size() == 0) {
                return;
            }
            int firstQueuedSequenceNumber = queuedActivities.peek()
                .getSequenceNumber();
            if (expectedSequenceNumber >= firstQueuedSequenceNumber) {

                log.error("Expected activity #" + expectedSequenceNumber
                    + " >= first queued #" + firstQueuedSequenceNumber);
                return;
            }
            long age = System.currentTimeMillis() - oldestLocalTimestamp;
            if (age > ACTIVITY_TIMEOUT) {
                if (age < ACTIVITY_TIMEOUT * 2) {
                    // Early exit if there is a file transfer running.
                    if (transferManager.isReceiving(jid)) {
                        return;
                    }
                }

                int skipCount = firstQueuedSequenceNumber
                    - expectedSequenceNumber;
                log.warn("Gave up waiting for activity # "
                    + expectedSequenceNumber
                    + ((skipCount == 1) ? "" : " to "
                        + (firstQueuedSequenceNumber - 1)) + " from " + jid);
                expectedSequenceNumber = firstQueuedSequenceNumber;
                updateOldestLocalTimestamp();
            }
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
        public synchronized List<TimedActivity> createTimedActivities(
            JID recipient, List<IActivity> activities) {

            ArrayList<TimedActivity> result = new ArrayList<TimedActivity>(
                activities.size());
            ActivityQueue queue = getActivityQueue(recipient);
            for (IActivity activity : activities) {
                result.add(queue.createTimedActivity(activity));
            }
            return result;
        }

        /**
         * Adds a received {@link TimedActivity}. There must be a source set on
         * the activity.
         * 
         * @param timedActivity
         *            to add to the qeues.
         * 
         * @throws IllegalArgumentException
         *             if the source of the activity is <code>null</code>.
         */
        public void add(TimedActivity timedActivity) {
            String source;

            // HACK In TextEditActivities the source and the sender differ if
            // they are gone through Jupiter on the host side.
            IActivity activity = timedActivity.getActivity();
            if (activity instanceof TextEditActivity) {
                TextEditActivity textEditActivity = (TextEditActivity) activity;
                source = textEditActivity.getSender();
            } else {
                source = timedActivity.getSource();
            }

            if (source == null) {
                throw new IllegalArgumentException(
                    "Source of activity must not be null");
            }

            getActivityQueue(new JID(source)).add(timedActivity);
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
         * @return all activities that can be executed. If there are none, an
         *         empty List is returned.
         */
        public List<TimedActivity> removeActivities() {
            ArrayList<TimedActivity> result = new ArrayList<TimedActivity>();
            for (ActivityQueue queue : jid2queue.values()) {
                TimedActivity activity;
                while ((activity = queue.removeNext()) != null) {
                    result.add(activity);
                }
            }
            return result;
        }

        /**
         * @see ActivitySequencer#getActivityHistory(JID, int, boolean)
         */
        public List<TimedActivity> getHistory(JID user, int fromSequenceNumber,
            boolean andUp) {

            LinkedList<TimedActivity> result = new LinkedList<TimedActivity>();
            for (TimedActivity activity : getActivityQueue(user).history) {
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

        /**
         * Check each queue for activities that are too old and eventually skip
         * missing activities.
         */
        public void checkForMissingActivities() {
            for (ActivityQueue queue : jid2queue.values()) {
                queue.checkForMissingActivities();
            }
        }
    }

    protected final ActivityQueuesManager queues = new ActivityQueuesManager();

    /*
     * TODO The AS should not know the ConcurrentDocumentManager but rather pass
     * all calls up into the SharedProject.
     */
    protected ConcurrentDocumentManager concurrentDocumentManager;

    /**
     * Whether this AS currently sends or receives events
     */
    protected boolean started = false;

    protected Timer flushTimer;

    protected final ISharedProject sharedProject;

    protected final ITransmitter transmitter;

    protected final DataTransferManager transferManager;

    /**
     * This object is used to synchronize on the execution of activities.
     * 
     * The two threads which need to have mutually exclusive access are:
     * 
     * 1.) network dispatch
     * 
     * 2.) the flush timer
     */
    protected Object execLock = new Object();

    public ActivitySequencer(ISharedProject sharedProject,
        ITransmitter transmitter, DataTransferManager transferManager) {

        this.sharedProject = sharedProject;
        this.transmitter = transmitter;
        this.transferManager = transferManager;
    }

    /**
     * Start periodical flushing and sending of outgoing activities and checking
     * for received activities that are queued for too long.
     * 
     * @throws IllegalStateException
     *             if this method is called on an already started
     *             {@link ActivitySequencer} or if the
     *             {@link ConcurrentDocumentManager} was not set before this
     *             method is called.
     * 
     * @see #stop()
     * @see #setConcurrentManager(ConcurrentDocumentManager)
     */
    public void start() {

        if (started || concurrentDocumentManager == null) {
            throw new IllegalStateException();
        }

        this.flushTimer = new Timer(true);

        started = true;

        this.flushTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Util.runSafeSync(log, new Runnable() {
                    public void run() {
                        flushTask();
                    }
                });
            }

            private void flushTask() {
                // Just to assert that after stop() no task is executed anymore
                if (!started)
                    return;

                List<IActivity> activities = flush();

                if (activities.size() > 0
                    && sharedProject.getParticipantCount() > 1) {
                    List<JID> participantJIDs = new ArrayList<JID>(
                        sharedProject.getParticipantCount());
                    for (User participant : sharedProject.getParticipants()) {
                        participantJIDs.add(participant.getJID());
                    }
                    sendActivities(participantJIDs, activities);
                }

                synchronized (execLock) {
                    queues.checkForMissingActivities();
                    // Maybe the check above has unblocked queued activities
                    // that can be executed now.
                    for (TimedActivity activity : queues.removeActivities()) {
                        exec(activity.getActivity());
                    }
                }
            }
        }, 0, MILLIS_UPDATE);
    }

    /**
     * Stop periodical flushing and sending of outgoing activities and checking
     * for received activities that are queued for too long.
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
     * TODO This should be pushed up into the SharedProject
     */
    protected void exec(final IActivity activity) {

        log.debug("Executing untransformed activity: " + activity);

        // TODO Replace this with a single call to the ConcurrentDocumentManager
        // and use the ActivityReceiver to handle all cases.
        try {
            if (activity instanceof EditorActivity) {
                this.concurrentDocumentManager.execEditorActivity(activity);
            }
            if (activity instanceof FileActivity) {
                this.concurrentDocumentManager.execFileActivity(activity);
            }
            if (activity instanceof FolderActivity) {
                // TODO [FileOps] Does not handle FolderActivity
            }
            if (activity instanceof Request) {
                this.concurrentDocumentManager
                    .receiveRequest((Request) activity);
                return;
            }
        } catch (Exception e) {
            log.error("Error while executing activity.", e);
        }

        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {

                if (activity instanceof TextEditActivity) {
                    if (concurrentDocumentManager.isHostSide()
                        || concurrentDocumentManager
                            .isManagedByJupiter(activity)) {
                        return;
                    }
                }

                // Execute all other activities
                for (IActivityProvider executor : ActivitySequencer.this.providers) {
                    executor.exec(activity);
                }
            }
        });

    }

    /**
     * The central entry point for receiving Activities from the Network
     * component (either via message or data transfer, thus the following is
     * synchronized on the queue).
     * 
     * The activities are sorted (in the queue) and executed in order.
     * 
     * If an activity is missing, this method just returns and queues the given
     * activity
     */
    public void exec(TimedActivity nextActivity) {

        assert nextActivity != null;
        /*
         * TODO This code might lead to problems with activities gone through
         * Jupiter which have a timestamp that does not match the source in the
         * activity, with the exception of activities originally generated by
         * the host.
         */
        synchronized (execLock) {

            log.debug("Rcvd Activities fr " + nextActivity.getSource() + ":"
                + nextActivity);

            queues.add(nextActivity);

            if (!started)
                return;

            for (TimedActivity activity : queues.removeActivities()) {
                exec(activity.getActivity());
            }
        }
    }

    /**
     * @return List of activities that can be executed. The list is empty if
     *         there are no activities to execute.
     */
    protected List<IActivity> flush() {
        List<IActivity> out = new ArrayList<IActivity>(this.activities);
        this.activities.clear();
        return optimize(out);
    }

    /**
     * Sends given activities to given recipients.
     */
    public void sendActivities(Collection<JID> recipients,
        List<IActivity> activities) {

        setSenderOnTextEditActivities(activities);

        // Send the activities to each user.
        JID myJID = sharedProject.getLocalUser().getJID();
        for (JID recipientJID : recipients) {

            if (recipientJID.equals(myJID)) {
                continue;
            }

            ArrayList<TimedActivity> stillToSend = new ArrayList<TimedActivity>(
                activities.size());
            List<TimedActivity> timedActivities = createTimedActivities(
                recipientJID, activities);
            for (TimedActivity timedActivity : timedActivities) {

                // Check each activity if it is a file creation which will be
                // send asynchronous, and collect all others in stillToSend.
                // TODO boolean methods with side effects are bad style
                if (!ifFileCreateSendAsync(recipientJID, timedActivity)) {
                    stillToSend.add(timedActivity);
                }
            }
            if (stillToSend.size() > 0) {
                transmitter.sendTimedActivities(recipientJID, stillToSend);
            }
            log.debug("Sent Activities to " + recipientJID + ": "
                + timedActivities);
        }
    }

    /**
     * Sets sender on {@link TextEditActivity}s.
     */
    private void setSenderOnTextEditActivities(List<IActivity> activities) {
        String sender = sharedProject.getLocalUser().getJID().toString();
        for (IActivity activity : activities) {
            if (activity instanceof TextEditActivity) {
                TextEditActivity textEditActivity = (TextEditActivity) activity;
                textEditActivity.setSender(sender);
                assert textEditActivity.getSource() != null;
            }
        }
    }

    /**
     * Sends given {@link TimedActivity} asynchronous to the given recipient,
     * iff the activity is a {@link TextEditActivity}.
     * 
     * @return <code>true</code> if the activity was a file creation, otherwise
     *         <code>false</code>.
     */
    private boolean ifFileCreateSendAsync(JID recipientJID,
        TimedActivity timedActivity) {

        IActivity activity = timedActivity.getActivity();
        if (activity instanceof FileActivity) {
            FileActivity fileActivity = (FileActivity) activity;
            if (fileActivity.getType().equals(FileActivity.Type.Created)) {
                try {
                    transmitter.sendFileAsync(recipientJID, sharedProject
                        .getProject(), fileActivity.getPath(), timedActivity
                        .getSequenceNumber(),
                        new AbstractFileTransferCallback() {
                            @Override
                            public void fileTransferFailed(IPath path,
                                Exception e) {
                                log.error("File could not be send:", e);
                            }
                        });
                } catch (IOException e) {
                    log.error("File could not be send:", e);
                    // TODO This means we were really unable to send
                    // this file. No more falling back.
                }
                return true;
            }
        }
        return false;
    }

    public void addProvider(IActivityProvider provider) {
        this.providers.add(provider);
        provider.addActivityListener(this);
    }

    public void removeProvider(IActivityProvider provider) {
        this.providers.remove(provider);
        provider.removeActivityListener(this);
    }

    /**
     * All the ActivityProviders will call this method when new events occurred
     * in the UI.
     * 
     * TODO This method should be pushed up into SharedProject
     * 
     * @see IActivityListener
     */
    public void activityCreated(IActivity activity) {

        /* Let ConcurrentDocumentManager have a look at the activities first */
        boolean consumed = this.concurrentDocumentManager
            .activityCreated(activity);

        if (!consumed) {
            this.activities.add(activity);
        }
    }

    /**
     * Create {@link TimedActivity}s for the given recipient and activities and
     * add them to the history of activities for the recipient.
     * 
     * This operation is thread safe, i.e. it is guaranteed that all activities
     * get increasing, consecutive sequencer numbers, even if this method is
     * called from different threads concurrently.
     */
    protected List<TimedActivity> createTimedActivities(JID recipient,
        List<IActivity> activities) {
        return queues.createTimedActivities(recipient, activities);
    }

    /**
     * Get the activity history for given user and given timestamp.
     * 
     * If andUp is <code>true</code> all activities that are equal or greater
     * than the timestamp are returned, otherwise just the activity that matches
     * the timestamp exactly.
     * 
     * If no activity matches the criteria an empty list is returned.
     */
    public List<TimedActivity> getActivityHistory(JID user,
        int fromSequenceNumber, boolean andUp) {

        return queues.getHistory(user, fromSequenceNumber, andUp);
    }

    /**
     * Get a {@link Map} that maps the {@link JID} of users with queued
     * activities to the first missing sequence number.
     */
    public Map<JID, Integer> getExpectedSequenceNumbers() {
        return queues.getExpectedSequenceNumbers();
    }

    /**
     * This method tries to reduce the number of activities transmitted by
     * removing activities that would overwrite each other and joining
     * activities that can be send as a single activity.
     */
    private static List<IActivity> optimize(List<IActivity> toOptimize) {

        List<IActivity> result = new ArrayList<IActivity>(toOptimize.size());

        TextSelectionActivity selection = null;
        LinkedHashMap<IPath, ViewportActivity> viewport = new LinkedHashMap<IPath, ViewportActivity>();

        for (IActivity activity : toOptimize) {

            if (activity instanceof TextEditActivity) {
                TextEditActivity textEdit = (TextEditActivity) activity;
                textEdit = joinTextEdits(result, textEdit);
                result.add(textEdit);
            } else if (activity instanceof TextSelectionActivity) {
                selection = (TextSelectionActivity) activity;
            } else if (activity instanceof ViewportActivity) {
                ViewportActivity viewActivity = (ViewportActivity) activity;
                viewport.remove(viewActivity.getEditor());
                viewport.put(viewActivity.getEditor(), viewActivity);
            } else {
                result.add(activity);
            }
        }

        // only send one selection activity
        if (selection != null)
            result.add(selection);

        // Add only one viewport per editor
        for (Map.Entry<IPath, ViewportActivity> entry : viewport.entrySet()) {
            result.add(entry.getValue());
        }

        return result;
    }

    private static TextEditActivity joinTextEdits(List<IActivity> result,
        TextEditActivity textEdit) {
        if (result.size() == 0) {
            return textEdit;
        }

        IActivity lastActivity = result.get(result.size() - 1);
        if (lastActivity instanceof TextEditActivity) {
            TextEditActivity lastTextEdit = (TextEditActivity) lastActivity;

            if (((lastTextEdit.getSource() == null) || lastTextEdit.getSource()
                .equals(textEdit.getSource()))
                && (textEdit.offset == lastTextEdit.offset
                    + lastTextEdit.text.length())) {
                result.remove(lastTextEdit);
                textEdit = new TextEditActivity(lastTextEdit.getSource(),
                    lastTextEdit.offset, lastTextEdit.text + textEdit.text,
                    lastTextEdit.replacedText + textEdit.replacedText,
                    lastTextEdit.getEditor());
            }
        }

        return textEdit;
    }

    /**
     * Sets the {@link ConcurrentDocumentManager}.
     * 
     * Must be called before {@link #start()}.
     */
    public void setConcurrentManager(
        ConcurrentDocumentManager concurrentDocumentManager) {

        this.concurrentDocumentManager = concurrentDocumentManager;
    }

    public ConcurrentDocumentManager getConcurrentDocumentManager() {
        return this.concurrentDocumentManager;
    }

    /**
     * Execute activity after jupiter transforming process.
     * 
     * @param activity
     * 
     *            TODO Push this method into SharedProject
     * 
     * @swt Must be called from the SWT Thread
     */
    public void execTransformedActivity(IActivity activity) {

        assert Util.isSWT();

        try {
            log.debug("Executing   transformed activity: " + activity);

            for (IActivityProvider exec : this.providers) {
                exec.exec(activity);
            }

            /*
             * FIXME The following will send the activities to everybody, so all
             * drivers will receive the message twice (once through Jupiter once
             * as a Activity)
             */

            // send activity to everybody
            if (this.concurrentDocumentManager.isHostSide()) {
                // log.debug("send transformed activity: " + activity);
                this.activities.add(activity);
            }
        } catch (Exception e) {
            log.error("Error while executing activity.", e);
        }
    }

    /**
     * Removes queued activities from given user.
     * 
     * @param jid
     *            of the user that left.
     */
    public void userLeft(JID jid) {
        queues.removeQueue(jid);
    }

    public void dispose() {
        concurrentDocumentManager.dispose();
    }
}
