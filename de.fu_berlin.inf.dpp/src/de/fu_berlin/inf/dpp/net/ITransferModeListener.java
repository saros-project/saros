package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

/**
 * This interface can be implemented to be informed about file transfers being
 * finished by the DataTransferManager (for instance to collect statistics about
 * incoming file transfers).
 * 
 * CAUTION 1: This listener is not informed if a file transfer fails!
 * 
 * CAUTION 2: transmissionMillisecs includes unpacking/de-compressing of data.
 */
public interface ITransferModeListener {

    /**
     * Informs a listener of a finished incoming or outgoing data transfer with
     * the given user.
     * 
     * @param jid
     *            The JID of the user to which data was sent (outgoing) or from
     *            which data was received (incoming)
     * 
     * @param sizeTransferred
     *            The size of the transferred data in byte
     * @param sizeUncompressed
     *            The size of the received data after (potentially)
     *            decompression in byte
     */
    public void transferFinished(JID jid, NetTransferMode newMode,
        boolean incoming, long sizeTransferred, long sizeUncompressed,
        long transmissionMillisecs);

    /**
     * Method called when the DataTransfer connection changed.
     * 
     * @param jid
     *            the peer for which the connection changed
     * @param connection
     *            maybenull if the connection was closed
     */
    public void connectionChanged(JID jid, IBytestreamConnection connection);

    /**
     * Is called when the transferMode information is reset (probably because
     * the connection changed)
     */
    public void clear();

}
