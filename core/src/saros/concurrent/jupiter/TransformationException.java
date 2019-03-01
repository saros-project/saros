package saros.concurrent.jupiter;

/** Exception thrown by algorithm instances whenever transforming operations fails. */
public class TransformationException extends Exception {

  private static final long serialVersionUID = 569529120440589145L;

  /** Creates a new TransformationException. */
  public TransformationException() {
    super();
  }

  /**
   * Creates a new TransformationException.
   *
   * @param message the detail message
   */
  public TransformationException(String message) {
    super(message);
  }

  /**
   * Creates a new TransformationException.
   *
   * @param cause the cause of this exception
   */
  public TransformationException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new TransformationException.
   *
   * @param message the detail message
   * @param cause the cause of this exception
   */
  public TransformationException(String message, Throwable cause) {
    super(message, cause);
  }
}
