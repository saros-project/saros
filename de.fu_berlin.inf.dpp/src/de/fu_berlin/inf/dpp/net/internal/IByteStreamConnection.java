package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;

/**
 * A IConnection is responsible for sending data to a particular user
 */
public interface IByteStreamConnection {

    public JID getPeer();

    public void close();

    public boolean isConnected();

    /**
     * If this call returns the data has been send successfully, otherwise
     * an IOException is thrown with the reason why the transfer failed.
     *
     * @param data
     *            The data to be sent.
     * @throws IOException
     *             if the send failed
     * @throws SarosCancellationException
     *             It will be thrown if the user (locally or remotely) has
     *             canceled the transfer.
     * @blocking Send the given data as a blocking operation.
     */
    public void send(TransferDescription data, byte[] content,
        SubMonitor callback) throws IOException, SarosCancellationException;

    public NetTransferMode getMode();
}