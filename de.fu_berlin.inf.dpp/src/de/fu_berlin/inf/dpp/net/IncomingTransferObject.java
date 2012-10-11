package de.fu_berlin.inf.dpp.net;

import java.io.IOException;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;

@Component(module = "net")
public interface IncomingTransferObject {

    /**
     * Reads the data from the underlying transfer (for instance a
     * BinaryChannel) and decompresses it if enabled in the TransferDescription
     * 
     * @throws IOException
     *             If there was a technical problem.
     * 
     * @blocking This is a long running operation. So do not call this from the
     *           {@link DispatchThreadContext} or from the SWT Thread.
     */
    public byte[] accept() throws IOException;

    /**
     * Returns the transfer description of this transfer object (which you can
     * get by calling accept).
     */
    public TransferDescription getTransferDescription();

    /**
     * Returns the NetTransferMode using which this Transfer is going to be/has
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

}
