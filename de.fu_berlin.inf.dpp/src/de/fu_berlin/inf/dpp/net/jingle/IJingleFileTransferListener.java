package de.fu_berlin.inf.dpp.net.jingle;

import java.io.InputStream;

import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

/**
 * this class contains method for jingle file transfer action
 * 
 * @author orieger
 * 
 */
public interface IJingleFileTransferListener {

    public void incomingData(TransferDescription data, InputStream input,
        NetTransferMode mode);

    public void connected(String protocol, String remote);
}
