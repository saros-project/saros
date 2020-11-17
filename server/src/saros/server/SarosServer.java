package saros.server;

import org.apache.log4j.Logger;
import saros.server.console.ServerConsole;

/** The entry point for the Saros server. */
public class SarosServer {

  private static final Logger log = Logger.getLogger(SarosServer.class);

  private ServerLifecycle lifecycle;

  /** The Saros version which is impersonated by the current server version. */
  // FIXME create a version handling that allows a separate server versioning
  // the current handling is tied to the current Saros/E versioning
  public static final String SAROS_VERSION = "16.0.1";

  /** Initializes and starts a Saros server. */
  public SarosServer() {
    lifecycle = new ServerLifecycle();
  }

  public void start() {

    // Logger uses default config log4j2.xml

    log.info("Starting server...");

    lifecycle.start();
  }

  public void stop() {
    log.info("Stopping server...");
    lifecycle.stop();
  }

  /**
   * Starts the server.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    final SarosServer server = new SarosServer();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    server.stop();
                  }
                }));

    server.start();

    if (ServerConfig.isInteractive()) {
      server.lifecycle.getSarosContext().getComponent(ServerConsole.class).run();
      System.exit(0);
    }
  }
}
