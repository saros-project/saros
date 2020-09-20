package saros.lsp;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Function;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.lookup.MainMapLookup;
import org.apache.logging.log4j.status.StatusLogger;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.filesystem.IWorkspacePath;
import saros.lsp.filesystem.WorkspacePath;
import saros.lsp.log.LanguageClientAppender;

/** Entry point for the Saros language server. */
public class SarosLauncher implements Callable<Integer> {

  private static final Logger LOG = Logger.getLogger(SarosLauncher.class);
  private static final String LOG4J2_CONFIG_FILENAME = "saros_log4j2.xml";

  @Option(
      names = {"-p", "--port"},
      description = "The port to listen on")
  int port;

  @Option(
      names = {"-l", "--log"},
      description = "The log level")
  String logLevel;

  @Override
  public Integer call() throws Exception {
    setupLoggers();
    LOG.info("listening on port " + port);

    final AsynchronousServerSocketChannel serverSocket =
        AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("localhost", port));

    final SarosLifecycle lifecycle = new SarosLifecycle();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override()
              public void run() {
                lifecycle.stop();
              }
            });

    lifecycle.start();

    startLanguageServer(lifecycle, serverSocket);

    return 0;
  }

  /**
   * Starts the Saros language server.
   *
   * @param args command-line arguments
   * @throws Exception on critical failures
   */
  public static void main(final String... args) {
    new CommandLine(new SarosLauncher()).execute(args);
  }

  /**
   * Starts the Saros language server.
   *
   * @param lifecycle The lifecycle of Saros
   * @param serverSocket The used socket for a client connection
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  private void startLanguageServer(
      final SarosLifecycle lifecycle, final AsynchronousServerSocketChannel serverSocket)
      throws IOException, InterruptedException, ExecutionException {

    LOG.info("starting saros language server...");

    final ISarosLanguageServer langSvr = lifecycle.getLanguageServer();
    final AsynchronousSocketChannel socket = createSocket(serverSocket);

    LOG.info("connected to client");

    final Launcher<ISarosLanguageClient> l =
        createClientLauncher(langSvr, ISarosLanguageClient.class, socket);
    final ISarosLanguageClient langClt = lifecycle.registerLanguageClient(l.getRemoteProxy());

    registerClientAppender(langClt);

    langSvr.onInitialize(
        params -> {
          try {
            IWorkspacePath root = new WorkspacePath(new URI(params.getRootUri()));
            lifecycle.registerWorkspace(root);
          } catch (URISyntaxException e) {
            LOG.error(e);
          }
        });

    l.startListening();
  }

  /**
   * Registers a log appender that logs to the Saros language client.
   *
   * @param client The Saros language client
   */
  private void registerClientAppender(final ISarosLanguageClient client) {
    final LanguageClientAppender clientAppender = new LanguageClientAppender(client);
    clientAppender.setThreshold(Level.toLevel(this.logLevel));
    LOG.addAppender(clientAppender);
  }

  /**
   * Creates a server socket for receiving Saros language client connections and waits for the
   * client to connect.
   *
   * @param serverSocket The used server socket
   * @return The socket channel with a client connection
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  static AsynchronousSocketChannel createSocket(final AsynchronousServerSocketChannel serverSocket)
      throws IOException, InterruptedException, ExecutionException {

    AsynchronousSocketChannel socketChannel;

    socketChannel = serverSocket.accept().get();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override()
              public void run() {
                try {
                  LOG.info("shutdown from runtime detected");
                  socketChannel.close();
                } catch (final IOException e) {
                  // NOP
                }
              }
            });

    return socketChannel;
  }

  /**
   * Creates the launcher of the language client that will allow communications to the client.
   *
   * @param <T> Type of the client
   * @param languageServer The local service endpoint
   * @param remoteInterface The class of the client
   * @param socketChannel The used socket channel for communication
   * @return The launcher for the specified language client
   * @throws IOException
   */
  static <T> Launcher<T> createClientLauncher(
      final Object languageServer,
      final Class<T> remoteInterface,
      final AsynchronousSocketChannel socketChannel)
      throws IOException {
    final Function<MessageConsumer, MessageConsumer> wrapper =
        consumer -> {
          final MessageConsumer result = consumer;
          return result;
        };

    return Launcher.createIoLauncher(
        languageServer,
        remoteInterface,
        Channels.newInputStream(socketChannel),
        Channels.newOutputStream(socketChannel),
        Executors.newCachedThreadPool(),
        wrapper);
  }

  private void setupLoggers() {
    try {
      final String logDir = System.getProperty("user.home") + File.separator + "SarosLogs";
      final boolean isDebugMode = Boolean.getBoolean("saros.debug");
      final Level logLevel = isDebugMode ? Level.ALL : Level.INFO;

      // make arguments accessible in the log configuration file
      MainMapLookup.setMainArguments("logDir", logDir, "logLevel", "WARN");

      // trigger reconfiguration with new properties and config file
      Configurator.initialize(
          null,
          ConfigurationSource.fromResource(
              LOG4J2_CONFIG_FILENAME, SarosLauncher.class.getClassLoader()));
    } catch (RuntimeException e) {
      StatusLogger.getLogger().error("initializing loggers failed", e);
      e.printStackTrace();
    }
  }
}
