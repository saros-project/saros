package saros.communication.connection;

import saros.net.ConnectionState;

/** A listener for changes to the current connection state. */
@FunctionalInterface
public interface IConnectionStateListener {

  /** List of possible error types. TODO add cases for connect failures */
  enum ErrorType {
    /** A previously available connection was unexpectedly disconnected */
    CONNECTION_LOST,
    /** Another client logged on with the same resource */
    RESOURCE_CONFLICT
  }

  /**
   * Is fired when the state of the connection changes.
   *
   * @param state the current state of the connection
   * @param errorType the error that occurred when the state is {@link ConnectionState#ERROR} or
   *     <code>
   *     null</code> when the error is not available
   */
  public void connectionStateChanged(ConnectionState state, ErrorType errorType);
}
