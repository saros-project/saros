package de.fu_berlin.inf.dpp.exceptions;

import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;

/** Exception used for signaling that the local user canceled an operation */
public class LocalCancellationException extends SarosCancellationException {

  private static final long serialVersionUID = 3663315740957551184L;
  protected CancelOption cancelOption;

  /**
   * Standard constructor.
   *
   * <p>If no {@link CancelOption} is specified {@link CancelOption#NOTIFY_PEER} is set.
   */
  public LocalCancellationException() {
    super();
    this.cancelOption = CancelOption.NOTIFY_PEER;
  }

  public LocalCancellationException(String msg, CancelOption cancelOption) {
    super(msg);
    this.cancelOption = cancelOption;
  }

  public CancelOption getCancelOption() {
    return cancelOption;
  }
}
