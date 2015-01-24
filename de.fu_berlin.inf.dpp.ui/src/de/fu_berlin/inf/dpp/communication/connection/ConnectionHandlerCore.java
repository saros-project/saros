package de.fu_berlin.inf.dpp.communication.connection;

import de.fu_berlin.inf.dpp.net.xmpp.ConnectionConfigurationFactory;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountLocator;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;

/**
 * TODO the whole connection handler has to be moved to the core
 * this is just a stub which is needed by the HTML-UI
 */
public class ConnectionHandlerCore {

    @Inject
    private XMPPAccountLocator accountLocator;

    @Inject
    private XMPPConnectionService connectionService;

    @Inject
    private XMPPAccountStore accountStore;

    /**
     * Connects the XMPP account with the given name
     *
     * @param username the username of the XMPP account
     */
    public void connectUser(String username) {
        final XMPPAccount account = accountLocator.findAccount(username);
        try {
            connectionService.connect(ConnectionConfigurationFactory
                    .createConnectionConfiguration(account.getDomain(),
                        account.getServer(), account.getPort(),
                        account.useTLS(), account.useSASL()),
                account.getUsername(), account.getPassword());
        } catch (XMPPException e) {
            //TODO error notification / handling
            throw new RuntimeException(e);
        }
        accountStore.setAccountActive(account);
    }
}
