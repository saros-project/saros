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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

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
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager.Side;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The ActivitySequencer is responsible for making sure that activities are sent
 * and received in the right order.
 * 
 * @author rdjemili
 * @author coezbek
 */
public class ActivitySequencer implements IActivityListener, IActivityManager {

    private static Logger log = Logger.getLogger(ActivitySequencer.class
        .getName());

    private final List<IActivity> activities = new LinkedList<IActivity>();

    private final List<IActivityProvider> providers = new LinkedList<IActivityProvider>();

    /**
     * A priority queue for timed activities.
     * 
     * TODO "Timestamps" are treated more like consecutive sequence numbers, so
     * may be all names and documentation should be changed to reflect this.
     */
    class ActivityQueue {

        /** Timestamp expected from the next activity. */
        private int expectedTimestamp;
        private PriorityQueue<TimedActivity> queuedActivities;

        public ActivityQueue(int expectedTimestamp) {
            this.expectedTimestamp = expectedTimestamp;
            this.queuedActivities = new PriorityQueue<TimedActivity>(128,
                new Comparator<TimedActivity>() {
                    public int compare(TimedActivity t1, TimedActivity t2) {
                        return t1.getTimestamp() - t2.getTimestamp();
                    }
                });
        }

        public void add(TimedActivity activity) {
            if (activity.getTimestamp() < expectedTimestamp) {
                log.warn("Received activity more than once: " + activity);
                return;
            }
            int size = queuedActivities.size();
            if (size > 0) {
                log.debug("There are " + size + " activities queued for "
                    + activity.getSource());
                log.debug("first queued: " + queuedActivities.peek()
                    + ", expected nr: " + expectedTimestamp);
            }
            queuedActivities.add(activity);
        }

        /**
         * @return The next activity if there is one and it carries the expected
         *         timestamp, otherwise <code>null</code>.
         */
        public TimedActivity getNext() {
            if (queuedActivities.size() > 0
                && queuedActivities.peek().getTimestamp() == expectedTimestamp) {
                expectedTimestamp++;
                return queuedActivities.remove();
            }
            return null;
        }
    }

    /**
     * This class manages a {@link ActivityQueue} for each other user of a
     * session.
     */
    class ActivityQueuesManager {
        protected Map<JID, ActivityQueue> jid2queue;

        public ActivityQueuesManager() {
            this.jid2queue = new HashMap<JID, ActivityQueue>();
        }

        /**
         * Adds a {@link TimedActivity}. There must be a source set on the
         * activity.
         * 
         * @param activity
         *            to add to the qeues.
         * 
         * @throws IllegalArgumentException
         *             if the source of the activity is <code>null</code>.
         */
        public void add(TimedActivity activity) {
            String source = activity.getSource();
            if (source == null) {
                throw new IllegalArgumentException("Source must not be null");
            }
            JID jid = new JID(source);
            ActivityQueue activityQueue = jid2queue.get(jid);
            if (activityQueue == null) {
                activityQueue = new ActivityQueue(activity.getTimestamp());
                jid2queue.put(jid, activityQueue);
            }
            activityQueue.add(activity);
        }

        // TODO Invoke this method when a user leaves the session.
        public void removeQueue(JID jid) {
            jid2queue.remove(jid);
        }

        /**
         * @return all activities that can be executed. If there are none, an
         *         empty List is returned.
         */
        public List<TimedActivity> getActivities() {
            ArrayList<TimedActivity> result = new ArrayList<TimedActivity>();
            for (ActivityQueue queue : jid2queue.values()) {
                TimedActivity activity;
                while ((activity = queue.getNext()) != null) {
                    result.add(activity);
                }
            }
            return result;
        }
    }

    private final ActivityQueuesManager queues = new ActivityQueuesManager();

    private final List<TimedActivity> activityHistory = new LinkedList<TimedActivity>();

    private int timestamp = 0;

    private ConcurrentDocumentManager concurrentDocumentManager;

    /**
     * outgoing queue for direct client sync messages for all driver.
     */
    private final BlockingQueue<Pair<JID, Request>> outgoingSyncActivities = new LinkedBlockingQueue<Pair<JID, Request>>();

