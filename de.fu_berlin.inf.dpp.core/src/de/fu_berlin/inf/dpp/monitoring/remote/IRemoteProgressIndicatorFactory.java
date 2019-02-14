package de.fu_berlin.inf.dpp.monitoring.remote;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Interface for constructing {@link IRemoteProgressIndicator} instances of the correct
 * (application-specific) type.
 */
@Component(module = "core")
public interface IRemoteProgressIndicatorFactory {

  /**
   * Creates a remote progress indicator.
   *
   * @param remoteProgressManager {@link RemoteProgressManager} which manages the indicator
   * @param remoteProgressID ID of the remote user's {@link RemoteProgressMonitor} to watch
   * @param remoteUser the user who owns the watched remote progress monitor
   * @return progress indicator
   */
  public IRemoteProgressIndicator create(
      RemoteProgressManager remoteProgressManager, String remoteProgressID, User remoteUser);
}
