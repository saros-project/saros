package saros.server.dummies;

import saros.monitoring.remote.IRemoteProgressIndicator;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.monitoring.remote.RemoteProgressManager;
import saros.session.User;

/**
 * An implementation of {@link IRemoteProgressIndicatorFactory} which returns {@link
 * NullRemoteProgressIndicator} instances.
 */
public class NullRemoteProgressIndicatorFactory implements IRemoteProgressIndicatorFactory {

  @Override
  public IRemoteProgressIndicator create(
      RemoteProgressManager remoteProgressManager, String remoteProgressID, User remoteUser) {

    return new NullRemoteProgressIndicator(remoteProgressID, remoteUser);
  }
}
