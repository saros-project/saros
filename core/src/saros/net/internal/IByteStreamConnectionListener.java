package saros.net.internal;

/**
 * Listener interface used by IStreamService and IBytestreamConnection to notify about established
 * or changed connections and incoming XMPP extensions.
 */
public interface IByteStreamConnectionListener {

  public default void connectionClosed(String connectionID, IByteStreamConnection connection) {
    // NOP;
  }

  /**
   * Gets called when a connection change is detected. The {@linkplain IByteStreamConnection
   * connection} must be initialized first by calling {@link IByteStreamConnection#initialize()} to
   * be able to receive and send data.
   *
   * @param connectionID the id of the connection
   * @param connection
   * @param incomingRequest <code>true</code> if the connection was a result of a remote connect
   *     request, <code>false</code> if the connect request was initiated on the local side
   */
  public default void connectionChanged(
      String connectionID, IByteStreamConnection connection, boolean incomingRequest) {
    // NOP;
  }
}
