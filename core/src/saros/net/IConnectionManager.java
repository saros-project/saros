package saros.net;

import java.io.IOException;
import saros.net.stream.StreamMode;

/**
 * The connection manager is responsible for establishing connections to remote addresses.
 *
 * <p>It offers support for two connection types.
 *
 * <ol>
 *   <li>Establishing a connection using {@link #connect(String, Object)} to establish a connection
 *       which is needed in conjunction with the {@link ITransmitter transmitter} and {@link
 *       IReceiver receiver} in order to send and receive related packages.
 *   <li>Establishing a connection using {@link #connectStream(String, Object)} to establish a
 *       {@link IStreamConnection connection} that can be used for custom purposes. In order to get
 *       notified about such a connection on the remote side you have to install a {@link
 *       IStreamConnectionListener}.
 * </ol>
 *
 * <p><b>Note</b>: Stream connections must be closed by calling {@link IStreamConnection#close()
 * close} on the given connection.
 */
public interface IConnectionManager {

  public static final int IBB_SERVICE = 1;

  public static final int SOCKS5_SERVICE = 2;

  /**
   * Sets the services that should be used to establish direct connections.
   *
   * @param serviceMask bit wise OR mask that contain the service to use, -1 for all available
   *     services or 0 for no service at all
   */
  public void setServices(int serviceMask);

  public void addStreamConnectionListener(final IStreamConnectionListener listener);

  public void removeStreamConnectionListener(final IStreamConnectionListener listener);

  /**
   * Connects to the given address using the given stream ID.
   *
   * @param id the ID of the stream
   * @param address the remote address to connect to
   * @return the stream connection
   * @throws IOException if an I/O error occurs or such a stream already exists
   */
  public IStreamConnection connectStream(String id, Object address) throws IOException;

  /**
   * Connects to the given address using the given ID.
   *
   * @param id
   * @param address
   * @throws IOException if an I/O error occurs
   */
  public void connect(String id, Object address) throws IOException;

  /**
   * Closes the given connection.
   *
   * @param id the ID of the connection
   * @param address the remote address
   * @return <code>true</code> if the connection was closed, <code>false</code> if no such
   *     connection exists
   */
  // TODO rename to close
  public boolean closeConnection(String id, Object address);

  // TODO RENAME
  public StreamMode getTransferMode(String connectionID, Object address);
}
