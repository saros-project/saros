package saros.ui.core_facades;

import saros.HTMLUIContextFactory;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;

/** Bundles backend calls for connecting to and disconnecting from a server. */
public class ConnectionFacade {

  private final ConnectionHandler connectionHandler;

  private final XMPPAccountStore accountStore;

  /**
   * Created by PicoContainer
   *
   * @param connectionHandler
   * @param accountStore
   * @see HTMLUIContextFactory
   */
  public ConnectionFacade(ConnectionHandler connectionHandler, XMPPAccountStore accountStore) {

    this.connectionHandler = connectionHandler;
    this.accountStore = accountStore;
  }

  /**
   * Connects the given XMPP account to the server.
   *
   * @param account representing an XMPP account
   */
  public void connect(XMPPAccount account) {
    accountStore.setAccountActive(account);
    connectionHandler.connect(account, false);
  }

  /** Disconnects the currently connected account. */
  public void disconnect() {
    connectionHandler.disconnect();
  }
}
