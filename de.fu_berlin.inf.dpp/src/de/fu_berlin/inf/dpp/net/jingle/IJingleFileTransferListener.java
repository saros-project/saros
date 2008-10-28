package de.fu_berlin.inf.dpp.net.jingle;

import java.io.InputStream;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;

/**
 * this class contains method for jingle file transfer action
 * 
 * @author orieger
 * 
 */
public interface IJingleFileTransferListener {

    public void incommingFileList(String fileList_content, JID sender);

    public void incomingResourceFile(JingleFileTransferData data,
	    InputStream input);

    public void exceptionOccured(JingleSessionException exception);
}
