package de.fu_berlin.inf.dpp.synchronize;

import de.fu_berlin.inf.dpp.activities.StopActivity;
import de.fu_berlin.inf.dpp.activities.StopActivity.State;
import de.fu_berlin.inf.dpp.activities.StopActivity.Type;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.ObservableValue;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
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
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

/**
 * The @StopManager class is used to coordinate blocking of user input between different Saros
 * Users. It both produces and consumes activities. Blocking the user input is not implemented by
 * this class but by classes that implement the Blockable interface and have registered themselves
 * with the StopManager by calling {@link #addBlockable}.
 *
 * <p>There are two variants of the {@link #stop} method. One is working on a single Saros User and
 * the other is working on a collection of them. The guarantee the StopManager makes is that at the
 * end of the execution of the {@link #stop} method all Saros Users are stopped or all of them are
 * started.
 *
 * <p>A StartHandle will be returned for each stopped user, it can be used to remove the block of
 * remote users.
 */
@Component(module = "core")
public final class StopManager extends AbstractActivityProducer implements Startable {

  private static final Logger log = Logger.getLogger(StopManager.class);

  private static final Random RANDOM = new Random();

  // Waits MILLISTOWAIT ms until the next test for progress cancellation
  public final int MILLISTOWAIT = 100;

  /** Timeout to abort waiting for a response */
  static final long TIMEOUT = 20000;

  protected List<Blockable> blockables = new CopyOnWriteArrayList<Blockable>();

  protected ObservableValue<Boolean> blocked = new ObservableValue<Boolean>(false);

  protected final ISarosSession sarosSession;

  /**
   * Maps a User to a List of his StartHandles. Never touch this directly, use the add and remove
   * methods.
   */
  private Map<User, List<StartHandle>> startHandles =
      Collections.synchronizedMap(new HashMap<User, List<StartHandle>>());

  /**
   * For every initiated unlock (identified by its StopActivity id) there is one acknowledgment
   * expected.
   */
  private Map<String, StartHandle> startsToBeAcknowledged =
      Collections.synchronizedMap(new HashMap<String, StartHandle>());

  /** For every initiated StopActivity (type: LockRequest) there is one acknowledgment expected. */
  private Set<StopActivity> expectedAcknowledgments =
      Collections.synchronizedSet(new HashSet<StopActivity>());

  private final Object notificationLock = new Object();
  /** Indicates of the component is stopped; */
  private boolean isStopped = false;

  public StopManager(ISarosSession session) {
    this.sarosSession = session;
  }

