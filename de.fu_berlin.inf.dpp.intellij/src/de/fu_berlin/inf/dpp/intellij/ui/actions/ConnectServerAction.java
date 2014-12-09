/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountLocator;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.xmpp.ConnectionConfigurationFactory;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
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
    private XMPPAccountLocator accountLocator;

    @Inject
    private XMPPConnectionService connectionService;

    @Override
    public String getActionName() {
        return NAME;
    }

    /**
     * Connects with the given user.
     */
    public void executeWithUser(String user) {
        XMPPAccount account = accountLocator.findAccount(user);
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
            //FIXME: This should use the ConnectionHandler instead
            ConnectionConfiguration connectionConfiguration = ConnectionConfigurationFactory
                .createConnectionConfiguration(account.getDomain(),
                    account.getServer(), account.getPort(), account.useTLS(),
                    account.useSASL());
            connectionService.connect(connectionConfiguration, account.getUsername(),
                account.getPassword());
            accountStore.setAccountActive(account);
        } catch (Exception e) {
            if (!(e instanceof XMPPException)) {
                JOptionPane.showMessageDialog(null,
                    "An unexpected error occured: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                    generateHumanReadableErrorMessage((XMPPException) e),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
            LOG.error("Could not connect " + account, e);
        }
    }

    private String generateHumanReadableErrorMessage(XMPPException e) {
        //FIXME: Copy from de.fu_berlin.inf.dpp.ui.eventhandler.ConnectingFailureHandler
        //must be consolidated together with ConnectionHandler.

        // as of Smack 3.3.1 this is always null for connection attemps
        // Throwable cause = e.getWrappedThrowable();

        XMPPError error = e.getXMPPError();

        if (error != null && error.getCode() == 504)
            return
                "The XMPP server could not be found. Make sure that you entered the domain part of your JID correctly.\n\nIn case of DNS or SRV problems please try to manually configure the server address and port under the advanced settings for this account or update the hosts file of your OS.\n\n"
                    + "You should change your account settings (by deletings .saros/idea_config.dat and recreating it)."
                    + "\n\nDetailed error:\nSMACK: " + error + "\n" + e
                    .getMessage();
        else if (error != null && error.getCode() == 502)
            return
                "Could not connect to the XMPP server. Make sure that a XMPP service is running on the given domain / IP address and port.\n\nIn case of DNS or SRV problems please try to manually configure the server address and port under the advanced settings for this account or update the hosts file of your OS.\n\n"
                    + "You should change your account settings (by deletings .saros/idea_config.dat and recreating it)."
                    + "\n\nDetailed error:\nSMACK: " + error + "\n" + e
                    .getMessage();

        String question = null;

        String errorMessage = e.getMessage();

        if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("invalid-authzid")
                //jabber.org got it wrong ...
                || errorMessage.toLowerCase().contains("not-authorized") // SASL
                || errorMessage.toLowerCase().contains("403") // non SASL
                || errorMessage.toLowerCase().contains("401")) { // non SASL

                question = "Invalid username or password.\n\n"
                    + "You should change your account settings (by deletings .saros/idea_config.dat and recreating it).";
            } else if (errorMessage.toLowerCase().contains("503")) {
                question =
                    "The XMPP server only allows authentication via SASL.\nPlease enable SASL for the current account in the account options and try again.\n\n"
                        + "You should change your account settings (by deletings .saros/idea_config.dat and recreating it).";
            }
        }

        if (question == null)
            question = "Could not connect to XMPP server.\n\n"
                + "You should change your account settings (by deletings .saros/idea_config.dat and recreating it).";

        return question;

    }
}
