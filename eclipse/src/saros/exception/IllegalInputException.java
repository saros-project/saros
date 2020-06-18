package saros.exception;

/**
 * Signals that the user input was not valid.
 *
 * <p>The reason why the input is not valid is passed as the exception message.
 */
public class IllegalInputException extends Exception {
  public IllegalInputException(String message) {
    super(message);
  }
}
