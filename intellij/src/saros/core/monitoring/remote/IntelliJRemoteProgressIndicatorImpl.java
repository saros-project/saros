package saros.core.monitoring.remote;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import saros.activities.ProgressActivity;
import saros.activities.ProgressActivity.ProgressAction;
import saros.core.monitoring.IStatus;
import saros.core.monitoring.Status;
import saros.intellij.runtime.UIMonitoredJob;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.remote.IRemoteProgressIndicator;
import saros.monitoring.remote.RemoteProgressManager;
import saros.session.User;
import saros.ui.util.ModelFormatUtils;

/** IntelliJ implementation of the {@link IRemoteProgressIndicator} interface. */
final class IntelliJRemoteProgressIndicatorImpl implements IRemoteProgressIndicator {

  private static final Logger LOG = Logger.getLogger(IntelliJRemoteProgressIndicatorImpl.class);

  private final RemoteProgressManager rpm;
  private final String remoteProgressID;
  private final User remoteUser;

  private boolean running;
  private boolean started;

  /**
   * A queue of incoming ProgressActivities which will be processed locally to update the local
   * Progress dialog.
   */
  private LinkedBlockingQueue<ProgressActivity> activities =
      new LinkedBlockingQueue<ProgressActivity>();

  /**
   * Creates an IntelliJRemoteProgressIndicatorImpl.
   *
   * @param rpm {@link RemoteProgressManager} which creates the indicator
   * @param remoteProgressID ID of the tracked remote progress
   * @param remoteUser user generating the tracked remote progress
   */
  IntelliJRemoteProgressIndicatorImpl(
      final RemoteProgressManager rpm, final String remoteProgressID, final User remoteUser) {
    this.rpm = rpm;
    this.remoteProgressID = remoteProgressID;
    this.remoteUser = remoteUser;
  }

  @Override
  public String getRemoteProgressID() {
    return remoteProgressID;
  }

  @Override
  public User getRemoteUser() {
    return remoteUser;
  }

  @Override
  public synchronized void start() {
    if (started) return;

    started = true;

    final UIMonitoredJob job =
        new UIMonitoredJob(
            ModelFormatUtils.format("Observing remote progress for {0}", remoteUser)) {
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            try {
              mainloop(monitor);
              return Status.OK_STATUS;
            } catch (Exception e) {
              LOG.error(e);
              return Status.CANCEL_STATUS;
            } finally {
              rpm.progressIndicatorStopped(IntelliJRemoteProgressIndicatorImpl.this);
            }
          }
        };
    job.schedule();
    running = true;
  }

  @Override
  public synchronized void stop() {
    if (!running) return;

    running = true;

    /**
     * This Activity is just used as a PoisonPill for the ActivityLoop of the ProgressMonitor
     * (identified by the ProgressAction.DONE) and therefore most values don't have to be set
     * correctly as this Activity will never be sent over the Network.
     */
    handleProgress(
        new ProgressActivity(
            remoteUser, remoteUser, remoteProgressID, 0, 0, null, ProgressAction.DONE));
  }

  @Override
  public synchronized void handleProgress(ProgressActivity activity) {
    if (!remoteUser.equals(activity.getSource())) {
      LOG.warn(
          "RemoteProgress with ID: "
              + remoteProgressID
              + " is owned by user "
              + remoteUser
              + " rejecting activity from other user: "
              + activity);
      return;
    }

    if (!running) {
      LOG.debug(
          "RemoteProgress with ID: "
              + remoteProgressID
              + " has already been closed. Discarding activity: "
              + activity);
      return;
    }

    activities.add(activity);
  }

  private void mainloop(final IProgressMonitor monitor) {
    int worked = 0;
    boolean firstTime = true;

    update:
    while (true) {

      final ProgressActivity activity;

      try {
        if (monitor.isCanceled()) break update;

        // poll so this monitor can be closed locally
        activity = activities.poll(1000, TimeUnit.MILLISECONDS);

        if (activity == null) continue update;

      } catch (InterruptedException e) {
        return;
      }

      final String taskName = activity.getTaskName();

      int newWorked;

      if (LOG.isTraceEnabled()) LOG.trace("executing progress activity: " + activity);

      switch (activity.getAction()) {
        case BEGINTASK:
          monitor.beginTask(taskName, activity.getWorkTotal());
          continue update;
        case SETTASKNAME:
          monitor.setTaskName(taskName);
          continue update;
        case SUBTASK:
          if (taskName != null) monitor.subTask(taskName);
          newWorked = activity.getWorkCurrent();
          if (newWorked > worked) {
            monitor.worked(newWorked - worked);
            worked = newWorked;
          }
          continue update;
        case UPDATE:
          if (firstTime) {
            monitor.beginTask(taskName, activity.getWorkTotal());
            firstTime = false;
          } else {
            if (taskName != null) monitor.subTask(taskName);

            newWorked = activity.getWorkCurrent();
            if (newWorked > worked) {
              monitor.worked(newWorked - worked);
              worked = newWorked;
            }
          }
          continue update;
        case DONE:
          monitor.done();
          break update;
        case CANCEL:
          LOG.debug("progress was canceled by remote user");
          monitor.setCanceled(true);
          break update;
      }
    }
  }

  @Override
  public int hashCode() {
    return ((remoteProgressID == null) ? 0 : remoteProgressID.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (!(obj instanceof IntelliJRemoteProgressIndicatorImpl)) return false;

    return ObjectUtils.equals(
            remoteProgressID, ((IntelliJRemoteProgressIndicatorImpl) obj).remoteProgressID)
        && ObjectUtils.equals(remoteUser, ((IntelliJRemoteProgressIndicatorImpl) obj).remoteUser);
  }
}
