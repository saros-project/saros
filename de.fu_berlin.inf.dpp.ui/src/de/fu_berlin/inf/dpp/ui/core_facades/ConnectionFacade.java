package de.fu_berlin.inf.dpp.ui.core_facades;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;

/**
 * Bundles all backend calls for connecting to and disconnecting from a server.
 */
public class ConnectionFacade {

    private final ConnectionHandler connectionHandler;

    private final XMPPConnectionService connectionService;

    private final XMPPAccountStore accountStore;

    /**
     * Created by PicoContainer
     * 
     * @param connectionHandler
     * @param connectionService
     * @param accountStore
     * @see HTMLUIContextFactory
     */
    public ConnectionFacade(ConnectionHandler connectionHandler,
        XMPPConnectionService connectionService, XMPPAccountStore accountStore) {

        this.connectionHandler = connectionHandler;
        this.connectionService = connectionService;
        this.accountStore = accountStore;
    }

    /**
     * Connects the given XMPP account to the server.
     * 
     * @param account
     *            representing an XMPP account
     */
    public void connect(XMPPAccount account) {
        accountStore.setAccountActive(account);
        connectionHandler.connect(account, false);
    }

    /**
     * Disconnects the currently connected account.
     */
    public void disconnect() {
        connectionService.disconnect();
    }
}