package saros.core.monitoring.remote;

import saros.monitoring.remote.IRemoteProgressIndicator;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.monitoring.remote.RemoteProgressManager;
import saros.session.User;

/** IntelliJ implementation of the {@link IRemoteProgressIndicatorFactory} interface. */
public class IntelliJRemoteProgressIndicatorFactoryImpl implements IRemoteProgressIndicatorFactory {

  @Override
  public IRemoteProgressIndicator create(
      RemoteProgressManager remoteProgressManager, String remoteProgressID, User remoteUser) {
    return new IntelliJRemoteProgressIndicatorImpl(
        remoteProgressManager, remoteProgressID, remoteUser);
  }
}
