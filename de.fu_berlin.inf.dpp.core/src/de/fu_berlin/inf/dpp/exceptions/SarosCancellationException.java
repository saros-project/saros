package de.fu_berlin.inf.dpp.exceptions;

/** Exception used for signaling that a user (local or remote) canceled an operation */
public class SarosCancellationException extends Exception {

  private static final long serialVersionUID = 7943149705523090956L;

  public SarosCancellationException() {
    super();
  }

  public SarosCancellationException(String msg) {
    super(msg);
  }
}
