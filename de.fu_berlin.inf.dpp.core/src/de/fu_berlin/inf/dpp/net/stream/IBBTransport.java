package de.fu_berlin.inf.dpp.net.stream;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

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
    public StreamMode getNetTransferMode() {
        return StreamMode.IBB;
    }

    @Override
    public String toString() {
        return "XMPP-IBB-Transport";
    }
}
