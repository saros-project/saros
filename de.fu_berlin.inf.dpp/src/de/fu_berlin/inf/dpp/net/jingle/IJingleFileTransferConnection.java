package de.fu_berlin.inf.dpp.net.jingle;

import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;

public interface IJingleFileTransferConnection {

    /**
     * send data with exist connection.
     * 
     * @param transferData
     */
    public void sendFileData(JingleFileTransferData[] transferData);

    public void addJingleFileTransferListener(
	    IJingleFileTransferListener listener);

    public void removeJingleFileTransferListener(
	    IJingleFileTransferListener listener);
}
