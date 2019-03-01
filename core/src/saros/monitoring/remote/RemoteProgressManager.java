package saros.monitoring.remote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import saros.activities.ProgressActivity;
import saros.annotations.Component;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;

/**
 * The RemoteProgressManager is responsible for creating and managing {@link RemoteProgressMonitor
 * remote progress monitors} which report task progress to remote sites, as well as for listening
 * for remote progress and reporting it through {@link IRemoteProgressIndicator} instances.
 */
@Component(module = "core")
// FIXME this component has NO flow control, it can flood the network layer
public class RemoteProgressManager extends AbstractActivityProducer {

  private static final Random RANDOM = new Random();

  private final ISarosSessionManager sessionManager;
  private final IRemoteProgressIndicatorFactory progressIndicatorFactory;
  private volatile ISarosSession session;

  // the id should be unique enough
  // closing a progress will remove itself from the map
  private final Map<String, IRemoteProgressIndicator> progressIndicators =
      Collections.synchronizedMap(new HashMap<String, IRemoteProgressIndicator>());

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        /**
         * Forwards incoming remote progress activities to the matching {@link
         * IRemoteProgressIndicator} instances (and creates those instances if they don't exist
         * yet).
         */
        @Override
        public void receive(ProgressActivity progressActivity) {

          IRemoteProgressIndicator indicator;
          final String id = progressActivity.getProgressID();

          synchronized (progressIndicators) {
            indicator = progressIndicators.get(id);

            if (indicator == null) {
              indicator =
                  progressIndicatorFactory.create(
                      RemoteProgressManager.this, id, progressActivity.getSource());

              progressIndicators.put(id, indicator);

              indicator.start();
            }
          }

          indicator.handleProgress(progressActivity);
        }
      };

  private ISessionListener sessionListener =
      new ISessionListener() {
        /**
         * Stops all remote progress indicators owned by a particular user when that user leaves the
         * session.
         */
        @Override
        public void userLeft(User user) {

          final List<IRemoteProgressIndicator> indicatorsToStop =
              new ArrayList<IRemoteProgressIndicator>();

          synchronized (progressIndicators) {
            for (final IRemoteProgressIndicator progress : progressIndicators.values()) {
              if (progress.getRemoteUser().equals(user)) indicatorsToStop.add(progress);
            }
          }

          for (final IRemoteProgressIndicator indicator : indicatorsToStop) indicator.stop();
        }
      };

  private ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        /** Registers the required listeners after a new session has started. */
        @Override
        public void sessionStarted(ISarosSession session) {
          RemoteProgressManager.this.session = session;
          session.addActivityConsumer(consumer, Priority.ACTIVE);
          session.addActivityProducer(RemoteProgressManager.this);
          session.addListener(sessionListener);
        }

        /**
         * Cleans up and stops all remote progress indicators after the current session has ended.
         */
        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          session.removeActivityConsumer(consumer);
          session.removeActivityProducer(RemoteProgressManager.this);
          session.removeListener(sessionListener);

          final List<IRemoteProgressIndicator> indicatorsToStop =
              new ArrayList<IRemoteProgressIndicator>();

          synchronized (progressIndicators) {
            indicatorsToStop.addAll(progressIndicators.values());
          }

          for (final IRemoteProgressIndicator indicator : indicatorsToStop) indicator.stop();

          RemoteProgressManager.this.session = null;
        }
      };

  /**
   * Creates a RemoteProgressManager.
   *
   * @param sessionManager session manager whose session to listen to for remote progress activities
   * @param progressIndicatorFactory factory for creating {@link IRemoteProgressIndicator} instances
   *     in response to remote progress activities
   */
  public RemoteProgressManager(
      ISarosSessionManager sessionManager,
      IRemoteProgressIndicatorFactory progressIndicatorFactory) {

    this.sessionManager = sessionManager;
    this.sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    this.progressIndicatorFactory = progressIndicatorFactory;
  }

  /**
   * Returns a new {@link IProgressMonitor} whose progress is displayed at the specified remote
   * sites, in addition to being forwarded to the given other progress monitor. If there is no
   * session currently running, the passed-in monitor is returned unchanged instead.
   *
   * @param users users to send progress to
   * @param monitor progress monitor to forward progress to for additional local progress reporting;
   *     pass a {@link NullProgressMonitor} if you want the progress to be shown remotely only
   * @return remote progress monitor, or the given progress monitor if there is no running session
   * @see RemoteProgressMonitor
   * @see IRemoteProgressIndicator
   */
  public IProgressMonitor createRemoteProgressMonitor(List<User> users, IProgressMonitor monitor) {

    ISarosSession currentSession = session;

    if (currentSession == null) return monitor;

    return new RemoteProgressMonitor(
        this,
        getNextID(),
        currentSession.getLocalUser(),
        new ArrayList<User>(new HashSet<User>(users)),
        monitor);
  }

  /**
   * Called by a {@link RemoteProgressMonitor} when it has new progress. Causes the remote progress
   * manager to send out the given remote progress activity.
   *
   * @param activity {@link ProgressActivity} describing the progress made
   */
  void monitorUpdated(ProgressActivity activity) {
    fireActivity(activity);
  }

  /**
   * Called by a {@link IRemoteProgressIndicator} if it has stopped, either because {@link
   * IRemoteProgressIndicator#stop} has been called directly or {@link
   * IRemoteProgressIndicator#handleProgress} has received an activity that indicates the end of the
   * remote operation. Causes the remote progress manager to discard the indicator.
   *
   * <p>This method is for use by {@link IRemoteProgressIndicator} implementations only. Don't use
   * it from anywhere else.
   *
   * @param indicator the stopped remote progress indicator
   */
  public void progressIndicatorStopped(IRemoteProgressIndicator indicator) {
    progressIndicators.remove(indicator.getRemoteProgressID());
  }

  private String getNextID() {
    return Long.toHexString(RANDOM.nextLong());
  }
}
