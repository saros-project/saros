package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

import de.fu_berlin.inf.dpp.net.NetTransferMode;

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
    protected BytestreamManager getManager(Connection connection) {
        return InBandBytestreamManager.getByteStreamManager(connection);
    }

    @Override
    public NetTransferMode getTransportMode() {
        return NetTransferMode.IBB;
    }

}
