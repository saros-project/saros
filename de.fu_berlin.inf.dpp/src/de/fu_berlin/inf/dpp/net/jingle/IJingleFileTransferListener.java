package de.fu_berlin.inf.dpp.net.jingle;

import java.io.InputStream;

import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

/**
 * Callback to be informed when a file arrived via Jingle
 */
public interface IJingleFileTransferListener {

    public void incomingData(TransferDescription data, InputStream input,
        NetTransferMode mode);
}
