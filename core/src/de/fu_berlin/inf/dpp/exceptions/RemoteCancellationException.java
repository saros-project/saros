package de.fu_berlin.inf.dpp.exceptions;

/** Exception used for signaling that the user canceled an operation */
public class RemoteCancellationException extends SarosCancellationException {

  private static final long serialVersionUID = 3663315740957551184L;

  public RemoteCancellationException() {
    super();
  }

  public RemoteCancellationException(String msg) {
    super(msg);
  }
}
