package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Listener interface used by ITransport and IBytestreamConnection to notify
 * about established or changed connections and incoming objects.
 * 
 * @author jurke
 */
public interface IByteStreamConnectionListener {

    public void addIncomingTransferObject(
        final IncomingTransferObject incomingTransferObject);

    public void connectionClosed(JID peer, IByteStreamConnection connection);

    public void connectionChanged(JID peer, IByteStreamConnection connection,
        boolean incomingRequest);

}