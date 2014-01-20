package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;

@Component(module = "net")
public interface IncomingTransferObject {

    /**
     * Returns the uncompressed payload of this transfer object.
     * 
     * @Note For performance reason the underlying implementation may choose to
     *       always return the original data and therefore the returned byte
     *       array <b>must not</b> be modified.
     */
    public byte[] getPayload();

    /**
     * Returns the transfer description of this transfer object.
     */
    public TransferDescription getTransferDescription();

    /**
     * Returns the {@link NetTransferMode} that was used to receive the transfer
     * object.
     */
    public NetTransferMode getTransferMode();

    /**
     * Returns the size of the payload in bytes before decompression.
     */
    public long getCompressedSize();

    /**
     * Returns the size of the payload in bytes after decompression.
     */
    public long getUncompressedSize();

    /**
     * Returns the time in milliseconds until the transfer was completed.
     */
    public long getTransferDuration();

}
