package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnection;
import de.fu_berlin.inf.dpp.net.stream.StreamMode;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.io.IOException;

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

  public void addTransferListener(ITransferListener listener);

  public void removeTransferListener(ITransferListener listener);

  /** @deprecated */
  @Deprecated
  public void connect(JID peer) throws IOException;

  public void connect(String connectionID, JID peer) throws IOException;

  /**
   * @deprecated Disconnects {@link IByteStreamConnection} with the specified peer
   * @param peer {@link JID} of the peer to disconnect the {@link IByteStreamConnection}
   */
  @Deprecated
  public boolean closeConnection(JID peer);

  public boolean closeConnection(String connectionIdentifier, JID peer);

  /** @deprecated */
  @Deprecated
  public StreamMode getTransferMode(JID jid);

  public StreamMode getTransferMode(String connectionID, JID jid);
}
