package saros.exceptions;

/**
 * Signals that the current AWT action was triggered from an illegal context.
 *
 * <p>This exception will be thrown if an AWT action is triggered (synchronously) inside a write
 * safe context.
 *
 * @see saros.intellij.ui.util.SafeDialogUtils
 */
public class IllegalAWTContextException extends Exception {

  /**
   * Constructs a <code>IllegalAWTContextException</code> with the given message.
   *
   * @param message the exception message
   */
  public IllegalAWTContextException(String message) {
    super(message);
  }
}
