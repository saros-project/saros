package de.fu_berlin.inf.dpp.net.util;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Registration;

import de.fu_berlin.inf.dpp.net.XMPPUtil;

/**
 * @author cordes
 */
public final class XMPPAccountUtils {
    private static Logger LOG = Logger.getLogger(XMPPAccountUtils.class);

    private XMPPAccountUtils() {
        //
    }

    /**
     * Creates the given account on the given XMPP server.
     * 
     * @blocking
     * 
     * @param server
     *            the server on which to create the account.
     * @param username
     *            the username for the new account.
     * @param password
     *            the password for the new account.
     * @param monitor
     *            the progressmonitor for the operation.
     * @throws org.jivesoftware.smack.XMPPException
     *             exception that occurs while registering.
     */
    public static void createAccount(String server, String username,
        String password, IProgressMonitor monitor) throws XMPPException {

        monitor.beginTask("Registering account", 3);

        try {
            XMPPConnection connection = new XMPPConnection(server);
            monitor.worked(1);

            connection.connect();
            monitor.worked(1);

            Registration registration = null;
            try {
                registration = XMPPUtil.getRegistrationInfo(username,
                    connection);
            } catch (XMPPException e) {
                LOG.error("Server " + server + " does not support XEP-0077"
                    + " (In-Band Registration) properly:", e);
            }
            if (registration != null) {
                if (registration.getAttributes().containsKey("registered")) {
                    throw new XMPPException("Account " + username
                        + " already exists on server");
                }
                if (!registration.getAttributes().containsKey("username")) {
                    String instructions = registration.getInstructions();
                    if (instructions != null) {
                        throw new XMPPException(
                            "Registration via Saros not possible. Please follow these instructions:\n"
                                + instructions);
                    } else {
                        throw new XMPPException(
                            "Registration via Saros not supported by Server. Please see the server web site for informations for how to create an account");
                    }
                }
            }
            monitor.worked(1);

            AccountManager manager = connection.getAccountManager();
            manager.createAccount(username, password);
            monitor.worked(1);

            connection.disconnect();
        } finally {
            monitor.done();
        }
    }

    public static void deleteUserAccoountOnServer(String serverAddress,
        String username, String defaultPassword) throws XMPPException {
        XMPPConnection connection = new XMPPConnection(serverAddress);
        connection.connect();
        connection.login(username, defaultPassword);
        AccountManager manager = connection.getAccountManager();
        manager.deleteAccount();
        connection.disconnect();
    }
}