    /**
     * TODO Refactor like this:
     * 
     * <code>
     * concurrentManager.exec(activity); 
     * editorManager.exec(activity);
     * roleManager.exec(activity); 
     * sharedResourceManager.exec(activity);
     * </code>
     * 
     * Is easier to read and debug :-) But watch out for interdependencies
     * between these.
     */
    public void exec(final IActivity activity) {

        try {
            if (activity instanceof EditorActivity) {
                this.concurrentDocumentManager.execEditorActivity(activity);
            }
            if (activity instanceof FileActivity) {
                this.concurrentDocumentManager.execFileActivity(activity);
            }
            if (activity instanceof FolderActivity) {
                // TODO
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
         * TODO Here we may get into trouble with asynchronously send file
         * contents. The editor activated activity may arrive before the actual
         * file data.
         * 
         * The "dead" code below doesn't work because the time stamps of
         * activities gone through Jupiter don't match the source in the
         * activity, with the exception of activities originally generated by
         * the host.
         */
        if (true) {
            exec(nextActivity.getActivity());
            return;
        }

        synchronized (queues) {
            queues.add(nextActivity);
            for (TimedActivity activity : queues.getActivities()) {
                exec(activity.getActivity());
            }
        }
    }

    public List<IActivity> flush() {
        List<IActivity> out = new ArrayList<IActivity>(this.activities);
        this.activities.clear();
        return optimize(out);
    }

    /**
     * Gets all activities since last flush.
     * 
     * @return the activities that have accumulated since the last flush.
     */
    public List<TimedActivity> flushTimed() {
        List<IActivity> activities = flush();
        List<TimedActivity> timedActivities = new ArrayList<TimedActivity>(
            activities.size());
        for (IActivity activity : activities) {
            timedActivities.add(new TimedActivity(activity, this.timestamp++));
        }

        return timedActivities;
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

    public int getTimestamp() {
        return this.timestamp;
    }

    public int getQueuedActivitiesSize() {
        // TODO Return the number of activities stored in this.queues here.
        return 0;
    }

    public List<TimedActivity> getActivityHistory() {
        return this.activityHistory;
    }

    /**
     * This method tries to reduce the number of activities transmitted by
     * removing activities that would overwrite each other and joining
     * activities that can be send as a single activity.
     */
    private List<IActivity> optimize(List<IActivity> toOptimize) {

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

    private TextEditActivity joinTextEdits(List<IActivity> result,
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
                textEdit = new TextEditActivity(lastTextEdit.offset,
                    lastTextEdit.text + textEdit.text,
                    lastTextEdit.replacedText + textEdit.replacedText,
                    lastTextEdit.getEditor(), lastTextEdit.getSource());
            }
        }

        return textEdit;
    }

    public void initConcurrentManager(Side side, User host, JID myJID,
        ISharedProject sharedProject) {
        this.concurrentDocumentManager = new ConcurrentDocumentManager(side,
            host, myJID, sharedProject, this);
    }

    public ConcurrentDocumentManager getConcurrentDocumentManager() {
        return this.concurrentDocumentManager;
    }

    public synchronized void forwardOutgoingRequest(JID to, Request req) {
        /* put request into outgoing queue. */
        this.outgoingSyncActivities.add(new Pair<JID, Request>(to, req));
    }

    public Pair<JID, Request> getNextOutgoingRequest()
        throws InterruptedException {
        return this.outgoingSyncActivities.take();
    }

    /**
     * Execute activity after jupiter transforming process.
     * 
     * @param activity
     */
    public void execTransformedActivity(IActivity activity) {
        try {
            log.debug("execute transformed activity: " + activity);

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
                log.debug("send transformed activity: " + activity);
                this.activities.add(activity);
            }
        } catch (Exception e) {
            log.error("Error while executing activity.", e);
        }
    }

    /**
     * Given a List of TimedActivities it will either return the Activity for
     * the given timestamp (andup == false) or all activities that have a
     * timestamp < than the given (andup == true).
     * 
     * If not Activities can be found an empty list is returned.
     */
    public static List<TimedActivity> filterActivityHistory(
        List<TimedActivity> toFilter, int timestamp, boolean andup) {

        List<TimedActivity> result = new LinkedList<TimedActivity>();

        if (andup) {
            for (TimedActivity tact : toFilter) {
                if (tact.getTimestamp() >= timestamp) {
                    result.add(tact);
                }
            }
        } else {
            for (TimedActivity tact : toFilter) {
                if (tact.getTimestamp() == timestamp) {
                    result.add(tact);
                    // Found the one we were looking for
                    return result;
                }
            }
        }

        return result;
    }
}
