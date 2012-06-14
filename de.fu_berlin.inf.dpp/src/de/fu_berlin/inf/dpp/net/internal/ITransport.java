package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.Connection;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;

/**
 * This interface is used to define various transport methods (probably only XEP
 * 65 SOCKS5, XEP 47 in-band bytestream and XEP 16x Jingle
 */
public interface ITransport {

    /**
     * Try to connect to the given user.
     * 
     * @throws IOException
     */
    public IByteStreamConnection connect(JID peer, SubMonitor progress)
        throws IOException, InterruptedException;

    public void prepareXMPPConnection(Connection connection,
        IByteStreamConnectionListener listener);

    public void disposeXMPPConnection();

    public String toString();

    public NetTransferMode getDefaultNetTransferMode();
}