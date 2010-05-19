package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.ibb.InBandBytestreamManager;

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

public class IBBTransport extends BytestreamTransport {

    @Override
    public BytestreamManager getManager(XMPPConnection connection) {
        return InBandBytestreamManager.getByteStreamManager(connection);
    }

    @Override
    public NetTransferMode getDefaultNetTransferMode() {
        return NetTransferMode.IBB;
    }

}