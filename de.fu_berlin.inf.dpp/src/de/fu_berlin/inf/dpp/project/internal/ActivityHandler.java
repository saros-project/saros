package de.fu_berlin.inf.dpp.project.internal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.window.Window;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.management.TransformationResult;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This handler is responsible for handling the correct thread access when
 * transforming activities with the {@link ConcurrentDocumentServer} and
 * {@link ConcurrentDocumentClient}. The sending and executing of activities
 * <b>must</b> be done in {@linkplain IActivityHandlerCallback callback} as it
 * is <b>not</b> performed by this handler !
 * 
 * @author Stefan Rossbach
 */
public final class ActivityHandler implements Startable {

    private static final Logger LOG = Logger.getLogger(ActivityHandler.class);

    /** join timeout when stopping this component */
    private static final long TIMEOUT = 10000;

    private static final int DISPATCH_MODE_SYNC = 0;

    private static final int DISPATCH_MODE_ASYNC = 1; // Experimental

    private static final int DISPATCH_MODE;

    static {
        int dispatchModeToUse = Integer.getInteger(
            "de.fu_berlin.inf.dpp.session.ACTIVITY_DISPATCH_MODE",
            DISPATCH_MODE_SYNC);

        if (dispatchModeToUse != DISPATCH_MODE_ASYNC)
            dispatchModeToUse = DISPATCH_MODE_SYNC;

        DISPATCH_MODE = dispatchModeToUse;
    }

    public static class QueueItem {

        public final List<User> recipients;
        public final IActivity activity;

        public QueueItem(List<User> recipients, IActivity activity) {
            if (recipients.size() == 0)
                LOG.fatal("empty list of recipients in constructor",
                    new StackTrace());
            this.recipients = recipients;
            this.activity = activity;
        }

        public QueueItem(User host, IActivity activity) {
            this(Collections.singletonList(host), activity);
        }
    }

    private final LinkedBlockingQueue<List<IActivity>> dispatchQueue = new LinkedBlockingQueue<List<IActivity>>();

    private final IActivityHandlerCallback callback;

    private final ISarosSession session;

    private final ConcurrentDocumentServer documentServer;

    private final ConcurrentDocumentClient documentClient;

    private final UISynchronizer synchronizer;

    /*
     * We must use a thread for synchronous execution otherwise we would block
     * the DispatchThreadContext which handles the dispatching of all network
     * packets
     */
    private Thread dispatchThread;

