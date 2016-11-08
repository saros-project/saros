package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * Listener interface used by IStreamService and IBytestreamConnection to notify
 * about established or changed connections and incoming XMPP extensions.
 * 
 * @author jurke
 */
public interface IByteStreamConnectionListener {

    /**
     * Gets called when a {@linkplain BinaryXMPPExtension} was received.
     * 
     * @param extension
     */
    public void receive(final BinaryXMPPExtension extension);

    public void connectionClosed(String connectionID, JID remoteJID,
        IByteStreamConnection connection);

    /**
     * Gets called when a connection change is detected. The
     * {@linkplain IByteStreamConnection connection} must be initialized first
     * by calling {@link IByteStreamConnection#initialize()} to be able to
     * receive and send data.
     * 
     * @param connectionID
     *            the id of the connection
     * @param remoteJID
     *            the JID of the remote side this connection is connected to
     * @param connection
     * @param incomingRequest
     *            <code>true</code> if the connection was a result of a remote
     *            connect request, <code>false</code> if the connect request was
     *            initiated on the local side
     */
    public void connectionChanged(String connectionID, JID remoteJID,
        IByteStreamConnection connection, boolean incomingRequest);

}