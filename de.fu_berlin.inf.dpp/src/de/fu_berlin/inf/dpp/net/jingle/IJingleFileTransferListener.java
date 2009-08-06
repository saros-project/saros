package de.fu_berlin.inf.dpp.net.jingle;

import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

/**
 * Callback to be informed when a file arrived via Jingle
 */
public interface IJingleFileTransferListener {

    public void incomingData(TransferDescription data, NetTransferMode mode,
        byte[] input, long size, long transferDuration);

    public void transferFailed(TransferDescription data,
        NetTransferMode connectionType, Exception e);

    public void incomingDescription(TransferDescription data,
        NetTransferMode connectionType);
}
