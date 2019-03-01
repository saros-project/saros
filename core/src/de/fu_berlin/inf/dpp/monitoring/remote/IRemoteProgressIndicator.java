package de.fu_berlin.inf.dpp.monitoring.remote;

import de.fu_berlin.inf.dpp.activities.ProgressActivity;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Interface for displaying the progress of a {@link RemoteProgressMonitor} on the
 * progress-activity-receiving side. Implementations are application-specific and accompanied by a
 * matching {@link IRemoteProgressIndicatorFactory}.
 */
public interface IRemoteProgressIndicator {

  /**
   * Returns the ID of the tracked remote progress. This is equivalent to the ID of the {@link
   * RemoteProgressMonitor} on the sender's side.
   *
   * @return unique ID of the remote progress
   */
  public String getRemoteProgressID();

  /**
   * Returns the user which generates the tracked remote progress.
   *
   * @return associated remote user
   */
  public User getRemoteUser();

  /** Starts displaying remote progress passed in through {@link #handleProgress} to the user. */
  public void start();

  /**
   * Stops displaying remote progress to the user. It is assumed that {@link #handleProgress} is not
   * called anymore after {@link #stop()}.
   */
  public void stop();

  /**
   * Called when a new progress activity has been received from the indicator's associated remote
   * monitor. The indicator is expected to display the progress to the user.
   *
   * @param activity received progress activity
   */
  public void handleProgress(ProgressActivity activity);
}
