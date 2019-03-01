package saros.monitoring.remote;

import saros.annotations.Component;
import saros.session.User;

/** Eclipse-specific implementation of the {@link IRemoteProgressIndicatorFactory} interface. */
@Component(module = "core")
public class EclipseRemoteProgressIndicatorFactoryImpl implements IRemoteProgressIndicatorFactory {

  /** {@inheritDoc} */
  @Override
  public IRemoteProgressIndicator create(
      RemoteProgressManager remoteProgressManager, String remoteProgressID, User source) {

    return new EclipseRemoteProgressIndicatorImpl(remoteProgressManager, remoteProgressID, source);
  }
}
