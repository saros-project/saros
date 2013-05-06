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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity.Type;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
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
public class ActivitySequencer implements Startable {

    private static final Logger log = Logger.getLogger(ActivitySequencer.class
        .getName());

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

    /** Special queue item that termins the processing */
    private static final DataObjectQueueItem POISON = new DataObjectQueueItem(
        (User) null, null);

    /** Buffer for outgoing activityDataObjects. */
    private final BlockingQueue<DataObjectQueueItem> outgoingQueue = new LinkedBlockingQueue<DataObjectQueueItem>();

    private final ActivityQueueManager incomingQueues;

    /**
     * Whether this AS currently sends or receives events
     */
    private boolean started = false;

    private Thread activitySendThread;

    private final ISarosSession sarosSession;

    private final SessionIDObservable sessionIDObservable;

    private final ITransmitter transmitter;

    private final JID localJID;

    private final DispatchThreadContext dispatchThread;

    public ActivitySequencer(final ISarosSession sarosSession,
        final ITransmitter transmitter,
        final DispatchThreadContext threadContext,
        final SessionIDObservable sessionIDObservable) {

        this.dispatchThread = threadContext;
        this.sarosSession = sarosSession;
        this.transmitter = transmitter;
        this.sessionIDObservable = sessionIDObservable;

        this.localJID = sarosSession.getLocalUser().getJID();

        this.incomingQueues = new ActivityQueueManager(localJID);
    }

    /**
     * Used by the unit test
     */
    public boolean isStarted() {
        return started;
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
    @Override
    public void start() {

        if (started) {
            throw new IllegalStateException();
        }

        started = true;

        activitySendThread = Utils.runSafeAsync("ActivitySender", log,
            new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean abort = false;
                        while (!abort)
                            abort = flushTask();
                    } catch (InterruptedException e) {
                        log.error("Flush got interrupted.", e);
                    }
                }

                private boolean flushTask() throws InterruptedException {

                    List<DataObjectQueueItem> activities = new ArrayList<DataObjectQueueItem>(
                        outgoingQueue.size());

                    activities.add(outgoingQueue.take());

                    // If there was more than one ADO waiting, get the rest now.
                    outgoingQueue.drainTo(activities);

                    boolean abort = false;

                    int idx = activities.indexOf(POISON);

                    if (idx != -1) {
                        abort = true;

                        /*
                         * remove the poison pill and all activities after the
                         * pill
                         */
                        while (activities.size() != idx)
                            activities.remove(idx);
                    }

                    Map<User, List<IActivityDataObject>> toSend = AutoHashMap
                        .getListHashMap();

                    for (DataObjectQueueItem item : activities) {
                        for (User recipient : item.recipients) {
                            toSend.get(recipient).add(item.activityDataObject);
                        }
                    }

                    for (Entry<User, List<IActivityDataObject>> e : toSend
                        .entrySet()) {
                        sendActivities(e.getKey(), optimize(e.getValue()));
                    }

                    return abort;
                }

                /**
                 * Sends given activityDataObjects to given recipient.
                 * 
                 * @private because this method must not be called from
                 *          somewhere else than this TimerTask.
                 * 
                 * @throws IllegalArgumentException
                 *             if the recipient is the local user or the
                 *             activityDataObjects contain <code>null</code>.
                 */
                private void sendActivities(User recipient,
                    List<IActivityDataObject> activityDataObjects) {

                    if (recipient.isLocal()) {
                        throw new IllegalArgumentException(
                            "Sending a message to the local user is not supported");
                    }

                    if (activityDataObjects.contains(null)) {
                        throw new IllegalArgumentException(
                            "Cannot send a null activityDataObject");
                    }

                    // Don't send activities to peers that are not in the
                    // session
                    // (anymore)
                    if (!recipient.isInSarosSession()) {
                        log.warn("Activities for peer not in session are dropped.");
                        return;
                    }

                    JID recipientJID = recipient.getJID();

                    List<TimedActivityDataObject> timedActivities = createTimedActivities(
                        recipientJID, activityDataObjects);

                    log.trace("Sending " + timedActivities.size()
                        + " activities to " + recipientJID + ": "
                        + timedActivities);

                    sendTimedActivities(recipientJID, timedActivities);
                }
            });

    }

    /**
     * Stop periodical flushing and sending of outgoing activityDataObjects and
     * checking for received activityDataObjects that are queued for too long.
     * 
     * @see #start()
     */
    @Override
    public void stop() {
        if (!started) {
            throw new IllegalStateException();
        }

        /**
         * Try to poison the flush task using the "Poison Pill" as known from
         * the Java Concurrency in Practice book.
         */
        while (true) {
            try {
                outgoingQueue.put(POISON);
                activitySendThread.join();
                break;
            } catch (InterruptedException e) {
                //
            }
        }

        activitySendThread = null;
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
                log.trace("dispatching activity object " + activityDataObject
                    + " to the local user: " + user.getJID());

                dispatchThread.executeAsDispatch(new Runnable() {
                    @Override
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
     * @param user
     *            the user that left.
     */
    public void userLeft(User user) {
        incomingQueues.removeQueue(user.getJID());
    }

    private void sendTimedActivities(JID recipient,
        List<TimedActivityDataObject> timedActivities) {

        if (recipient == null
            || recipient.equals(sarosSession.getLocalUser().getJID())) {
            throw new IllegalArgumentException(
                "Recipient may not be null or equal to the local user");
        }
        if (timedActivities == null || timedActivities.size() == 0) {
            throw new IllegalArgumentException(
                "TimedActivities may not be null or empty");
        }

        String sID = sessionIDObservable.getValue();

        PacketExtension extensionToSend = ActivitiesExtension.PROVIDER.create(
            sID, timedActivities);

        String msg = "Sent (" + String.format("%03d", timedActivities.size())
            + ") " + Utils.prefix(recipient) + timedActivities;

        // only log on debug level if there is more than a checksum
        if (ActivityUtils.containsChecksumsOnly(timedActivities))
            log.trace(msg);
        else
            log.debug(msg);

        try {
            transmitter.sendToSessionUser(recipient, extensionToSend);
        } catch (IOException e) {
            log.error("Failed to sent activityDataObjects: " + timedActivities,
                e);
        }
    }
}
