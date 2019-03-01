package saros.session.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;
import saros.activities.ActivityOptimizer;
import saros.activities.ChecksumActivity;
import saros.activities.IActivity;
import saros.activities.IResourceActivity;
import saros.activities.ITargetedActivity;
import saros.activities.JupiterActivity;
import saros.activities.QueueItem;
import saros.concurrent.management.ConcurrentDocumentClient;
import saros.concurrent.management.ConcurrentDocumentServer;
import saros.concurrent.management.TransformationResult;
import saros.session.IActivityHandlerCallback;
import saros.session.ISarosSession;
import saros.session.User;
import saros.synchronize.UISynchronizer;
import saros.util.ThreadUtils;

/**
 * This handler is responsible for handling the correct thread access when transforming activities
 * with the {@link ConcurrentDocumentServer} and {@link ConcurrentDocumentClient}. The sending and
 * executing of activities <b>must</b> be done in {@linkplain IActivityHandlerCallback callback} as
 * it is <b>not</b> performed by this handler !
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
    int dispatchModeToUse =
        Integer.getInteger("saros.session.ACTIVITY_DISPATCH_MODE", DISPATCH_MODE_SYNC);

    if (dispatchModeToUse != DISPATCH_MODE_ASYNC) dispatchModeToUse = DISPATCH_MODE_SYNC;

    DISPATCH_MODE = dispatchModeToUse;
  }

  private final LinkedBlockingQueue<List<IActivity>> dispatchQueue =
      new LinkedBlockingQueue<List<IActivity>>();

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

  private final Runnable dispatchThreadRunnable =
      new Runnable() {

        final List<List<IActivity>> pendingActivities = new ArrayList<List<IActivity>>();
        final List<IActivity> activitiesToExecute = new ArrayList<IActivity>();

        @Override
        public void run() {
          LOG.debug("activity dispatcher started");

          while (!Thread.currentThread().isInterrupted()) {
            pendingActivities.clear();
            activitiesToExecute.clear();

            try {
              pendingActivities.add(dispatchQueue.take());
            } catch (InterruptedException e) {
              break;
            }

            dispatchQueue.drainTo(pendingActivities);

            for (final List<IActivity> activities : pendingActivities)
              activitiesToExecute.addAll(activities);

            dispatchAndExecuteActivities(activitiesToExecute);
          }

          LOG.debug("activity dispatcher stopped");
        }
      };

  public ActivityHandler(
      ISarosSession session,
      IActivityHandlerCallback callback,
      ConcurrentDocumentServer documentServer,
      ConcurrentDocumentClient documentClient,
      UISynchronizer synchronizer) {
    this.session = session;
    this.callback = callback;
    this.documentServer = documentServer;
    this.documentClient = documentClient;
    this.synchronizer = synchronizer;
  }

  /**
   * Transforms and dispatches the activities. The {@linkplain IActivityHandlerCallback callback}
   * will be notified about the results.
   *
   * @param activities an <b>immutable</b> list containing the activities
   */
  public synchronized void handleIncomingActivities(List<IActivity> activities) {

    if (session.isHost()) {
      /**
       * @JTourBusStop 8, Activity sending, Activity Server:
       *
       * <p>This is where the server (or server-part of the host) receives activities. The Server
       * may transform activities again if necessary and afterward sends them to the correct
       * clients. (Note that the callback.send() methods get an actual list of recipients.)
       */
      TransformationResult result = directServerActivities(activities);
      for (QueueItem item : result.getSendToPeers()) {
        List<User> recipients = getRecipientsForQueueItem(item);
        callback.send(recipients, item.activity);
      }

      activities = result.getLocalActivities();
    }

    /**
     * @JTourBusStop 9, Activity sending, Client Receiver:
     *
     * <p>This is the part where clients will receive activities. These activities are put into the
     * queue of the activity dispatcher. This queue is consumed by the dispatchThread, which
     * transforms activities again if necessary, and then forwards it to the SarosSession.
     */
    if (activities.isEmpty()) return;

    if (DISPATCH_MODE == DISPATCH_MODE_ASYNC) dispatchAndExecuteActivities(activities);
    else dispatchQueue.add(activities);
  }

  /**
   * Determines the recipients for a given QueueItem
   *
   * @param item the QueueItem for which the participants should be determined
   * @return a list of participants this activity should be sent to
   */
  private List<User> getRecipientsForQueueItem(QueueItem item) {

    /*
     * If the Activity is a IResourceActivity check that the user can
     * actually process them
     */
    List<User> recipients = new ArrayList<User>();
    if (item.activity instanceof IResourceActivity) {
      IResourceActivity activity = (IResourceActivity) item.activity;
      /*
       * HACK: IResourceActivities with null as path will be treated as
       * not being resource related as we can't decide whether to send
       * them or not. IResourceActivities must not have null as path but
       * as the EditorActivity currently break this and uses null paths
       * for non-shared-files we have to make this distinction for now.
       */
      if (activity.getPath() == null) {
        recipients = item.recipients;
      } else {
        for (User user : item.recipients) {
          if (session.userHasProject(user, activity.getPath().getProject())) {
            recipients.add(user);
          }
        }
      }
    } else {
      recipients = item.recipients;
    }
    return recipients;
  }

  /**
   * Transforms and determines the recipients of the activities. The {@linkplain
   * IActivityHandlerCallback callback} will be notified about the results.
   *
   * @param activities an <b>immutable</b> list containing the activities
   */
  /*
   * Note: transformation and executing has to be performed together in the
   * SWT thread. Else, it would be possible that local activities are executed
   * between transformation and application of remote operations. In other
   * words, the transformation would be applied to an out-dated state.
   */
  public void handleOutgoingActivities(final List<IActivity> activities) {
    /**
     * @JTourBusStop 6, Activity sending, Transforming the IActivity (Client):
     *
     * <p>First, this method will transform activities. Transformation has not effect on most
     * activities, but it turns TextEditActivity into JupiterActivities. Then, they are forward the
     * to the SarosSession. (The callback is a level of indirection that improves testability.)
     *
     * <p>Saros uses a client-server-architecture. All activities will first be send to the server
     * located at the Host, this is why the only recipient of the result is the session's host.
     * Please note: The Host itself has both client and server part, so even his activities will be
     * "sent" to himself first.
     */
    synchronizer.syncExec(
        ThreadUtils.wrapSafe(
            LOG,
            new Runnable() {

              @Override
              public void run() {
                for (IActivity activity : activities) {

                  IActivity transformationResult = documentClient.transformToJupiter(activity);

                  callback.send(Collections.singletonList(session.getHost()), transformationResult);
                }
              }
            }));
  }

  @Override
  public void start() {
    if (DISPATCH_MODE == DISPATCH_MODE_ASYNC) return;

    dispatchThread =
        ThreadUtils.runSafeAsync("dpp-activity-dispatcher", LOG, dispatchThreadRunnable);
  }

  @Override
  public void stop() {
    if (DISPATCH_MODE == DISPATCH_MODE_ASYNC) return;

    dispatchThread.interrupt();
    try {
      dispatchThread.join(TIMEOUT);
    } catch (InterruptedException e) {
      LOG.warn(
          "interrupted while waiting for " + dispatchThread.getName() + " thread to terminate");

      Thread.currentThread().interrupt();
    }

    if (dispatchThread.isAlive()) LOG.error(dispatchThread.getName() + " thread is still running");
  }

  /**
   * Executes the current activities by dispatching the received activities to the SWT EDT.
   *
   * <p>We must use synchronous dispatching as it is possible that some handlers or Eclipse itself
   * open dialogs during the execution of an activity.
   *
   * <p>If the current activity list would be dispatched asynchronously it is possible that further
   * activities may be executed during the currently executed activity list and so leading up to
   * unknown errors.
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
   * @param activities the activities to execute
   */
  /*
   * Note: transformation and executing has to be performed together in the
   * SWT thread. Else, it would be possible that local activities are executed
   * between transformation and application of remote operations. In other
   * words, the transformation would be applied to an out-dated state.
   */
  private void dispatchAndExecuteActivities(final List<IActivity> activities) {

    final List<IActivity> optimizedActivities = ActivityOptimizer.optimize(activities);

    final Runnable transformingRunnable =
        new Runnable() {
          @Override
          public void run() {

            for (IActivity activity : optimizedActivities) {

              User source = activity.getSource();

              /*
               * Ensure that we do not execute activities after all
               * listeners were notified (See SarosSession#removeUser). It
               * is still possible that a user may left during activity
               * execution but this is likely no to produce any errors.
               *
               * TODO: as the notification for users who left the session
               * is send in parallel with the activities there will be
               * race conditions were one user may execute a given
               * activity but another user will not which may lead to
               * unwanted inconsistencies if that activity was a resource
               * activity.
               */
              if (!source.isInSession()) {
                LOG.warn("dropping activity for user that is no longer in session: " + activity);
                continue;
              }

              List<IActivity> transformedActivities = documentClient.transformFromJupiter(activity);

              for (IActivity transformedActivity : transformedActivities) {
                try {
                  callback.execute(transformedActivity);
                } catch (Exception e) {
                  LOG.error("failed to execute activity: " + activity, e);
                }
              }
            }
          }
        };

    if (LOG.isTraceEnabled()) {

      if (optimizedActivities.size() != activities.size()) {
        LOG.trace("original activities to dispatch: [#" + activities.size() + "] " + activities);
      }

      LOG.trace(
          "dispatching [#"
              + optimizedActivities.size()
              + "] optimized activities [mode = "
              + DISPATCH_MODE
              + "] : "
              + optimizedActivities);
    }

    if (DISPATCH_MODE == DISPATCH_MODE_SYNC)
      synchronizer.syncExec(ThreadUtils.wrapSafe(LOG, transformingRunnable));
    else synchronizer.asyncExec(ThreadUtils.wrapSafe(LOG, transformingRunnable));
  }

  /**
   * This method is responsible for directing activities received at the server to the various
   * clients.
   *
   * @param activities A list of incoming activities
   * @return A number of targeted activities.
   */
  private TransformationResult directServerActivities(List<IActivity> activities) {

    TransformationResult result = new TransformationResult(session.getLocalUser());

    final List<User> remoteUsers = session.getRemoteUsers();
    final List<User> allUsers = session.getUsers();

    for (IActivity activity : activities) {
      documentServer.checkFileDeleted(activity);

      if (activity instanceof JupiterActivity || activity instanceof ChecksumActivity) {

        result.addAll(documentServer.transformIncoming(activity));
      } else if (activity instanceof ITargetedActivity) {
        ITargetedActivity target = (ITargetedActivity) activity;
        result.add(new QueueItem(target.getTarget(), activity));

      } else if (remoteUsers.size() > 0) {

        // We must not send the activity back to the sender
        List<User> receivers = new ArrayList<User>();
        for (User user : allUsers) {
          if (!user.equals(activity.getSource())) {
            receivers.add(user);
          }
        }
        result.add(new QueueItem(receivers, activity));

        /*
         * should we really execute an activity from a user that is
         * about to or has left the session ?
         */
      } else if (!(session.getLocalUser().equals(activity.getSource()))) {
        result.executeLocally.add(activity);
      }
    }
    return result;
  }
}
