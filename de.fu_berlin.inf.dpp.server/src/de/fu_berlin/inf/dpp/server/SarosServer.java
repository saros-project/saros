package de.fu_berlin.inf.dpp.server;

import de.fu_berlin.inf.dpp.server.console.ServerConsole;
import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/** The entry point for the Saros server. */
public class SarosServer {

  private static final Logger LOG = Logger.getLogger(SarosServer.class);
  private static final String LOGGING_CONFIG_FILE = "/log4j.properties";

  private ServerLifecycle lifecycle;

  /** The Saros server's version. */
  // FIXME move to META-INF or config file
  public static final String SAROS_VERSION = "14.11.28.DEVEL";

  /** Initializes and starts a Saros server. */
  public SarosServer() {
    lifecycle = new ServerLifecycle();
  }

  public void start() {

    // Logging
    URL log4jProperties = SarosServer.class.getResource(LOGGING_CONFIG_FILE);
    PropertyConfigurator.configure(log4jProperties);

    lifecycle.start();
  }

  public void initConsole(ServerConsole console) {
    // no commands (yet) to register
  }

  public void stop() {
    lifecycle.stop();
  }

  /**
   * Starts the server.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    final SarosServer server = new SarosServer();

    LOG.info("Starting server...");
    server.start();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    LOG.info("Stopping server...");
                    server.stop();
                  }
                }));

    if (ServerConfig.isInteractive()) {
      ServerConsole console = new ServerConsole(System.in, System.out);
      server.initConsole(console);
      console.run();
    }
  }
}
