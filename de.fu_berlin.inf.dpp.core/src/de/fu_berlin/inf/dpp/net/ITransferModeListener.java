package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * This interface can be implemented to be informed about file transfers being
 * finished by the DataTransferManager (for instance to collect statistics about
 * incoming file transfers).
 * <p>
 * CAUTION 1: This listener is not informed if a file transfer fails!
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
    public void transferFinished(JID jid, ConnectionMode mode,
        boolean incoming, long sizeTransferred, long sizeUncompressed,
        long transmissionMillisecs);

    /**
     * Gets called when the {@linkplain ConnectionMode transfer mode} changed.
     * 
     * @param jid
     *            the peer for which the transfer mode changed
     * @param mode
     *            the mode currently used for transfer
     */
    public void transferModeChanged(JID jid, ConnectionMode mode);
}
