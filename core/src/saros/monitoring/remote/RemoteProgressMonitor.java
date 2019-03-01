package saros.monitoring.remote;

import java.util.Collection;
import org.apache.log4j.Logger;
import saros.activities.ProgressActivity;
import saros.activities.ProgressActivity.ProgressAction;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.session.User;
import saros.util.StackTrace;

/**
 * An {@link IProgressMonitor} implementation which sends all progress as activities to a specific
 * set of users. Instances are created by calling {@link
 * RemoteProgressManager#createRemoteProgressMonitor}.
 */
class RemoteProgressMonitor implements IProgressMonitor {

  private static final Logger LOG = Logger.getLogger(RemoteProgressMonitor.class);

  private final RemoteProgressManager rpm;
  private final String id;
  private final User localUser;
  private final Collection<User> remoteUsers;
  private final IProgressMonitor monitor;

  private int worked = 0;
  private int totalWorked = -1;

  /**
   * Creates a RemoteProgressMonitor which wraps an existing {@link IProgressMonitor}. All progress
   * is both forwarded to that monitor and sent out as progress activities.
   *
   * @param rpm {@link RemoteProgressManager} which handles the monitor
   * @param id unique ID added to sent progress activities so that their receivers can match the
   *     activities with their associated remote monitors
   * @param localUser the current session's local user
   * @param remoteUsers users to send progress activities to
   * @param monitor {@link IProgressMonitor} to wrap
   */
  RemoteProgressMonitor(
      final RemoteProgressManager rpm,
      final String id,
      final User localUser,
      final Collection<User> remoteUsers,
      IProgressMonitor monitor) {
    this.rpm = rpm;
    this.id = id;
    this.localUser = localUser;
    this.remoteUsers = remoteUsers;
    this.monitor = monitor == null ? new NullProgressMonitor() : monitor;
  }

  @Override
  public void beginTask(String name, int totalWorked) {
    monitor.beginTask(name, totalWorked);
    this.totalWorked = totalWorked;
    createProgressActivityForUsers(0, totalWorked, name, ProgressAction.BEGINTASK);
  }

  @Override
  public void done() {
    monitor.done();
    createProgressActivityForUsers(0, 0, null, ProgressAction.DONE);
  }

  @Override
  public boolean isCanceled() {
    return monitor.isCanceled();
  }

  @Override
  public void setCanceled(boolean value) {
    monitor.setCanceled(value);
    createProgressActivityForUsers(worked, totalWorked, "Cancellation", ProgressAction.CANCEL);
  }

  @Override
  public void setTaskName(String name) {
    monitor.setTaskName(name);
    createProgressActivityForUsers(worked, totalWorked, name, ProgressAction.SETTASKNAME);
  }

  @Override
  public void subTask(String name) {
    monitor.subTask(name);
    createProgressActivityForUsers(worked, totalWorked, name, ProgressAction.SUBTASK);
  }

  @Override
  public void worked(int work) {
    monitor.worked(work);
    worked += work;

    if (worked > totalWorked) {
      LOG.warn(
          worked
              + " > "
              + totalWorked
              + " (worked > totalworked) | maybe forget to call beginTask()",
          new StackTrace());
    }

    createProgressActivityForUsers(worked, totalWorked, null, ProgressAction.UPDATE);
  }

  private void createProgressActivityForUsers(
      int workCurrent, int workTotal, String taskName, ProgressAction action) {

    for (final User target : remoteUsers)
      rpm.monitorUpdated(
          new ProgressActivity(localUser, target, id, workCurrent, workTotal, taskName, action));
  }
}
