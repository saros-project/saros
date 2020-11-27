package saros.net.xmpp;

import org.jivesoftware.smack.Connection;
import saros.net.ConnectionState;

/**
 * A listener for changes to the current connection state. Use {@link
 * XMPPConnectionService#addListener(IConnectionListener)} to attach it.
 */
public interface IConnectionListener {

  /**
   * Is fired when the state of the connection changes.
   *
   * @param connection The affected XMPP-connection that changed its state
   * @param state the new state of the connection. If the state is <code>ERROR</code>, you can use
   *     {@link XMPPConnectionService#getConnectionError()} to get the error message.
   */
  public void connectionStateChanged(Connection connection, ConnectionState state);
}
