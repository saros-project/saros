package saros.server.dummies;

import saros.activities.ProgressActivity;
import saros.monitoring.remote.IRemoteProgressIndicator;
import saros.session.User;

/** A dummy implementation of {@link IRemoteProgressIndicator} which just does nothing. */
public class NullRemoteProgressIndicator implements IRemoteProgressIndicator {

  private String remoteProgressID;
  private User remoteUser;

  NullRemoteProgressIndicator(final String remoteProgressID, final User remoteUser) {

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
  public void start() {
    // Do nothing
  }

  @Override
  public void stop() {
    // Do nothing
  }

  @Override
  public void handleProgress(ProgressActivity activity) {
    // Do nothing
  }
}
