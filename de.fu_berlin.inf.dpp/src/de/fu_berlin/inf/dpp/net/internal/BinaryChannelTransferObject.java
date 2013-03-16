package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.NetTransferMode;

public class BinaryChannelTransferObject implements IncomingTransferObject {

    private TransferDescription transferDescription;

    private int chunkCount;
    private long transferredSize;
    private long uncompressedSize;
    private byte[] payload;
    private long tranferDuration;
    private NetTransferMode transferMode;

    public BinaryChannelTransferObject(NetTransferMode transferMode,
        TransferDescription transferDescription, int chunkCount) {
        this.transferMode = transferMode;
        this.transferDescription = transferDescription;
        this.chunkCount = chunkCount;
        transferredSize = 0;
        uncompressedSize = 0;
        tranferDuration = System.currentTimeMillis();
    }

    @Override
    public byte[] getPayload() throws IOException {
        return payload;
    }

    @Override
    public TransferDescription getTransferDescription() {
        return transferDescription;
    }

    @Override
    public NetTransferMode getTransferMode() {
        return transferMode;
    }

    @Override
    public long getTransferredSize() {
        return transferredSize;
    }

    @Override
    public long getUncompressedSize() {
        return uncompressedSize;
    }

    @Override
    public long getTransferDuration() {
        return tranferDuration;
    }

    /**
     * Sets the payload for this transfer object.
     * 
     * @param originalSize
     *            the original size of the payload (received data)
     * @param data
     *            the payload data
     * 
     * @throws IllegalStateException
     *             if there are still missing chunks, see also
     *             {@link #isLastChunk}
     */
    void setPayload(long originalSize, byte[] data) {

        if (chunkCount > 0)
            throw new IllegalStateException("there are chunks missing: "
                + chunkCount + " > 0");

        tranferDuration = System.currentTimeMillis() - tranferDuration;
        payload = data;
        transferredSize = originalSize;
        uncompressedSize = data.length;
    }

    /**
     * Checks if all outstanding chunks have arrived. This method <b>must</b> be
     * called after a chunk has been received.
     * 
     * @return <code>true</code> if {@link #setPayload} can now be called,
     *         <code>false</code> otherwise
     */
    boolean isLastChunk() {
        return --chunkCount <= 0;
    }
}