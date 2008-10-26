package de.fu_berlin.inf.dpp.net.jingle.transmitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData.FileTransferType;
import de.fu_berlin.inf.dpp.net.jingle.IFileTransferTransmitter;
import de.fu_berlin.inf.dpp.net.jingle.IJingleFileTransferListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferProcessMonitor;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferTCPConnection;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;

public class FileTransferTCPTransmitter extends JingleFileTransferTCPConnection
	implements IFileTransferTransmitter, Runnable {

    private static Logger logger = Logger
	    .getLogger(FileTransferTCPTransmitter.class);

    public static final int tileWidth = 25;
    private JingleFileTransferData currentSending = null;
    private boolean on = true;
    private boolean receive = false;
    private final InetAddress remoteHost;
    private final int remotePort;

    /* transfer information */
    private final List<JingleFileTransferData> transferList = new Vector<JingleFileTransferData>();

    private boolean transmit = false;

    public FileTransferTCPTransmitter(int localPort, InetAddress remoteHost,
	    int remotePort, JingleFileTransferData[] transferData,
	    JingleFileTransferProcessMonitor monitor) {

	this.remoteHost = remoteHost;
	this.remotePort = remotePort;

	this.transmit = true;
    }

    public void addJingleFileTransferListener(
	    IJingleFileTransferListener listener) {
	this.listener = listener;
    }

    /**
     * add new transfer data to transfer queue
     * 
     * @param transferData
     */
    private synchronized void addNewData(JingleFileTransferData[] transferData) {
	/* if job in queue. */
	while (this.transferList.size() > 0) {
	    try {
		wait();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

	/* add new jobs. */
	for (JingleFileTransferData d : transferData) {
	    this.transferList.add(d);
	}

	notifyAll();
    }

    private JID getRemoteJID() {
	if (this.currentSending != null) {
	    return this.currentSending.recipient;
	} else {
	    /*
	     * error occured on the beginning. try to find out recipient jid
	     * with transfer list.
	     */
	    if ((this.transferList != null) && (this.transferList.size() > 0)) {
		return this.transferList.get(0).recipient;
	    }
	}
	return null;
    }

    public void removeJingleFileTransferListener(
	    IJingleFileTransferListener listener) {
	this.listener = null;

    }

    public void run() {
	start();
    }

    /**
     * add new data to transfer.
     * 
     * @param transferData
     *            new data to send
     */
    public void sendFileData(JingleFileTransferData[] transferData) {

	addNewData(transferData);
	this.transmit = true;
    }

    /**
     * Set Transmit Enabled/Disabled
     * 
     * @param transmit
     *            boolean Enabled/Disabled
     */
    public synchronized void setTransmit(boolean transmit) {
	this.transmit = transmit;
    }

    public void start() {

	Socket socket = null;
	try {

	    try {
		// TODO: Socket create methode mit time out einfügen

		/* Übertragung zwischen zwei Partnern. */
		socket = new Socket(this.remoteHost, this.remotePort);
	    } catch (SocketException se) {
		FileTransferTCPTransmitter.logger
			.warn("Second tcp socket initiation.");
		Thread.sleep(1500);
		socket = new Socket(this.remoteHost, this.remotePort);
	    }

	    InputStream input = socket.getInputStream();
	    OutputStream output = socket.getOutputStream();

	    while (this.on) {

		/**
		 * Time out f�r offen verbindung ohne daten einbauen.
		 */
		if (this.transmit) {

		    /* send file number. */
		    transferData(output, input);

		}
		if (this.receive) {
		    /* get number of file to be transfer. */
		    // InputStream input = socket.getInputStream();
		    int fileNumber = input.read();

		    System.out.println("incomming file numbers: " + fileNumber);

		    for (int i = 0; i < fileNumber; i++) {
			/* receive file meta data */
			receiveMetaData(input);

			if (this.receiveTransferData.type == FileTransferType.FILELIST_TRANSFER) {
			    receiveFileListData(input);
			}
			// if (receiveTransferData.type ==
			// FileTransferType.RESOURCE_TRANSFER) {
			// /* receive file. */
			// receiveFile(socket);
			// }

		    }

		    this.receive = false;
		}
	    }
	    output.close();
	    input.close();
	    socket.close();

	} catch (Exception e1) {
	    FileTransferTCPTransmitter.logger.error(e1);
	    if (this.listener != null) {
		this.listener.exceptionOccured(new JingleSessionException(
			"Error during Jingle file transfer.", getRemoteJID()));
	    }

	    return;
	}
    }

    /**
     * Stops Transmitter
     */
    public void stop() {
	this.transmit = false;
	this.on = false;
    }

    private synchronized void transferData(OutputStream output,
	    InputStream input) throws IOException {
	/* if no jobs in queue. */
	while (this.transferList.size() == 0) {
	    try {
		wait();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

	FileTransferTCPTransmitter.logger.debug("send transfer number : "
		+ this.transferList.size());
	output.write(this.transferList.size());

	for (JingleFileTransferData data : this.transferList) {

	    /* save current packet for error handling */
	    this.currentSending = data;

	    /* send file meta data */
	    FileTransferTCPTransmitter.logger.debug("send meta data for : "
		    + data.file_project_path);
	    sendMetaData(output, data);

	    if (data.type == FileTransferType.FILELIST_TRANSFER) {
		sendFileListData(output, data.file_list_content);
		/*
		 * if file list send, we expect remote file list to receive.
		 */
		this.receive = true;
		this.transmit = false;
	    }
	    if (data.type == FileTransferType.RESOURCE_TRANSFER) {
		FileTransferTCPTransmitter.logger.debug("send file : "
			+ data.file_project_path);
		/* file has been send by meta data object. */
		// sendFile(output, data);
	    }

	}

	/* remove from queue */
	this.transferList.clear();
	this.currentSending = null;
	/* set monitor status complete :) */
	// monitor.setComplete(true);
	notifyAll();
    }
}
