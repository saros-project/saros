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

    public void connectionClosed(String connectionID, JID remoteJID,
        IByteStreamConnection connection);

    public void connectionChanged(String connectionID, JID remoteJID,
        IByteStreamConnection connection, boolean incomingRequest);

}