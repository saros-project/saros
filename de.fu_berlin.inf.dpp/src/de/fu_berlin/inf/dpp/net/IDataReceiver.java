package de.fu_berlin.inf.dpp.net;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.internal.TransferDescription;

/**
 * Implementors don't need to (but may) close the InputStream if they consumed
 * the stream. But: Implementors are expected to have consumed all data from the
 * stream when returning, because the caller will close or reuse the stream once
 * the receiver returns.
 */
public interface IDataReceiver {

    /**
     * @return true if the input stream has been consumed by the receiver
     */
    boolean receivedArchive(TransferDescription data, InputStream input);

    /**
     * @return true if the input stream has been consumed by the receiver
     */
    boolean receivedResource(JID from, IPath path, InputStream input);

    /**
     * @return true if the input stream has been consumed by the receiver
     */
    boolean receivedFileList(TransferDescription data, InputStream input);

    /**
     * @return true if the input stream has been consumed by the receiver
     */
    boolean receiveActivity(JID sender, InputStream input);
}
