package de.fu_berlin.inf.dpp.server.session;

import de.fu_berlin.inf.dpp.communication.extensions.JoinSessionRequestExtension;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/** Listens for and handles JoinSessionRequests allowing clients to join server sessions. */
public final class JoinSessionRequestHandler {
  private final ISarosSessionManager sessionManager;

  private static final Logger log = Logger.getLogger(JoinSessionRequestHandler.class);

  /**
   * Invitation Requests are handled very fast and are almost immediately dispatched into another
   * executor in the {@link NegotiationHandler}. More then one thread is currently not necessary and
   * would likely just introduce more overhead. We just want asynchronous execution and a queue.
   */
  private final ThreadPoolExecutor executor =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.NANOSECONDS,
          new ArrayBlockingQueue<Runnable>(10),
          new NamedThreadFactory("JoinSessionRequestHandler-"),
          new ThreadPoolExecutor.DiscardPolicy());

  private final PacketListener joinSessionRequestListener =
      new PacketListener() {
        @Override
        public void processPacket(final Packet packet) {
          try {
            executor.execute(
                new Runnable() {
                  @Override
                  public void run() {
                    handleInvitationRequest(
                        new JID(packet.getFrom()),
                        JoinSessionRequestExtension.PROVIDER.getPayload(packet));
                  }
                });
          } catch (RejectedExecutionException e) {
            log.warn("Join Session request cannot be accepted (queue is full).", e);
          }
        }
      };

  public JoinSessionRequestHandler(ISarosSessionManager sessionManager, IReceiver receiver) {

    this.sessionManager = sessionManager;
    receiver.addPacketListener(
        joinSessionRequestListener, JoinSessionRequestExtension.PROVIDER.getPacketFilter());
  }

  private void handleInvitationRequest(final JID from, JoinSessionRequestExtension extension) {

    sessionManager.invite(from, "Invitation by request");
  }
}
