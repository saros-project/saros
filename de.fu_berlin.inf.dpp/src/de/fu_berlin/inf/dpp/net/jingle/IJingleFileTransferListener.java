package de.fu_berlin.inf.dpp.net.jingle;

import java.io.File;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * this class contains method for jingle file transfer action
 * @author orieger
 *
 */
public interface IJingleFileTransferListener {
	
	/**
	 * 
	 * @param monitor
	 */
	public void incommingFileTransfer(JingleFileTransferProcessMonitor monitor);

}
