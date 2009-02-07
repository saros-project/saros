package de.fu_berlin.inf.dpp.net.jingle;

import java.io.InputStream;

/**
 * this class contains method for jingle file transfer action
 * 
 * @author orieger
 * 
 */
public interface IJingleFileTransferListener {

    public void incomingData(TransferDescription data, InputStream input);

    public void connected(String protocol, String remote);
}
