package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.extensions.XStreamExtensionProvider;

@Component(module = "net")
public interface IncomingTransferObject {

    /**
     * Returns the payload of this transfer object.
     * 
     */
    public byte[] getPayload();

    /**
     * Returns the transfer description of this transfer object.
     */
    public TransferDescription getTransferDescription();

    /**
     * Returns the NetTransferMode using which this transfer is going to be/has
     * been received.
     */
    public NetTransferMode getTransferMode();

    public static class IncomingTransferObjectExtensionProvider extends
        XStreamExtensionProvider<IncomingTransferObject> {
        public IncomingTransferObjectExtensionProvider() {
            super("incomingTransferObject");
        }
    }

    /**
     * Returns the size of the transferred data in bytes.
     */
    public long getTransferredSize();

    /**
     * Returns the size of the data in bytes after decompression.
     */
    public long getUncompressedSize();

    /**
     * Returns the time in milliseconds until the transfer was completed.
     */
    public long getTransferDuration();

}
