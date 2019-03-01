package saros.net.stream;

import java.io.IOException;
import org.jivesoftware.smack.Connection;
import saros.net.internal.IByteStreamConnection;
import saros.net.internal.IByteStreamConnectionListener;
import saros.net.xmpp.JID;

/**
 * This interface is used to define various services (probably only XEP 65 SOCKS5, XEP 47 in-band
 * bytestreams) that offer the possibility to establish network connections/sessions.
 */
public interface IStreamService {

  /** Delimiter that must be used to encode various arguments into a session id. */
  public static final char SESSION_ID_DELIMITER = ':';

  /**
   * Establishes a {@link IByteStreamConnection connection} to the given JID.
   *
   * @param connectionID an ID used to identify this stream(session)
   * @param remoteAddress a <b>resource qualified</b> JID to connect to
   * @throws NullPointerException if connectionID or peer is <code>null</code>
   * @throws IllegalArgumentException if the connection id is an empty string or contains at least
   *     one {@value #SESSION_ID_DELIMITER} character
   * @throws IOException if no connection could be established
   * @throws InterruptedException if the stream establishment was interrupted
   */
  public IByteStreamConnection connect(String connectionID, JID remoteAddress)
      throws IOException, InterruptedException;

  /**
   * Initializes the service. After initialization the service is able to establish connections via
   * {@link #connect}.
   *
   * @param connection
   * @param listener
   */
  public void initialize(Connection connection, IByteStreamConnectionListener listener);

  /**
   * Un-initializes the service. After un-initialization the service is not able to establish
   * connections via {@link #connect}.
   */
  public void uninitialize();
}
