package de.fu_berlin.inf.dpp.server.dummies;

import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicator;
import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicatorFactory;
import de.fu_berlin.inf.dpp.monitoring.remote.RemoteProgressManager;
import de.fu_berlin.inf.dpp.session.User;

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
