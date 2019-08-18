package saros.net;

import java.io.IOException;
import saros.net.internal.IConnection;
import saros.net.stream.StreamMode;
import saros.net.xmpp.JID;

// TODO Javadoc

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

  public IStreamConnection connectStream(String id, Object address) throws IOException;
  /** @deprecated */
  @Deprecated
  public IConnection connect(Object address) throws IOException;

  public IConnection connect(String connectionID, Object address) throws IOException;

  /**
   * @deprecated Disconnects with the specified address
   * @param address
   */
  @Deprecated
  public boolean closeConnection(Object address);

  public boolean closeConnection(String connectionIdentifier, Object address);

  /** @deprecated */
  @Deprecated
  public StreamMode getTransferMode(JID jid);

  public StreamMode getTransferMode(String connectionID, Object address);
}
