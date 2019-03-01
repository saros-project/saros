package de.fu_berlin.inf.dpp.exceptions;

/** Exception used for signaling that a operation has been canceled */
public class OperationCanceledException extends Exception {

  private static final long serialVersionUID = -8669860051136837906L;

  public OperationCanceledException() {
    super();
  }

  public OperationCanceledException(String message) {
    super(message);
  }

  public OperationCanceledException(Throwable e) {
    super(e);
  }
}
