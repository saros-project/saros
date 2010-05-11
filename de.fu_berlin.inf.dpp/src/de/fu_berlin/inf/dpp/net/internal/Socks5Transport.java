package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.socks5bytestream.Socks5BytestreamManager;

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

public class Socks5Transport extends BytestreamTransport {

    @Override
    public BytestreamManager getManager(XMPPConnection connection) {
        Socks5BytestreamManager socks5ByteStreamManager = Socks5BytestreamManager
            .getBytestreamManager(connection);
        socks5ByteStreamManager.setTargetResponseTimeout(7000);
        SmackConfiguration
            .setLocalSocks5ProxyPort((int) (Math.random() * 300) + 7778);
        return socks5ByteStreamManager;
    }

    @Override
    protected NetTransferMode getNetTransferMode() {
        return NetTransferMode.SOCKS5;
    }
}