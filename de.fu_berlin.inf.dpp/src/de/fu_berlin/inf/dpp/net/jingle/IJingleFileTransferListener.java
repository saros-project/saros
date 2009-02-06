package de.fu_berlin.inf.dpp.net.jingle;

import java.io.InputStream;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * this class contains method for jingle file transfer action
 * 
 * @author orieger
 * 
 */
public interface IJingleFileTransferListener {

    public void incomingFileList(String fileList_content, JID sender);

    public void incomingResourceFile(JingleFileTransferData data,
        InputStream input);

    public void connected(String protocol, String remote);
}
