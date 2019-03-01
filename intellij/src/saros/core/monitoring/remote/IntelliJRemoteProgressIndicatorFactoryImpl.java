package de.fu_berlin.inf.dpp.core.monitoring.remote;

import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicator;
import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicatorFactory;
import de.fu_berlin.inf.dpp.monitoring.remote.RemoteProgressManager;
import de.fu_berlin.inf.dpp.session.User;

/** IntelliJ implementation of the {@link IRemoteProgressIndicatorFactory} interface. */
public class IntelliJRemoteProgressIndicatorFactoryImpl implements IRemoteProgressIndicatorFactory {

  @Override
  public IRemoteProgressIndicator create(
      RemoteProgressManager remoteProgressManager, String remoteProgressID, User remoteUser) {
    return new IntelliJRemoteProgressIndicatorImpl(
        remoteProgressManager, remoteProgressID, remoteUser);
  }
}
