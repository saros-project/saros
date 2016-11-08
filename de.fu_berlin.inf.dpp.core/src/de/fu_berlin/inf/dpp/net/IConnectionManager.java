package de.fu_berlin.inf.dpp.net;

import java.io.IOException;

import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnection;
import de.fu_berlin.inf.dpp.net.stream.ConnectionMode;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

// TODO Javadoc

public interface IConnectionManager {

    public static final int IBB_TRANSPORT = 1;

    public static final int SOCKS5_TRANSPORT = 2;

    /**
     * Sets the transport that should be used to establish direct connections.
     * 
     * @param transportMask
     *            bit wise OR mask that contain the transport to use, -1 for all
     *            available transports or 0 for no transport at all
     */
    public void setTransport(int transportMask);

    public void addTransferListener(ITransferListener listener);

    public void removeTransferListener(ITransferListener listener);

    /**
     * @deprecated
     */
    @Deprecated
    public void connect(JID peer) throws IOException;

    public void connect(String connectionID, JID peer) throws IOException;

    /**
     * @deprecated Disconnects {@link IByteStreamConnection} with the specified
     *             peer
     * 
     * @param peer
     *            {@link JID} of the peer to disconnect the
     *            {@link IByteStreamConnection}
     */
    @Deprecated
    public boolean closeConnection(JID peer);

    public boolean closeConnection(String connectionIdentifier, JID peer);

    /**
     * @deprecated
     */
    @Deprecated
    public ConnectionMode getTransferMode(JID jid);

    public ConnectionMode getTransferMode(String connectionID, JID jid);

}