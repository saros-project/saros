package de.fu_berlin.inf.dpp.server;

import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.context.ContainerContext;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;

/**
 * The entry point for the Saros server.
 */

/*
 * FIXME This is currently ALPHA, the server always assumes it can connect to
 * the XMPP server and never gets disconnected. Of course this is unrealistic !
 */
public class SarosServer {

    private static final Logger LOG = Logger.getLogger(SarosServer.class);
    private static final String LOGGING_CONFIG_FILE = "/log4j.properties";

    private ContainerContext context;

    /**
     * The Saros server's version.
     */
    // FIXME move to META-INF or config file
    public static final String SAROS_VERSION = "14.11.28.DEVEL";

    /**
     * Initializes and starts a Saros server.
     */
    public SarosServer() {
    }

    public void start() {

        // Logging
        URL log4jProperties = SarosServer.class
            .getResource(LOGGING_CONFIG_FILE);
        PropertyConfigurator.configure(log4jProperties);

        // Context
        ArrayList<IContextFactory> factories = new ArrayList<IContextFactory>();
        factories.add(new ServerContextFactory());

        context = new ContainerContext(factories, null);
        /*
         * Ensure that components which act on their own (rather than being
         * called from other components) are loaded.
         */
        context.initialize();

        connectToXMPPServer();
    }

    public void stop() {
        context.getComponent(ISarosSessionManager.class).stopSession(
            SessionEndReason.LOCAL_USER_LEFT);

        context.getComponent(ConnectionHandler.class).disconnect();

        context.dispose();
    }

    private void connectToXMPPServer() {
        String jidString = ServerConfig.getJID();
        String password = ServerConfig.getPassword();

        if (jidString == null || password == null) {
            LOG.fatal("XMPP credentials are missing! Pass the "
                + "system properties de.fu_berlin.inf.dpp.server.jid and"
                + "de.fu_berln.inf.dpp.server.password to the server");
            System.exit(1);
        }

        /*
         * TODO we do not need a JID store, add a method the ConnectionHandler
         * instead
         */
        XMPPAccountStore store = context.getComponent(XMPPAccountStore.class);
        XMPPAccount account = store.findAccount(jidString);
        if (account == null) {
            JID jid = new JID(jidString);
            account = store.createAccount(jid.getName(), password,
                jid.getDomain(), "", 0, true, true);
        }

        ConnectionHandler connectionHandler = context
            .getComponent(ConnectionHandler.class);
        connectionHandler.connect(account, false);

    }

    /**
     * Starts the server.
     * 
     * @param args
     *            command-line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        SarosServer server = new SarosServer();

        LOG.info("Starting server...");
        server.start();

        System.in.read();

        LOG.info("Stopping server...");
        server.stop();
    }
}