package saros.lsp;

import java.io.IOException;
import java.net.Socket;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.log.LanguageClientAppender;

/** Entry point for the Saros LSP server. */
public class SarosLauncher {

  private static final Logger log = Logger.getLogger(SarosLauncher.class);

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

    // Logger uses default config log4j2.xml

    int port = Integer.parseInt(args[0]);
    Socket socket = new Socket("localhost", port);

    log.info("listening on port " + port);

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
    log.addAppender(new LanguageClientAppender(langClt));
    langSvr.connect(langClt);

    l.startListening();
  }
}
