package de.fu_berlin.inf.dpp.synchronize;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.StopActivity;
import de.fu_berlin.inf.dpp.activities.StopActivity.State;
import de.fu_berlin.inf.dpp.activities.StopActivity.Type;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

@Component(module = "core")
public class StopManager implements IActivityProvider, Disposable {

    private static Logger log = Logger.getLogger(StopManager.class.getName());

    // waits MILLISTOWAIT ms until the next test for progress cancellation
    protected final int MILLISTOWAIT = 100;

    private final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    protected List<Blockable> blockables = new LinkedList<Blockable>();

    protected ISharedProject sharedProject;

    SharedProjectObservable sharedProjectObservable;

    /**
     * Maps a User to a List of his StartHandles. Never touch this directly, use
     * the add and remove methods.
     */
    private Map<User, List<StartHandle>> startHandles = Collections
        .synchronizedMap(new HashMap<User, List<StartHandle>>());

    // blocking mechanism
    protected Lock reentrantLock = new ReentrantLock();
    protected final Condition acknowledged = reentrantLock.newCondition();

    /**
     * for every initiated StopActivity (type: LockRequest) there is one
     * acknowledgment expected
     */
    protected Set<StopActivity> expectedAcknowledgments = Collections
        .synchronizedSet(new HashSet<StopActivity>());

    protected ValueChangeListener<SharedProject> sharedProjectObserver = new ValueChangeListener<SharedProject>() {
        public void setValue(SharedProject newSharedProject) {

            if (newSharedProject == StopManager.this.sharedProject)
                return;

            if (StopManager.this.sharedProject != null) {
                StopManager.this.sharedProject.getSequencer().removeProvider(
                    StopManager.this);
                reset();
            }

            StopManager.this.sharedProject = newSharedProject;

            if (newSharedProject != null) {
                newSharedProject.getSequencer().addProvider(StopManager.this);
            }
        }
    };

    public StopManager(SharedProjectObservable observable) {

        this.sharedProjectObservable = observable;
        observable.add(sharedProjectObserver);
    }

