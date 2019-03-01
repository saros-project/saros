package de.fu_berlin.inf.dpp.negotiation;

public class NegotiationTools {
  public enum CancelOption {
    /**
     * Use this option if the peer should be notified that the invitation has been canceled. He gets
     * a message with the cancellation reason.
     */
    NOTIFY_PEER,
    /** Use this option if the peer should not be notified that the invitation has been canceled. */
    DO_NOT_NOTIFY_PEER;
  }

  public enum CancelLocation {
    /** Use this option if the invitation has been canceled by the local user. */
    LOCAL,
    /** Use this option if the invitation has been canceled by the remote user. */
    REMOTE;
  }
}