    private final Runnable dispatchThreadRunnable = new Runnable() {

        @Override
        public void run() {
            LOG.debug("activity dispatcher started");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    dispatchAndExecuteActivities(dispatchQueue.take());
                } catch (InterruptedException e) {
                    break;
                }
            }
            LOG.debug("activity dispatcher stopped");
        }
    };

    public ActivityHandler(ISarosSession session,
        IActivityHandlerCallback callback,
        ConcurrentDocumentServer documentServer,
        ConcurrentDocumentClient documentClient, UISynchronizer synchronizer) {
        this.session = session;
        this.callback = callback;
        this.documentServer = documentServer;
        this.documentClient = documentClient;
        this.synchronizer = synchronizer;
    }

    /**
     * Transforms and dispatches the activities. The
     * {@linkplain IActivityHandlerCallback callback} will be notified about the
     * results.
     * 
     * @param activities
     *            an <b>immutable</b> list containing the activities
     */
    public synchronized void handleIncomingActivities(List<IActivity> activities) {
        if (session.isHost()) {
            TransformationResult transformed = documentServer
                .transformIncoming(activities);

            activities = transformed.getLocalActivities();

            for (QueueItem item : transformed.getSendToPeers())
                callback.send(item.recipients, item.activity);
        }

        if (activities.isEmpty())
            return;

        if (DISPATCH_MODE == DISPATCH_MODE_ASYNC)
            dispatchAndExecuteActivities(activities);
        else
            dispatchQueue.add(activities);
    }

    /**
     * @JTourBusStop 6, Activity sending, Transforming the IActivity:
     * 
     *               This function will transform activities and then forward
     *               them to the callback. E.g. this will turn TextEditActivity
     *               into Jupiter actitivities.
     */

    /**
     * Transforms and determines the recipients of the activities. The
     * {@linkplain IActivityHandlerCallback callback} will be notified about the
     * results.
     * 
     * @param activities
     *            an <b>immutable</b> list containing the activities
     */
    /*
     * Note: transformation and executing has to be performed together in the
     * SWT thread. Else, it would be possible that local activities are executed
     * between transformation and application of remote operations. In other
     * words, the transformation would be applied to an out-dated state.
     */
    public void handleOutgoingActivities(final List<IActivity> activities) {
        synchronizer.syncExec(Utils.wrapSafe(LOG, new Runnable() {

            @Override
            public void run() {
                for (IActivity activity : activities) {
                    for (QueueItem item : documentClient
                        .transformOutgoing(activity)) {
                        callback.send(item.recipients, item.activity);
                    }
                }
            }
        }));
    }

    @Override
    public void start() {
        if (DISPATCH_MODE == DISPATCH_MODE_ASYNC)
            return;

        dispatchThread = new Thread(Utils.wrapSafe(LOG, dispatchThreadRunnable));
        dispatchThread.setName("Activity-Dispatcher");
        dispatchThread.start();
    }

    @Override
    public void stop() {
        if (DISPATCH_MODE == DISPATCH_MODE_ASYNC)
            return;

        dispatchThread.interrupt();
        try {
            dispatchThread.join(TIMEOUT);
        } catch (InterruptedException e) {
            LOG.warn("interrupted while waiting for "
                + dispatchThread.getName() + " thread to terminate");

            Thread.currentThread().interrupt();
        }

        if (dispatchThread.isAlive())
            LOG.error(dispatchThread.getName() + " thread is still running");
    }

    /**
     * 
     * Executes the current activities by dispatching the received activities to
     * the SWT EDT.
     * 
     * We must use synchronous dispatching as it is possible that some handlers
     * or Eclipse itself open dialogs during the execution of an activity.
     * 
     * If the current activity list would be dispatched asynchronously it is
     * possible that further activities may be executed during the currently
     * executed activity list and so leading up to unknown errors.
     * 
     * <pre>
     * Activities to execute:
     * [A, B, C, D, E, F, G, H]
     *           ^
     *           |
     *           --> is currently blocked by a dialog
     * 
     * new activities arrive
     * [J, K, L, M]
     * 
     * final execution order can be:
     * 
     * [A, B, C, D_B, J, K, L, M, D_A, E, F, G, H]
     * 
     * Where D_B(efore) is the code that has been executed before
     * entering the modal context and D_A(fter) the code after
     * leaving the modal context.
     * 
     * Note: If the next activities list also contains an activity that
     * uses a modal context the execution chain will become even less
     * predictable !
     * </pre>
     * 
     * 
     * @see ModalContext
     * @see Window#setBlockOnOpen(boolean shouldBlock)
     * @see IRunnableContext#run(boolean fork, boolean cancelable,
     *      IRunnableWithProgress runnable)
     * @param activities
     *            the activities to execute
     */
    /*
     * Note: transformation and executing has to be performed together in the
     * SWT thread. Else, it would be possible that local activities are executed
     * between transformation and application of remote operations. In other
     * words, the transformation would be applied to an out-dated state.
     */
    private void dispatchAndExecuteActivities(final List<IActivity> activities) {
        Runnable transformingRunnable = new Runnable() {
            @Override
            public void run() {
                TransformationResult transformed = documentClient
                    .transformIncoming(activities);

                for (QueueItem item : transformed.getSendToPeers())
                    callback.send(item.recipients, item.activity);

                for (IActivity activity : transformed.executeLocally)
                    callback.execute(activity);
            }
        };

        if (LOG.isTraceEnabled())
            LOG.trace("dispatching " + activities.size()
                + " activities [mode = " + DISPATCH_MODE + "] : " + activities);

        if (DISPATCH_MODE == DISPATCH_MODE_SYNC)
            synchronizer.syncExec(Utils.wrapSafe(LOG, transformingRunnable));
        else
            synchronizer.asyncExec(Utils.wrapSafe(LOG, transformingRunnable));
    }
}
