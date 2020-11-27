package saros.negotiation;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.PacketExtension;
import saros.communication.extensions.CancelInviteExtension;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

public abstract class SessionNegotiation extends Negotiation {

  private static final Logger log = Logger.getLogger(SessionNegotiation.class);

  /** Timeout for all packet exchanges during the session negotiation */
  protected static final long PACKET_TIMEOUT =
      Long.getLong("saros.negotiation.session.PACKET_TIMEOUT", 30000L);

  /**
   * Timeout on how long the session negotiation should wait for the remote user to accept the
   * invitation
   */
  protected static final long INVITATION_ACCEPTED_TIMEOUT =
      Long.getLong("saros.negotiation.session.INVITATION_ACCEPTED_TIMEOUT", 600000L);

  /**
   * Timeout on how long the session negotiation should wait for the remote user to connect to the
   * host side.
   */
  protected static final long CONNECTION_ESTABLISHED_TIMEOUT =
      Long.getLong("saros.negotiation.session.CONNECTION_ESTABLISHED_TIMEOUT", 120000L);

  protected final SessionNegotiationHookManager hookManager;

  protected final ISarosSessionManager sessionManager;

  protected final String description;

  protected ISarosSession sarosSession;

  public SessionNegotiation(
      final String id,
      final JID peer,
      final String description,
      final ISarosSessionManager sessionManager,
      final SessionNegotiationHookManager hookManager,
      final ITransmitter transmitter,
      final IReceiver receiver) {
    super(id, peer, transmitter, receiver);

    this.sessionManager = sessionManager;
    this.hookManager = hookManager;
    this.description = description;
  }

  /** @return the user-provided informal description that can be provided with an invitation. */
  public String getDescription() {
    return description;
  }

  @Override
  protected void notifyCancellation(SarosCancellationException exception) {

    if (!(exception instanceof LocalCancellationException)) return;

    LocalCancellationException cause = (LocalCancellationException) exception;

    if (cause.getCancelOption() != CancelOption.NOTIFY_PEER) return;

    log.debug("notifying remote contact " + getPeer() + " of the local cancellation");

    PacketExtension notification =
        CancelInviteExtension.PROVIDER.create(
            new CancelInviteExtension(getID(), cause.getMessage()));

    transmitter.sendPacketExtension(getPeer(), notification);
  }

  @Override
  protected void notifyTerminated(NegotiationListener listener) {
    listener.negotiationTerminated(this);
  }
}
