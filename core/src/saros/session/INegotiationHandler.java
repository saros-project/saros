package saros.session;

import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.AbstractOutgoingProjectNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.OutgoingSessionNegotiation;

/**
 * Interface for handling incoming and outgoing session and project negotiations. @Note
 * Implementations <b>must not</b> block on all methods that are provided by this interface.
 * Furthermore it is possible that the methods are called concurrently.
 *
 * @author srossbach
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
   * Called when a local project should be synchronized with a remote session user.
   *
   * @param negotiation the negotiation to use for executing the project synchronization
   */
  public void handleOutgoingProjectNegotiation(AbstractOutgoingProjectNegotiation negotiation);

  /**
   * Called when a remote project from a remote session user should be synchronized with a local
   * project.
   *
   * @param negotiation the negotiation to use for handling the project synchronization
   */
  public void handleIncomingProjectNegotiation(AbstractIncomingProjectNegotiation negotiation);
}
