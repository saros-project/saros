package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.ibb.InBandBytestreamManager;

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

/**
 * Transport class for in-band bytestreams
 * 
 * @author jurke
 */
public class IBBTransport extends BytestreamTransport {

    private static IBBTransport instance = null;

    private IBBTransport() {
        //
    }

    public synchronized static IBBTransport getTransport() {
        if (instance == null)
            instance = new IBBTransport();
        return instance;
    }

    @Override
    protected BytestreamManager getManager(XMPPConnection connection) {
        return InBandBytestreamManager.getByteStreamManager(connection);
    }

    @Override
    public NetTransferMode getDefaultNetTransferMode() {
        return NetTransferMode.IBB;
    }

}