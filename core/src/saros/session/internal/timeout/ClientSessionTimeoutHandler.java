package saros.session.internal.timeout;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import saros.communication.extensions.PingExtension;
import saros.communication.extensions.PongExtension;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.internal.ActivitySequencer;
import saros.util.ThreadUtils;

/**
 * Component for detecting network errors on the client side of a session.
 *
 * @author srossbach
 */
public final class ClientSessionTimeoutHandler extends SessionTimeoutHandler {

  private static final Logger LOG = Logger.getLogger(ClientSessionTimeoutHandler.class);

  private boolean shutdown;

  private boolean pingReceived;

  private long lastPingReceived;

  private Thread workerThread;

  private final PacketListener pingPacketListener =
      new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
          synchronized (ClientSessionTimeoutHandler.this) {
            lastPingReceived = System.currentTimeMillis();
            pingReceived = true;
            ClientSessionTimeoutHandler.this.notifyAll();
          }
        }
      };

  private final Runnable clientSessionTimeoutWatchdog =
      new Runnable() {

        @Override
        public void run() {
          while (true) {

            boolean abort = false;

            synchronized (ClientSessionTimeoutHandler.this) {
              while (!pingReceived && !shutdown) {
                try {
                  ClientSessionTimeoutHandler.this.wait(PING_PONG_UPDATE_DELAY);
                } catch (InterruptedException e) {
                  if (!shutdown) LOG.error("watchdog shutdown prematurely", e);

                  return;
                }

                if ((System.currentTimeMillis() - lastPingReceived) > PING_PONG_TIMEOUT) {
                  abort = true;
                  break;
                }
              }

              if (shutdown) return;

              pingReceived = false;
            }

            if (abort) {
              LOG.error("no ping received, reached timeout = " + PING_PONG_TIMEOUT);
              handleNetworkError(session.getHost().getJID(), "rx");
              return;
            }

            try {
              transmitter.send(
                  ISarosSession.SESSION_CONNECTION_ID,
                  session.getHost().getJID(),
                  PongExtension.PROVIDER.create(new PongExtension(currentSessionID)));
            } catch (IOException e) {
              LOG.error("failed to send pong", e);
              handleNetworkError(session.getHost().getJID(), "tx");
            }
          }
        }
      };

  public ClientSessionTimeoutHandler(
      ISarosSession session,
      ISarosSessionManager sessionManager,
      ActivitySequencer sequencer,
      ITransmitter transmitter,
      IReceiver receiver) {
    super(session, sessionManager, sequencer, transmitter, receiver);
  }

  @Override
  public void start() {

    if (session.isHost())
      throw new IllegalStateException("component cannot be started in host mode");

    super.start();

    lastPingReceived = System.currentTimeMillis();

    receiver.addPacketListener(
        pingPacketListener, PingExtension.PROVIDER.getPacketFilter(currentSessionID));

    workerThread =
        ThreadUtils.runSafeAsync("dpp-client-network-watchdog", LOG, clientSessionTimeoutWatchdog);
  }

  @Override
  public void stop() {
    super.stop();

    receiver.removePacketListener(pingPacketListener);

    synchronized (this) {
      shutdown = true;
      notifyAll();
    }

    try {
      workerThread.join(TIMEOUT);
    } catch (InterruptedException e) {
      LOG.warn("interrupted while waiting for " + workerThread.getName() + " thread to terminate");

      Thread.currentThread().interrupt();
    }

    if (workerThread.isAlive()) LOG.error(workerThread.getName() + " thread is still running");
  }
}