  /**
   * @JTourBusStop 14, Activity sending, Receiving an Activity:
   *
   * <p>This anonymous subclass of AbstractActivityConsumer overrides the receive() method with the
   * right overload. This way, the triple dispatch ensures that those who are interested in certain
   * activities receive exactly these types.
   */
  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(final StopActivity stopActivity) {
          handleStopActivity(stopActivity);
        }
      };

  /**
   * @JTourBusStop 2, StopManager:
   *
   * <p>This is where lock/unlock requests and acknowledgments will be handled. When there are
   * outgoing lock requests the expected answers will be put into the expectedAcknowledgements set
   * and when the acknowledgment arrives it will be removed from the set. For incoming lock requests
   * lockProject(true) will be called.
   */
  private void handleStopActivity(final StopActivity stopActivity) {
    assert sarosSession != null;

    User user = stopActivity.getRecipient();

    if (user.isRemote())
      throw new IllegalArgumentException("Received StopActivity which is not for the local user");

    if (stopActivity.getType() == Type.LOCKREQUEST) {

      /*
       * local user locks his session and adds a startHandle so he knows
       * he is locked. Then he acknowledges.
       */
      if (stopActivity.getState() == State.INITIATED) {
        addStartHandle(generateStartHandle(stopActivity));
        // locks session and acknowledges

        lockSession(true);
        fireActivity(stopActivity.generateAcknowledgment(sarosSession.getLocalUser()));

        return;
      }
      if (stopActivity.getState() == State.ACKNOWLEDGED) {
        synchronized (notificationLock) {
          if (!expectedAcknowledgments.contains(stopActivity)) {
            log.warn("Received unexpected StopActivity: " + stopActivity);
            return;
          }

          /**
           * Remove from the expectedAcknowledgements set and inform who ever has been waiting for
           * that to happen. Warn if the removal is failing besides the above check.
           */
          if (!expectedAcknowledgments.remove(stopActivity)) {
            log.warn("Received unexpected " + "StopActivity acknowledgement: " + stopActivity);
            return;
          }

          notificationLock.notifyAll();
        }
        return;
      }
    }

    if (stopActivity.getType() == Type.UNLOCKREQUEST) {
      if (stopActivity.getState() == State.INITIATED) {

        executeUnlock(generateStartHandle(stopActivity));
        // sends an acknowledgment
        fireActivity(stopActivity.generateAcknowledgment(sarosSession.getLocalUser()));
        return;
      }

      if (stopActivity.getState() == State.ACKNOWLEDGED) {
        StartHandle handle = startsToBeAcknowledged.remove(stopActivity.getActivityID());
        if (handle == null) {
          log.error("StartHandle for " + stopActivity + " could not be found.");
          return;
        }
        handle.acknowledge();
        return;
      }
    }

    throw new IllegalArgumentException("StopActivity is of unknown type: " + stopActivity);
  }

  /**
   * Blocking method that asks the given users to halt all user-input and returns a list of handles
   * to be used when the users can start again.
   *
   * @param users the participants who has to stop
   * @param cause the cause for stopping as it is displayed in the progress monitor
   * @noGUI this method must not be called from the GUI thread.
   * @blocking returning after the given users acknowledged the stop
   * @throws CancellationException if the timeout is exceeded
   */
  public List<StartHandle> stop(final Collection<User> users, final String cause)
      throws CancellationException {

    final List<StartHandle> resultingHandles =
        Collections.synchronizedList(new LinkedList<StartHandle>());
    final LinkedList<Thread> threads = new LinkedList<Thread>();

    final AtomicBoolean isTimeout = new AtomicBoolean(false);

    for (final User user : users) {
      threads.add(
          ThreadUtils.runSafeAsync(
              "dpp-stop-" + user + "-" + cause,
              log,
              new Runnable() {
                @Override
                public void run() {
                  try {
                    StartHandle startHandle = stop(user, cause);
                    resultingHandles.add(startHandle);
                    log.debug("added " + startHandle + " to resulting handles.");
                  } catch (CancellationException e) {
                    isTimeout.set(true);
                    log.error("user " + user + " did not respond");
                  } catch (InterruptedException e) {
                    isTimeout.set(true);
                    log.error("waiting for response of user " + user + " was interrupted");
                  }
                }
              }));
    }

    /**
     * We have started all threads and we will wait for all of them to finish now. This is the
     * safest and most simple approach to avoid ending up with ConcurrentModificationExceptions that
     * might happen when we resume the remote users while some threads still need to be executed.
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

    if (isTimeout.get()) {
      // Restart the already stopped users
      log.error("some users do not respond, restarting already stopped users");
      for (StartHandle startHandle : resultingHandles) startHandle.start();
      throw new CancellationException();
    }
    return resultingHandles;
  }

  /**
   * @JTourBusStop 1, StopManager:
   *
   * <p>Sometimes it is necessary to prevent others from making modifications, e.g. during the
   * OutgoingProjectNegotiation or during the recovery by ConsistencyWatchdogHandler and this class
   * is responsible for managing this process. Objects that want to implement a lock need to
   * register a Blockable with the StopManager, the Blockable will be called when the StopManager is
   * locked or unlocked. The stop method will either stop a single user or a list of users and then
   * will return a single handle or a list of handles.
   */

  /**
   * Blocking method that asks the given user to halt all user-input and returns a handle to be used
   * when the user can start again.
   *
   * @param user the participant who has to stop
   * @param cause the cause for stopping as it is displayed in the progress monitor
   * @noGUI this method must not be called from the GUI thread.
   * @blocking returning after the given user acknowledged the stop
   * @throws CancellationException if the timeout is exceeded
   * @throws InterruptedException
   */
  public StartHandle stop(User user, String cause)
      throws CancellationException, InterruptedException {
    assert sarosSession != null;

    // Creating StopActivity for asking user to stop
    User localUser = sarosSession.getLocalUser();
    final StopActivity stopActivity =
        new StopActivity(
            localUser,
            localUser,
            user,
            Type.LOCKREQUEST,
            State.INITIATED,
            new SimpleDateFormat("HHmmssSS").format(new Date()) + RANDOM.nextLong());

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

    long timeoutToExceed = System.currentTimeMillis() + StopManager.TIMEOUT;

    boolean isInterrupted = false;
    boolean acknowledged = false;
    synchronized (notificationLock) {
      while ((System.currentTimeMillis() < timeoutToExceed) && user.isInSession()) {

        acknowledged = !expectedAcknowledgments.contains(expectedAck);

        if (acknowledged && isStopped) {
          acknowledged = false;
          break;
        }

        if (acknowledged) break;

        try {
          notificationLock.wait(1000);
        } catch (InterruptedException e) {
          isInterrupted = true;
          break;
        }
      }
    }

    // clean up
    expectedAcknowledgments.remove(expectedAck);

    /*
     * the user did respond or we got interrupted ... do not care to check
     * if the user is still in session ... just try to resume ... it does
     * not matter if it fails
     */

    if (isInterrupted) {
      handle.start();
      throw new InterruptedException();
    }

    if (!acknowledged) {
      log.warn("No acknowledgment arrived, gave up waiting");

      handle.start();
      throw new CancellationException();
    }

    log.debug("Acknowledgment arrived " + user);

    return handle;
  }

  /**
   * The goal of this method is to ensure that the local user cannot cause any editing activities
   * (FileActivities and TextEditActivities).
   *
   * @param lock if true the session gets locked, else it gets unlocked
   */
  private void lockSession(boolean lock) {
    for (Blockable blockable : blockables) {
      if (lock) blockable.block();
      else blockable.unblock();
    }
    blocked.setValue(lock);
  }

  /**
   * Unlocks session without sending an acknowledgment if there don't exist any more startHandles.
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
      log.debug(startHandle + " couldn't be removed because it doesn't exist any more.");
    }

    int remainingHandles = getStartHandles(sarosSession.getLocalUser()).size();
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
   * @param handle the startHandle whose start() initiated the unlocking
   */
  private void initiateUnlock(StartHandle handle) {
    assert sarosSession != null;

    // short cut for local user
    if (handle.getUser().isLocal()) {
      executeUnlock(handle);
      return;
    }

    startsToBeAcknowledged.put(handle.getHandleID(), handle);

    final StopActivity activity =
        new StopActivity(
            sarosSession.getLocalUser(),
            sarosSession.getLocalUser(),
            handle.getUser(),
            Type.UNLOCKREQUEST,
            State.INITIATED,
            handle.getHandleID());

    /**
     * @JTourBusStop 4, Activity sending, Firing the activity:
     *
     * <p>The following call fires the activity and all listeners (including the session) will be
     * notified.
     */
    fireActivity(activity);
  }

  /**
   * Adds a StartHandle to startHandles, which maps a user to a list of StartHandles. These Lists
   * are created lazily.
   */
  private void addStartHandle(StartHandle startHandle) {
    getStartHandles(startHandle.getUser()).add(startHandle);
  }

  /**
   * Removes a StartHandle from startHandles. If the list for user is empty then the user is removed
   * from startHandles.
   *
   * @return false if the given startHandle didn't exist in Map, true otherwise
   */
  private boolean removeStartHandle(StartHandle startHandle) {
    return getStartHandles(startHandle.getUser()).remove(startHandle);
  }

  private List<StartHandle> getStartHandles(User user) {

    List<StartHandle> result;

    synchronized (startHandles) {
      result = startHandles.get(user);

      if (result == null) {
        result = new CopyOnWriteArrayList<StartHandle>();
        startHandles.put(user, result);
      }
    }

    return result;
  }

  private StartHandle generateStartHandle(StopActivity stopActivity) {
    User user = stopActivity.getAffected();
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

  private void clearExpectedAcknowledgments() {
    /**
     * Clear the expectedAcknowledgements and inform the threads that are blocked in the stop method
     * that there will be no response.
     */
    synchronized (this) {
      expectedAcknowledgments.clear();
      isStopped = true;
      notifyAll();
    }
  }

  private Object resumeLock = new Object();

  /**
   * Resume the StartHandle and remove it from internal lists.
   *
   * @param startHandle
   * @return Returns true if the last handle for the user has been removed.
   */
  /*
   * TODO decrease lock time
   */

  boolean resumeStartHandle(StartHandle startHandle) {
    synchronized (resumeLock) {
      removeStartHandle(startHandle);
      initiateUnlock(startHandle);
      return getStartHandles(startHandle.getUser()).isEmpty();
    }
  }

  @Override
  public void start() {
    sarosSession.addActivityConsumer(consumer, Priority.ACTIVE);

    /**
     * @JTourBusStop 3, Activity sending, An example of an IActivityProducer:
     *
     * <p>The most frequently used IActivityListener is the Saros Session. The canonical way to get
     * the session listening to one's activities is to add oneself to the session.
     */
    sarosSession.addActivityProducer(this);
  }

  @Override
  public void stop() {
    sarosSession.removeActivityConsumer(consumer);
    sarosSession.removeActivityProducer(this);
    lockSession(false);
    clearExpectedAcknowledgments();
  }
}
