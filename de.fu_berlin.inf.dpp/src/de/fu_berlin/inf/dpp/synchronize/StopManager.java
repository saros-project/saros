package de.fu_berlin.inf.dpp.synchronize;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.StopActivity;
import de.fu_berlin.inf.dpp.activities.business.StopActivity.State;
import de.fu_berlin.inf.dpp.activities.business.StopActivity.Type;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The @StopManager class is used to coordinate blocking of user input between
 * different Saros Users. Blocking the user input is not implemented by this
 * class but by classes that implement the @Blockable interface and have
 * registered themselves with the StopManager by calling {@link #addBlockable}.
 * 
 * There are two variants of the {@link #stop} method. One is working on a
 * single Saros User and the other is working on a collection of them. The
 * guarantee the StopManager makes is that at the end of the execution of the
 * {@link #stop} method all Saros Users are stopped or all of them are started.
 * 
 * A StartHandle will be returned for each stopped user, it can be used to
 * remove the block of remote users.
 */
@Component(module = "core")
public final class StopManager extends AbstractActivityProvider implements
    Startable {

    private static final Logger log = Logger.getLogger(StopManager.class);

    private static final Random RANDOM = new Random();

    // Waits MILLISTOWAIT ms until the next test for progress cancellation
    public final int MILLISTOWAIT = 100;

    protected List<Blockable> blockables = new CopyOnWriteArrayList<Blockable>();

    protected ObservableValue<Boolean> blocked = new ObservableValue<Boolean>(
        false);

    protected final ISarosSession sarosSession;

    /**
     * Maps a User to a List of his StartHandles. Never touch this directly, use
     * the add and remove methods.
     */
    private Map<User, List<StartHandle>> startHandles = Collections
        .synchronizedMap(new HashMap<User, List<StartHandle>>());

    /**
     * For every initiated unlock (identified by its StopActivity id) there is
     * one acknowledgment expected.
     */
    private Map<String, StartHandle> startsToBeAcknowledged = Collections
        .synchronizedMap(new HashMap<String, StartHandle>());

    /**
     * Blocking mechanism. The call to stop wants to block until the
     * acknowledgment for the StopActivity arrives at our anonymous
     * {@link AbstractActivityReceiver} implementation. We use a wait condition
     * for that. There might be multiple threads that wait for this event to
     * happen. The lock is not used to protect any data structure.
     */
    protected Lock reentrantLock = new ReentrantLock();
    protected final Condition acknowledged = reentrantLock.newCondition();

    /**
     * For every initiated StopActivity (type: LockRequest) there is one
     * acknowledgment expected.
     */
    protected Set<StopActivity> expectedAcknowledgments = Collections
        .synchronizedSet(new HashSet<StopActivity>());

    public StopManager(ISarosSession session) {
        this.sarosSession = session;
    }

    /**
     * @JTourBusStop 9, Activity sending, Triple dispatch:
     * 
     *               This class extends the AbstractActivityReceiver and
     *               overrides the method with the right overload.
     */

    /**
     * @JTourBusStop 2, StopManager:
     * 
     *               This is where lock/unlock requests and acknowledgments will
     *               be handled. When there are outgoing lock requests the
     *               expected answers will be put into the
     *               expectedAcknowledgements set and when the acknowledgment
     *               arrives it will be removed from the set. For incoming lock
     *               requests lockProject(true) will be called.
     */
    protected IActivityReceiver activityDataObjectReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(final StopActivity stopActivity) {
            assert sarosSession != null;

            User user = stopActivity.getRecipient();
            if (!user.isInSarosSession() || user.isRemote())
                throw new IllegalArgumentException(
                    "Received StopActivity which is not for the local user");

            if (stopActivity.getType() == Type.LOCKREQUEST) {

                /*
                 * local user locks his session and adds a startHandle so he
                 * knows he is locked. Then he acknowledges.
                 */
                if (stopActivity.getState() == State.INITIATED) {
                    addStartHandle(generateStartHandle(stopActivity));
                    // locks session and acknowledges

                    lockSession(true);
                    fireActivity(stopActivity
                        .generateAcknowledgment(sarosSession.getLocalUser()));

                    return;
                }
                if (stopActivity.getState() == State.ACKNOWLEDGED) {
                    if (!expectedAcknowledgments.contains(stopActivity)) {
                        log.warn("Received unexpected StopActivity: "
                            + stopActivity);
                        return;
                    }

                    /**
                     * Remove from the expectedAcknowledgements set and inform
                     * who ever has been waiting for that to happen. Warn if the
                     * removal is failing besides the above check.
                     */
                    if (!expectedAcknowledgments.remove(stopActivity)) {
                        log.warn("Received unexpected "
                            + "StopActivity acknowledgement: " + stopActivity);
                        return;
                    }

                    reentrantLock.lock();
                    acknowledged.signalAll();
                    reentrantLock.unlock();
                    return;
                }
            }

            if (stopActivity.getType() == Type.UNLOCKREQUEST) {
                if (stopActivity.getState() == State.INITIATED) {

                    executeUnlock(generateStartHandle(stopActivity));
                    // sends an acknowledgment
                    fireActivity(stopActivity
                        .generateAcknowledgment(sarosSession.getLocalUser()));
                    return;
                }

                if (stopActivity.getState() == State.ACKNOWLEDGED) {
                    StartHandle handle = startsToBeAcknowledged
                        .remove(stopActivity.getActivityID());
                    if (handle == null) {
                        log.error("StartHandle for " + stopActivity
                            + " could not be found.");
                        return;
                    }
                    handle.acknowledge();
                    return;
                }
            }

            throw new IllegalArgumentException(
                "StopActivity is of unknown type: " + stopActivity);
        }
    };

    /**
     * Blocking method that asks the given users to halt all user-input and
     * returns a list of handles to be used when the users can start again.
     * 
     * @param users
     *            the participants who has to stop
     * @param cause
     *            the cause for stopping as it is displayed in the progress
     *            monitor
     * 
     * @param monitor
     *            The caller is expected to call beginTask and done on the given
     *            SubMonitor
     * 
     * @noSWT This method mustn't be called from the SWT thread.
     * 
     * @blocking returning after the given users acknowledged the stop
     * 
     * @cancelable This method can be canceled by the user
     * 
     * @throws CancellationException
     */
    public List<StartHandle> stop(final Collection<User> users,
        final String cause, final IProgressMonitor monitor)
        throws CancellationException {

        final List<StartHandle> resultingHandles = Collections
            .synchronizedList(new LinkedList<StartHandle>());
        final LinkedList<Thread> threads = new LinkedList<Thread>();

        for (final User user : users) {
            threads.add(Utils.runSafeAsync("BlockUser-" + user + "-" + cause,
                log, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (monitor.isCanceled())
                                return;

                            StartHandle startHandle = stop(user, cause, monitor);
                            resultingHandles.add(startHandle);
                            log.debug("added " + startHandle
                                + " to resulting handles.");
                        } catch (CancellationException e) {
                            log.debug("user canceled the stopping");
                            monitor.setCanceled(true);
                        } catch (InterruptedException e) {
                            log.debug("canceling because of an InterruptedException");
                            monitor.setCanceled(true);
                        }
                    }
                }));
        }

        /**
         * We have started all threads and we will wait for all of them to
         * finish now. This is the safest and most simple approach to avoid
         * ending up with ConcurrentModificationExceptions that might happen
         * when we resume the remote users while some threads still need to be
         * executed.
         */
        while (!threads.isEmpty()) {
            try {
                threads.getFirst().join();
                threads.removeFirst();
            } catch (InterruptedException e) {
                // We need to ignore this right now as we would end up with
                // inconsistent state at the end of this method. Some remote
                // Users might be blocked already and would remain blocked. We
                // will just need to try again until all threads have terminated
            }
        }

        if (monitor.isCanceled()) {
            // Restart the already stopped users
            log.debug("Monitor was canceled. Restarting already stopped buddies.");
            for (StartHandle startHandle : resultingHandles)
                startHandle.start();
            throw new CancellationException();
        }
        return resultingHandles;
    }

    /**
     * @JTourBusStop 1, StopManager:
     * 
     *               Sometimes it is necessary to prevent others from making
     *               modifications, e.g. during the OutgoingProjectNegotiation
     *               or during the recovery by ConsistencyWatchdogHandler and
     *               this class is responsible for managing this process.
     *               Objects that want to implement a lock need to register a
     *               Blockable with the StopManager, the Blockable will be
     *               called when the StopManager is locked or unlocked. The stop
     *               method will either stop a single user or a list of users
     *               and then will return a single handle or a list of handles.
     */

    /**
     * Blocking method that asks the given user to halt all user-input and
     * returns a handle to be used when the user can start again.
     * 
     * @param user
     *            the participant who has to stop
     * @param cause
     *            the cause for stopping as it is displayed in the progress
     *            monitor
     * 
     * @param progress
     *            The caller is expected to call beginTask and done on the given
     *            SubMonitor
     * 
     * @noSWT This method mustn't be called from the SWT thread.
     * 
     * @blocking returning after the given user acknowledged the stop
     * 
     * @cancelable This method can be canceled by the user
     * 
     * @throws CancellationException
     * @throws InterruptedException
     */
    public StartHandle stop(User user, String cause,
        final IProgressMonitor progress) throws CancellationException,
        InterruptedException {
        assert sarosSession != null;

        // Creating StopActivity for asking user to stop
        User localUser = sarosSession.getLocalUser();
        final StopActivity stopActivity = new StopActivity(localUser,
            localUser, user, Type.LOCKREQUEST, State.INITIATED,
            new SimpleDateFormat("HHmmssSS").format(new Date())
                + RANDOM.nextLong());

        StartHandle handle = generateStartHandle(stopActivity);
        addStartHandle(handle);

        // Short cut if affected user is local
        if (user.isLocal()) {
            lockSession(true);
            return handle;
        }

        StopActivity expectedAck = stopActivity.generateAcknowledgment(user);
        expectedAcknowledgments.add(expectedAck);

        fireActivity(stopActivity);

        // Block until user acknowledged
        log.debug("Waiting for acknowledgment " + Utils.prefix(user.getJID()));
        reentrantLock.lock();
        try {
            while (expectedAcknowledgments.contains(expectedAck)
                && !progress.isCanceled() && user.isInSarosSession()) {
                if (acknowledged.await(MILLISTOWAIT, TimeUnit.MILLISECONDS)) {
                    continue; /*
                               * Used to make FindBugs happy that we don't do
                               * anything if we are woken up
                               */
                }
            }
            // The monitor was canceled or the user has left the session.
            if (expectedAcknowledgments.contains(expectedAck)) {
                log.warn("No acknowlegment arrived, gave up waiting");
                expectedAcknowledgments.remove(expectedAck);
                handle.start();
                throw new CancellationException();
            }
            log.debug("Acknowledgment arrived " + Utils.prefix(user.getJID()));
        } catch (InterruptedException e) {
            handle.start();
            throw new InterruptedException();
        } finally {
            reentrantLock.unlock();
        }
        return handle;
    }

    /**
     * The goal of this method is to ensure that the local user cannot cause any
     * editing activityDataObjects (FileActivities and TextEditActivities).
     * 
     * @param lock
     *            if true the session gets locked, else it gets unlocked
     */
    // TODO: Make private when StoppedAction is removed.
    public void lockSession(boolean lock) {
        for (Blockable blockable : blockables) {
            if (lock)
                blockable.block();
            else
                blockable.unblock();
        }
        blocked.setValue(lock);
    }

    /**
     * Unlocks session without sending an acknowledgment if there don't exist
     * any more startHandles.
     * 
     * @return true if the affected user is unlocked afterwards
     */
    private boolean executeUnlock(StartHandle startHandle) {

        if (!startHandle.getUser().isLocal())
            throw new IllegalArgumentException(
                "ExecuteUnlock may only be called with a StartHandle for the local user");

        if (!removeStartHandle(startHandle)) {
            /*
             * Ok if the local user was the initiator of the stop. If not
             * something is wrong.
             */
            log.debug(startHandle
                + " couldn't be removed because it doesn't exist any more.");
        }

        int remainingHandles = getStartHandles(sarosSession.getLocalUser())
            .size();
        if (remainingHandles > 0) {
            log.debug(remainingHandles + " startHandles remaining.");
            return false;
        }

        lockSession(false);
        return true;
    }

    /**
     * Sends an initiated unlock request.
     * 
     * @param handle
     *            the startHandle whose start() initiated the unlocking
     */
    private void initiateUnlock(StartHandle handle) {
        assert sarosSession != null;

        // short cut for local user
        if (handle.getUser().isLocal()) {
            executeUnlock(handle);
            return;
        }

        startsToBeAcknowledged.put(handle.getHandleID(), handle);

        final StopActivity activity = new StopActivity(
            sarosSession.getLocalUser(), sarosSession.getLocalUser(),
            handle.getUser(), Type.UNLOCKREQUEST, State.INITIATED,
            handle.getHandleID());

        /**
         * @JTourBusStop 4, Activity sending, Firing the activity:
         * 
         *               The following fires the activity and all listeners will
         *               be called.
         */
        fireActivity(activity);
    }

    /**
     * @JTourBusStop 8, Activity sending, Handling activities:
     * 
     *               The activity dispatcher of the session has called us. This
     *               class will use the triple dispatch to filter for the
     *               activities the class is interested in.
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(IActivity activityDataObject) {
        activityDataObject.dispatch(activityDataObjectReceiver);
    }

    /**
     * Adds a StartHandle to startHandles, which maps a user to a list of
     * StartHandles. These Lists are created lazily.
     */
    private void addStartHandle(StartHandle startHandle) {
        getStartHandles(startHandle.getUser()).add(startHandle);
    }

    /**
     * Removes a StartHandle from startHandles. If the list for user is empty
     * then the user is removed from startHandles.
     * 
     * @return false if the given startHandle didn't exist in Map, true
     *         otherwise
     */
    private boolean removeStartHandle(StartHandle startHandle) {
        return getStartHandles(startHandle.getUser()).remove(startHandle);
    }

    private synchronized List<StartHandle> getStartHandles(User user) {

        List<StartHandle> result = startHandles.get(user);
        if (result == null) {
            result = new CopyOnWriteArrayList<StartHandle>();
            startHandles.put(user, result);
        }
        return result;
    }

    private StartHandle generateStartHandle(StopActivity stopActivity) {
        User user = stopActivity.getUser();
        return new StartHandle(user, this, stopActivity.getActivityID());
    }

    public void addBlockable(Blockable stoppable) {
        blockables.add(stoppable);
    }

    public void removeBlockable(Blockable stoppable) {
        blockables.remove(stoppable);
    }

    public ObservableValue<Boolean> getBlockedObservable() {
        return blocked;
    }

    public void sessionStopped() {
        lockSession(false);
        clearExpectedAcknowledgments();
    }

    private void clearExpectedAcknowledgments() {
        /**
         * Clear the expectedAcknowledgements and inform the threads that are
         * blocked in the stop method that there will be no response.
         */
        reentrantLock.lock();
        try {
            expectedAcknowledgments.clear();
            acknowledged.signalAll();
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Resume the StartHandle and remove it from internal lists.
     * 
     * @param startHandle
     * @return Returns true if the last handle for the user has been removed.
     */
    synchronized boolean resumeStartHandle(StartHandle startHandle) {
        removeStartHandle(startHandle);
        initiateUnlock(startHandle);
        return getStartHandles(startHandle.getUser()).isEmpty();
    }

    @Override
    public void start() {
        /**
         * @JTourBusStop 3, Activity sending, An example of an
         *               IActivityProvider:
         * 
         *               This is the canonical way of registering an activity
         *               provider with the session. The session will install a
         *               listener on this provider.
         */
        sarosSession.addActivityProvider(this);
    }

    @Override
    public void stop() {
        sarosSession.removeActivityProvider(this);
        sessionStopped();
    }
}
