package de.fu_berlin.inf.dpp.net.jingle;

import de.fu_berlin.inf.dpp.net.IncomingTransferObject;

/**
 * Callback to be informed when a file arrived via Jingle
 */
public interface IJingleFileTransferListener {
    public void incomingData(IncomingTransferObject incoming);
}
