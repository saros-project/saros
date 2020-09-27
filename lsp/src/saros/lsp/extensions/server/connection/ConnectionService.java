package saros.lsp.extensions.server.connection;

import java.util.concurrent.CompletableFuture;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.net.ConnectionState;

public class ConnectionService implements IConnectionService, IConnectionStateListener {

  private final ConnectionHandler connectionHandler;
  private final XMPPAccountStore accountStore;
  private final ISarosLanguageClient client;

  public ConnectionService(
      ConnectionHandler connectionHandler,
      XMPPAccountStore accountStore,
      ISarosLanguageClient client) {
    this.connectionHandler = connectionHandler;
    this.accountStore = accountStore;
    this.client = client;

    this.connectionHandler.addConnectionStateListener(this);
  }

  @Override
  public CompletableFuture<SarosResponse> connect() {

    try {
      XMPPAccount account = this.accountStore.getDefaultAccount();

      this.connectionHandler.connect(account, false);

    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> disconnect() {

    try {
      this.connectionHandler.disconnect();

    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResultResponse<Boolean>> state() {
    return CompletableFuture.completedFuture(
        new SarosResultResponse<Boolean>(this.connectionHandler.isConnected()));
  }

  @Override
  public void connectionStateChanged(ConnectionState state, ErrorType errorType) {

    this.client.sendStateConnected(
        new SarosResultResponse<Boolean>(state == ConnectionState.CONNECTED));
  }
}
