package saros.vscode;

import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import saros.server.console.ServerConsole;

/** The entry point for the Saros server. */
public class Saros {

  private static final Logger log = Logger.getLogger(Saros.class);
  private static final String LOGGING_CONFIG_FILE = "/log4j.properties";

  private Lifecycle lifecycle;

  /** The Saros version which is impersonated by the current server version. */
  // FIXME create a version handling that allows a separate server versioning
  // the current handling is tied to the current Saros/E versioning
  public static final String SAROS_VERSION = "15.0.0";

  /** Initializes and starts a Saros server. */
  public Saros() {
    lifecycle = new Lifecycle();
  }

  public void start() {

    // Logging
    URL log4jProperties = Saros.class.getResource(LOGGING_CONFIG_FILE);
    PropertyConfigurator.configure(log4jProperties);
    log.info("Starting saros...");

    lifecycle.start();
  }

  public void stop() {
    log.info("Stopping saros...");
    lifecycle.stop();
  }

  /**
   * Starts the server.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    final Saros server = new Saros();

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

    if (Config.isInteractive()) {
      server.lifecycle.getSarosContext().getComponent(ServerConsole.class).run();
      System.exit(0);
    }
  }
}
