package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import org.apache.log4j.Logger;

// TODO JavaDoc

// Accepts incoming TCP connections
public class TCPServer {

  private static final Logger LOG = Logger.getLogger(TCPServer.class);

  private ServerSocket server;

  private Thread connectionAcceptThread;

  private volatile boolean running;

  private final Runnable acceptRunnable =
      new Runnable() {

        @Override
        public void run() {
          while (running) {

            final Socket client;

            try {
              client = server.accept();
            } catch (IOException e) {
              if (!running) return;

              LOG.error("server socket is closed", e);
              return;
            }

            LOG.debug("accept request from: " + client.getRemoteSocketAddress());

            // TODO pass to logic
            try {
              client.close();
            } catch (Exception e) {
              // ignore
            }
          }
        }
      };

  /**
   * Starts a server on the given address and port.
   *
   * @param address the address to bind the server to or <code>null</code>
   * @param port the port to use, if negative the server will try to bind to a free port beginning
   *     with the given port number
   * @throws IOException if the server could not been started
   * @return the port number the server was bound to or -1 is already started
   */
  public synchronized int start(InetAddress address, int port) throws IOException {

    final int MAX_PORT = 65535;

    if (running) {
      LOG.warn("server is already started");
      return -1;
    }

    boolean searchFreePort = false;

    if (port < 0) {
      port = -port;
      searchFreePort = true;
    }

    if (port <= 0 || port > MAX_PORT) throw new IOException("invalid port number: " + port);

    server = new ServerSocket();

    while (port <= MAX_PORT) {
      final SocketAddress serverAddress = new InetSocketAddress(address, port);

      try {
        server.bind(serverAddress);
        break;
      } catch (IOException e) {
        if (!searchFreePort) {
          LOG.error("failed to bind socket to: " + serverAddress);
          closeServerSocket(server);
          throw e;
        }
      } catch (IllegalArgumentException e) {
        closeServerSocket(server);
        throw new IOException("internet address " + address + " is not supported", e);
      }

      port++;
    }

    if (port > MAX_PORT) {
      closeServerSocket(server);
      throw new IOException("failed to bind socket, no free ports available");
    }

    running = true;

    connectionAcceptThread = ThreadUtils.runSafeAsync("TCP-Server-Accept", LOG, acceptRunnable);

    LOG.info("server started on: " + server.getLocalSocketAddress());
    return port;
  }

  /** Stops the currently running server. */
  public synchronized void stop() {
    if (!running) {
      LOG.warn("server is not started");
      return;
    }

    closeServerSocket(server);

    try {
      connectionAcceptThread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.error("interrupted while stopping server");
      return;
    }

    LOG.info("server stopped on: " + server.getLocalSocketAddress());

    server = null;
    connectionAcceptThread = null;
    running = false;
  }

  private static void closeServerSocket(ServerSocket socket) {
    if (socket == null) return;

    try {
      socket.close();
    } catch (IOException e) {
      LOG.warn("failed to server close socket: " + socket, e);
    }
  }
}
