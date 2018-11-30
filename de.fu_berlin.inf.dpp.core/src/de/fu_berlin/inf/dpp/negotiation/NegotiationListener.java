package de.fu_berlin.inf.dpp.negotiation;

/**
 * Listener interface for signaling termination of {@link SessionNegotiation} and {@link
 * ProjectNegotiation} negotiations.
 *
 * @author srossbach
 */
public interface NegotiationListener {

  /**
   * Called when a session negotiation has been terminated.
   *
   * @param negotiation the session negotiation that was terminated
   */
  public void negotiationTerminated(SessionNegotiation negotiation);

  /**
   * Called when a project negotiation has been terminated.
   *
   * @param negotiation the project negotiation that was terminated
   */
  public void negotiationTerminated(ProjectNegotiation negotiation);
}
