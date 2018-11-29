package de.fu_berlin.inf.dpp.communication.connection;

import de.fu_berlin.inf.dpp.net.ConnectionState;

/** A listener for changes to the current connection state. */
public interface IConnectionStateListener {

  /**
   * Is fired when the state of the connection changes.
   *
   * @param state the current state of the connection
   * @param error the error that occurred when the state is {@link ConnectionState#ERROR} or <code>
   *     null</code> when the error is not available
   */
  public void connectionStateChanged(ConnectionState state, Exception error);
}
