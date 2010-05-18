/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;

public interface IBytestreamConnectionListener {

    public void addIncomingTransferObject(
        final IncomingTransferObject transferObjectDescription);

    public void connectionClosed(JID peer, IBytestreamConnection connection2);

}