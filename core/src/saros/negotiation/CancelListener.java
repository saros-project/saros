package saros.negotiation;

import saros.negotiation.NegotiationTools.CancelLocation;

/** Listener for monitoring {@link Negotiation negotiations}. */
public interface CancelListener {

  /**
   * Gets called when the negotiation was canceled.
   *
   * @param location the {@linkplain CancelLocation location} where the cancellation occurred
   * @param message additional message containing the reason why this cancellation occurred or
   *     <code>null</code>
   */
  public void canceled(CancelLocation location, String message);
}
