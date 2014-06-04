package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

import de.fu_berlin.inf.dpp.net.ConnectionMode;

/**
 * Transport class for in-band bytestreams
 * 
 * @author jurke
 */
public class IBBTransport extends ByteStreamTransport {

    public IBBTransport() {
        //
    }

    @Override
    protected BytestreamManager createManager(Connection connection) {
        return InBandBytestreamManager.getByteStreamManager(connection);
    }

    @Override
    public ConnectionMode getNetTransferMode() {
        return ConnectionMode.IBB;
    }

    @Override
    public String toString() {
        return "XMPP-IBB-Transport";
    }
}
