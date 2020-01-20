package saros.lsp;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.log.LanguageClientAppender;

/** Entry point for the Saros LSP server. */
public class SarosLauncher {

  private static final Logger LOG = Logger.getLogger(SarosLauncher.class);
  private static final String LOGGING_CONFIG_FILE = "/log4j.properties";

  /**
   * Starts the server.
   *
   * @param args command-line arguments
   * @throws Exception on critical failures
   */
  public static void main(String[] args) throws Exception {

    if (args.length > 1) {
      throw new IllegalArgumentException("wrong number of arguments");
    } else if (args.length != 1) {
      throw new IllegalArgumentException("port parameter not supplied");
    } else if (!args[0].matches("\\d+")) {
      throw new IllegalArgumentException("port is not a number");
    }

    URL log4jProperties = SarosLauncher.class.getResource(LOGGING_CONFIG_FILE);
    PropertyConfigurator.configure(log4jProperties);

    LOG.addAppender(new ConsoleAppender());

    int port = Integer.parseInt(args[0]);
    Socket socket = new Socket("localhost", port);

    LOG.info("listening on port " + port);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              public void run() {
                try {
                  socket.close();
                } catch (IOException e) {
                  // NOP
                }
              }
            });

    SarosLanguageServer langSvr = new SarosLanguageServer();
    Launcher<ISarosLanguageClient> l =
        Launcher.createLauncher(
            langSvr, ISarosLanguageClient.class, socket.getInputStream(), socket.getOutputStream());

    ISarosLanguageClient langClt = l.getRemoteProxy();
    LOG.addAppender(new LanguageClientAppender(langClt));
    langSvr.connect(langClt);

    l.startListening();
  }
}
