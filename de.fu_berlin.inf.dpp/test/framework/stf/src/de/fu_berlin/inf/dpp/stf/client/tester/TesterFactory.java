package de.fu_berlin.inf.dpp.stf.client.tester;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Configuration;

final class TesterFactory {

    private final static Logger log = Logger.getLogger(TesterFactory.class);

    public synchronized static AbstractTester createTester(String name) {

        Configuration configuration = Configuration.getInstance();

        AbstractTester tester;

        name = name.toUpperCase();

        log.debug("initializing bot for name '" + name + "'");

        Object jid = null;
        Object password = null;
        Object host = null;
        Object port = null;

        try {

            jid = configuration.get(name + "_JID");
            password = configuration.get(name + "_PASSWORD");
            host = configuration.get(name + "_HOST");
            port = configuration.get(name + "_PORT");

            if (jid == null)
                throw new NullPointerException("JID for bot '" + name
                    + "' is not set in the property file(s)");

            if (password == null)
                throw new NullPointerException("password for bot '" + name
                    + "' is not set in the property file(s)");

            if (host == null)
                throw new NullPointerException("host for bot '" + name
                    + "' is not set in the property file(s)");

            if (port == null)
                throw new NullPointerException("port for bot '" + name
                    + "' is not set in the property file(s)");

            tester = new RealTester(new JID(jid.toString()),
                password.toString(), host.toString(), Integer.parseInt(port
                    .toString()));

            log.info("created bot '" + name + "' with JID: " + jid
                + ", password: " + password + ", host: " + host + ", port: "
                + port);

        } catch (Exception e) {
            log.debug("error while initializing bot", e);
            tester = new InvalidTester(new RuntimeException(
                "could not connect to RMI of bot '" + name + "' with JID: "
                    + jid + ", password: " + password + ", host: " + host
                    + ", port: " + port + ", " + e.getMessage(), e.getCause()

            ));
        }

        return tester;

    }
}
