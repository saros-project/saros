package saros.lsp.monitoring.remote;

import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.monitoring.remote.IRemoteProgressIndicator;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.monitoring.remote.RemoteProgressManager;
import saros.session.User;

/** Implementation of {@link IRemoteProgressIndicatorFactory}. */
public class LspRemoteProgressIndicatorFactory implements IRemoteProgressIndicatorFactory {

  private ISarosLanguageClient client;

  public LspRemoteProgressIndicatorFactory(ISarosLanguageClient client) {
    this.client = client;
  }

  @Override
  public IRemoteProgressIndicator create(
      RemoteProgressManager remoteProgressManager, String remoteProgressID, User remoteUser) {

    return new LspRemoteProgressIndicator(this.client, remoteProgressID, remoteUser);
  }
}
