package de.fu_berlin.inf.dpp.stf.client.tester;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.client.Configuration;
import java.util.logging.Level;
import java.util.logging.Logger;

final class TesterFactory {

  private static final Logger LOGGER = Logger.getLogger(TesterFactory.class.getName());

  private static AbstractTester createInvalidTester(
      Exception exception, String name, Object jid, Object password, Object host, Object port) {

    return new InvalidTester(
        new JID(jid == null ? "not@set.in/config" : jid.toString()),
        password == null ? "" : password.toString(),
        new RuntimeException(
            "could not connect to RMI of bot '"
                + name
                + "' with JID: "
                + jid
                + ", host: "
                + host
                + ", port: "
                + port,
            exception));
  }

  public static synchronized AbstractTester createTester(String name) {

    Configuration configuration = Configuration.getInstance();

    AbstractTester tester;

    name = name.toUpperCase();

    LOGGER.info("initializing bot for name '" + name + "'");

    Object jid = null;
    Object password = null;
    Object host = null;
    Object port = null;

    jid = configuration.get(name + "_JID");
    password = configuration.get(name + "_PASSWORD");
    host = configuration.get(name + "_HOST");
    port = configuration.get(name + "_PORT");

    try {
      if (jid == null)
        throw new IllegalArgumentException(
            "JID for bot '" + name + "' is not set in the property file(s)");

      if (password == null)
        throw new IllegalArgumentException(
            "password for bot '" + name + "' is not set in the property file(s)");

      if (host == null)
        throw new IllegalArgumentException(
            "host for bot '" + name + "' is not set in the property file(s)");

      if (port == null)
        throw new IllegalArgumentException(
            "port for bot '" + name + "' is not set in the property file(s)");
    } catch (IllegalArgumentException e) {
      return createInvalidTester(e, name, jid, password, host, port);
    }

    try {
      tester =
          new RealTester(
              new JID(jid.toString()),
              password.toString(),
              host.toString(),
              Integer.parseInt(port.toString()));
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "could not create tester: " + name, e);
      return createInvalidTester(e, name, jid, password, host, port);
    }

    LOGGER.info(
        "created bot '" + name + "' with JID: " + jid + ", host: " + host + ", port: " + port);

    return tester;
  }
}
