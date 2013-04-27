package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.jivesoftware.smack.Connection;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;

/**
 * This interface is used to define various transport methods (probably only XEP
 * 65 SOCKS5, XEP 47 in-band bytestream and XEP 16x Jingle.
 */
public interface ITransport {

    /**
     * Establishes a {@link IByteStreamConnection connection} to the given JID.
     * 
     * @param jid
     *            a <b>resource qualified</b> JID to connect to
     * @throws IOException
     *             if no connection could be established
     * @throws InterruptedException
     *             if the connection establishment was interrupted
     */
    public IByteStreamConnection connect(JID jid) throws IOException,
        InterruptedException;

    /**
     * Initializes the transport. After initialization the transport is able to
     * establish connections via {@link #connect}.
     * 
     * @param connection
     * @param listener
     */
    public void initialize(Connection connection,
        IByteStreamConnectionListener listener);

    /**
     * Un-initializes the transport. After un-initialization the transport is
     * not able to establish connections via {@link #connect}.
     */
    public void uninitialize();

    /**
     * Returns the {@linkplain NetTransferMode transport mode} that the
     * transport is using to establish connections.
     * 
     * @return
     */
    public NetTransferMode getNetTransferMode();
}