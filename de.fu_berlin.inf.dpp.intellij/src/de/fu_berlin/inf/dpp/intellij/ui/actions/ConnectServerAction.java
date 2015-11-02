package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import org.picocontainer.annotations.Inject;

import javax.swing.JOptionPane;

/**
 * Connects to XMPP/Jabber server with given account or active account
 */
public class ConnectServerAction extends AbstractSarosAction {
    public static final String NAME = "connect";

    @Inject
    private XMPPAccountStore accountStore;

    @Inject
    private ConnectionHandler connectionHandler;

    @Override
    public String getActionName() {
        return NAME;
    }

    /**
     * Connects with the given user.
     */
    public void executeWithUser(String user) {
        XMPPAccount account = accountStore.findAccount(user);
        accountStore.setAccountActive(account);
        connectAccount(account);
        actionPerformed();
    }

    /**
     * Connects with active account from the {@link XMPPAccountStore}.
     */
    @Override
    public void execute() {
        XMPPAccount account = accountStore.getActiveAccount();
        connectAccount(account);
        actionPerformed();
    }

    /**
     * Connects an Account tothe XMPPService and sets it as active.
     *
     * @param account
     */
    private void connectAccount(XMPPAccount account) {
        LOG.info("Connecting server: [" + account.getUsername() + "@" + account
            .getServer() + "]");

        try {
            // TODO don't block UI
            connectionHandler.connect(account, false);
        } catch (RuntimeException e) {
            // TODO display user notification in connection listener
            JOptionPane.showMessageDialog(null,
                "An unexpected error occured: " + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            LOG.error("Could not connect " + account, e);
        }
    }
}