    protected final IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        /**
         * @return true if the activity is expected and causes an effect, false
         *         otherwise
         */
        @Override
        public boolean receive(final StopActivity stopActivity) {

            if (sharedProject == null)
                throw new IllegalStateException(
                    "cannot receive StopActivities without a shared project");

            if (stopActivity.getType() == Type.LOCKREQUEST) {

                /*
                 * local user locks his project and adds a startHandle so he
                 * knows he is locked. Then he acknowledges
                 */
                if (stopActivity.getState() == State.INITIATED
                    && sharedProject.getUser(stopActivity.getUser()).isLocal()) {
                    addStartHandle(generateStartHandle(stopActivity));
                    // locks project and acknowledges
                    Util.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            lockProject(true);
                            fireActivity(stopActivity
                                .generateAcknowledgment(sharedProject
                                    .getLocalUser().getJID().toString()));
                        }
                    });
                    return true;
                }
                if (stopActivity.getState() == State.ACKNOWLEDGED
                    && sharedProject.getUser(stopActivity.getInitiator())
                        .isLocal()) {
                    if (!expectedAcknowledgments.contains(stopActivity)) {
                        log.warn("received unexpected StopActivity: "
                            + stopActivity);
                        return false;
                    }

                    // it has to be removed from the expected ack list
                    // because it already arrived
                    if (expectedAcknowledgments.remove(stopActivity)) {
                        reentrantLock.lock();
                        acknowledged.signalAll();
                        reentrantLock.unlock();
                        return true;
                    } else {
                        log
                            .warn("received unexpected StopActivity acknowledgement: "
                                + stopActivity);
                        return false;
                    }
                }
            }

            if (stopActivity.getType() == Type.UNLOCKREQUEST) {
                if (stopActivity.getState() == State.INITIATED
                    && sharedProject.getUser(stopActivity.getUser()).isLocal()) {

                    return executeUnlock(generateStartHandle(stopActivity));
                }
            }
            return false;
        }
    };

    /**
     * Blocking method that asks the given users to halt all user-input and
     * returns a list of handles to be used when the users can start again.
     * 
     * TODO This method is not tested for more than one user since it is not
     * used yet.
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
     * @blocking returning when the given users acknowledged the stop
     * 
     * @cancelable This method can be canceled by the user
     * 
     * @throws CancellationException
     */
    public List<StartHandle> stop(final Collection<User> users,
        final String cause, final SubMonitor monitor)
        throws CancellationException {

        final List<StartHandle> resultingHandles = Collections
            .synchronizedList(new LinkedList<StartHandle>());
        final CountDownLatch doneSignal = new CountDownLatch(users.size());

        for (final User user : users) {
            Util.runSafeAsync(log, new Runnable() {
                public void run() {
                    try {
                        StartHandle startHandle = stop(user, cause, SubMonitor
                            .convert(new NullProgressMonitor()));
                        resultingHandles.add(startHandle);
                        log.debug("Added " + startHandle
                            + " to resulting handles.");
                        doneSignal.countDown();
                    } catch (CancellationException e) {
                        log.debug("User canceled the Stopping");
                        monitor.setCanceled(true);
                    }
                }
            });
        }
        while (resultingHandles.size() != users.size() && !monitor.isCanceled()) {
            try {
                // waiting for all startHandles
                doneSignal.await(MILLISTOWAIT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log
                    .error("Stopping was interrupted. Not all users could successfully be stopped.");
            }
        }
        if (monitor.isCanceled()) {
            // TODO SZ What about users that were stopped but have not returned
            // a startHandle (because of cancellation before receiving the
            // acknowledgment)

            // restart the already stopped users
            log
                .debug("Monitor was canceled. Restarting already stopped users.");
            for (StartHandle startHandle : resultingHandles)
                startHandle.start();
            throw new CancellationException();
        }
        return resultingHandles;
    }

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
     * @blocking returning when the given user acknowledged the stop
     * 
     * @cancelable This method can be canceled by the user
     * 
     * @throws CancellationException
     */
    public StartHandle stop(User user, String cause, final SubMonitor progress)
        throws CancellationException {

        if (sharedProject == null)
            throw new IllegalStateException(
                "stop cannot be called without a shared project");

        // creating StopActivity for asking user to stop
        StopActivity stopActivity = new StopActivity(sharedProject
            .getLocalUser().getJID().toString(), sharedProject.getLocalUser()
            .getJID(), user.getJID(), Type.LOCKREQUEST, State.INITIATED);

        StartHandle handle = generateStartHandle(stopActivity);

        // short cut if affected user is local
        if (user.isLocal()) {
            addStartHandle(handle);
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    lockProject(true);
                }
            });
            return handle;
        }

        StopActivity expectedAck = stopActivity.generateAcknowledgment(user
            .getJID().toString());
        expectedAcknowledgments.add(expectedAck);

        fireActivity(stopActivity);
        progress.setBlocked(Status.OK_STATUS);

        // block until user acknowledged
        log.debug("Waiting for acknowledgment " + Util.prefix(user.getJID()));
        reentrantLock.lock();
        try {
            while (expectedAcknowledgments.contains(expectedAck)
                && !progress.isCanceled()) {
                acknowledged.await(MILLISTOWAIT, TimeUnit.MILLISECONDS);
            }
            if (expectedAcknowledgments.contains(expectedAck)) {
                log.warn("No acknowlegment arrived, gave up waiting");
                expectedAcknowledgments.remove(expectedAck);
                throw new CancellationException();
            }
            log.debug("Acknowledgment arrived " + Util.prefix(user.getJID()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            reentrantLock.unlock();
            progress.clearBlocked();
        }
        addStartHandle(handle);
        return handle;
    }

    /**
     * The goal of this method is to ensure that the local user cannot cause any
     * editing activities (FileActivities and TextEditActivities).
     * 
     * @param lock
     *            if true the project gets locked, else it gets unlocked
     */
    protected void lockProject(boolean lock) {
        for (Blockable blockable : blockables) {
            if (lock)
                blockable.block();
            else
                blockable.unblock();
        }
    }

    /**
     * Unlocks project without sending an acknowledgment if there don't exist
     * any more startHandles.
     */
    protected boolean executeUnlock(StartHandle startHandle) {

        if (!startHandle.getUser().isLocal())
            throw new IllegalArgumentException(
                "ExecuteUnlock may only be called with a StartHandle for the local user");

        if (!removeStartHandle(startHandle))
            log.debug(startHandle
                + " couldn't be removed because it doesn't exist any more.");

        if (!noStartHandlesFor(sharedProject.getLocalUser())) {
            log.debug(startHandles.get(sharedProject.getLocalUser()).size()
                + " startHandles remaining.");
            return true;
        }
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                lockProject(false);
            }
        });
        return true;
    }

    /**
     * sends an initiated unlock request
     * 
     * @param handle
     *            the startHandle whose start() initiated the unlocking
     */
    public void initiateUnlock(StartHandle handle) {
        if (sharedProject == null)
            throw new IllegalStateException(
                "cannot initiate unlock without a shared project");

        // short cut for local user
        if (handle.getUser().isLocal()) {
            executeUnlock(handle);
            return;
        }

        fireActivity(new StopActivity(sharedProject.getLocalUser().getJID()
            .toString(), sharedProject.getLocalUser().getJID(), handle
            .getUser().getJID(), Type.UNLOCKREQUEST, State.INITIATED, handle
            .getHandleID()));
    }

    /**
     * {@inheritDoc}
     */
    public void addActivityListener(IActivityListener listener) {
        activityListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void exec(IActivity activity) {
        activity.dispatch(activityReceiver);
    }

    /**
     * {@inheritDoc}
     */
    public void removeActivityListener(IActivityListener listener) {
        activityListeners.remove(listener);
    }

    public void fireActivity(IActivity stopActivity) {
        /*
         * TODO Now StopActivities are sent to everybody, it would be better if
         * it was sent only to the affected participant.
         */
        for (IActivityListener listener : activityListeners) {
            listener.activityCreated(stopActivity); // informs ActivitySequencer
        }
    }

    /**
     * Adds a StartHandle to startHandles, which maps a user to a list of
     * StartHandles. These Lists are created lazily.
     */
    public void addStartHandle(StartHandle startHandle) {
        User user = startHandle.getUser();
        List<StartHandle> handleList = startHandles.get(user);
        if (handleList == null) {
            handleList = new LinkedList<StartHandle>();
            startHandles.put(user, handleList);
        }
        handleList.add(startHandle);
    }

    /**
     * Removes a StartHandle from startHandles. If the list for user is empty
     * then the user is removed from startHandles.
     * 
     * @return false if the given startHandle didn't exist in Map, true
     *         otherwise
     */
    public boolean removeStartHandle(StartHandle startHandle) {
        User user = startHandle.getUser();
        List<StartHandle> handleList = startHandles.get(user);
        if (handleList == null)
            return false; // nothing to do
        boolean out = handleList.remove(startHandle);
        if (handleList.isEmpty())
            startHandles.remove(user);
        return out;
    }

    /**
     * @return true if there don't exist any StartHandles for the given user
     */
    public boolean noStartHandlesFor(User user) {
        return !startHandles.containsKey(user);
    }

    public StartHandle generateStartHandle(StopActivity stopActivity) {
        User user = sharedProject.getUser(stopActivity.getUser());
        return new StartHandle(user, this, stopActivity.getActivityID());
    }

    public void addBlockable(Blockable stoppable) {
        blockables.add(stoppable);
    }

    public void removeBlockable(Blockable stoppable) {
        blockables.remove(stoppable);
    }

    public void dispose() {
        sharedProjectObservable.remove(sharedProjectObserver);
    }

    protected void reset() {
        lockProject(false);
        startHandles.clear();
        expectedAcknowledgments.clear();
    }
}
