package saros.negotiation;

/**
 * Listener interface for signaling termination of {@link SessionNegotiation} and {@link
 * ResourceNegotiation} negotiations.
 */
public interface NegotiationListener {

  /**
   * Called when a session negotiation has been terminated.
   *
   * @param negotiation the session negotiation that was terminated
   */
  public void negotiationTerminated(SessionNegotiation negotiation);

  /**
   * Called when a resource negotiation has been terminated.
   *
   * @param negotiation the resource negotiation that was terminated
   */
  public void negotiationTerminated(ResourceNegotiation negotiation);
}
