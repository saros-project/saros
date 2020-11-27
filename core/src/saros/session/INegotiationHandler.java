package saros.session;

import saros.negotiation.AbstractIncomingResourceNegotiation;
import saros.negotiation.AbstractOutgoingResourceNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.OutgoingSessionNegotiation;

/**
 * Interface for handling incoming and outgoing session and resource negotiations. @Note
 * Implementations <b>must not</b> block on all methods that are provided by this interface.
 * Furthermore it is possible that the methods are called concurrently.
 */
public interface INegotiationHandler {

  /**
   * Called when a session invitation is offered to a contact.
   *
   * @param negotiation the negotiation to use for executing the invitation
   */
  public void handleOutgoingSessionNegotiation(OutgoingSessionNegotiation negotiation);

  /**
   * Called when an invitation to a session is received from a contact.
   *
   * @param negotiation the negotiation to use for handling the invitation
   */
  public void handleIncomingSessionNegotiation(IncomingSessionNegotiation negotiation);

  /**
   * Called when a local reference points should be synchronized with a remote session user.
   *
   * @param negotiation the negotiation to use for executing the resource synchronization
   */
  public void handleOutgoingResourceNegotiation(AbstractOutgoingResourceNegotiation negotiation);

  /**
   * Called when a remote reference point from a remote session user should be synchronized with a
   * local reference point.
   *
   * @param negotiation the negotiation to use for handling the resource synchronization
   */
  public void handleIncomingResourceNegotiation(AbstractIncomingResourceNegotiation negotiation);
}
