package saros.net;

/**
 * Listener interface for accepting remote stream connections.
 *
 * <p>The listener must either accept or reject the connection. If there are multiple listeners
 * installed via {@link IConnectionManager#addStreamConnectionListener(IStreamConnectionListener)}
 * the connection will only be rejected if all listeners reject.
 *
 * <p>Furthermore if a listener accepts the request every outstanding listener will <b>not</b> get
 * notified about this connection establishment.
 */
public interface IStreamConnectionListener {

  /**
   * Gets called when a connection was established.
   *
   * @param connection the established connection
   * @return <code>true</code> to accept the connection or <code>false</code> to reject the
   *     connection.
   */
  public boolean connectionEstablished(IStreamConnection connection);
}
