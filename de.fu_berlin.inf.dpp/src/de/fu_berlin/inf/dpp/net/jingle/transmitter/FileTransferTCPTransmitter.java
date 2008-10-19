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

public class FileTransferTCPTransmitter extends JingleFileTransferTCPConnection implements IFileTransferTransmitter,
		Runnable {

	private static Logger logger = Logger
			.getLogger(FileTransferTCPTransmitter.class);

	private InetAddress remoteHost;
	private int remotePort;
	public static final int tileWidth = 25;
	private boolean on = true;
	private boolean transmit = false;
	private boolean receive = false;

	/* transfer information */
	private List<JingleFileTransferData> transferList = new Vector<JingleFileTransferData>();
	
	private JingleFileTransferData currentSending = null;

	public FileTransferTCPTransmitter(int localPort, InetAddress remoteHost,
			int remotePort, JingleFileTransferData[] transferData,
			JingleFileTransferProcessMonitor monitor) {

		this.remoteHost = remoteHost;
		this.remotePort = remotePort;

		transmit = true;
	}
	
	public void run() {
		start();
	}

	public void start() {

		Socket socket = null;
		try {

			try {
				// TODO: Socket create methode mit time out einfügen

				/* Übertragung zwischen zwei Partnern. */
				socket = new Socket(remoteHost, remotePort);
			} catch (SocketException se) {
				logger.warn("Second tcp socket initiation.");
				Thread.sleep(1500);
				socket = new Socket(remoteHost, remotePort);
			}
			
			
			
			InputStream input = socket.getInputStream();
			OutputStream output = socket.getOutputStream();

			while (on) {
				
				/**
				 * Time out f�r offen verbindung ohne daten einbauen.
				 */
				if (transmit) {

					/* send file number. */
					transferData(output,input);


				}
				if (receive) {
					/* get number of file to be transfer. */
					// InputStream input = socket.getInputStream();
					int fileNumber = input.read();

					System.out.println("incomming file numbers: " + fileNumber);

					for (int i = 0; i < fileNumber; i++) {
						/* receive file meta data */
						receiveMetaData(input);

						if (receiveTransferData.type == FileTransferType.FILELIST_TRANSFER) {
							receiveFileListData(input);
						}
						// if (receiveTransferData.type ==
						// FileTransferType.RESOURCE_TRANSFER) {
						// /* receive file. */
						// receiveFile(socket);
						// }

					}

					receive = false;
				}
			}
			output.close();
			input.close();
			socket.close();

		} catch (Exception e1) {
			logger.error(e1);
			if (listener != null) {
				listener.exceptionOccured(new JingleSessionException("Error during Jingle file transfer.",getRemoteJID()));
			}
			
			return;
		}
	}

	private JID getRemoteJID(){
		if(currentSending != null){
			return currentSending.recipient;
		}
		else{
			/* error occured on the beginning. try to find out recipient jid
			 * with transfer list. */
			if(transferList != null && transferList.size() > 0){
				return transferList.get(0).recipient;
			}
		}
		return null;
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

	/**
	 * Stops Transmitter
	 */
	public void stop() {
		this.transmit = false;
		this.on = false;
	}

	/**
	 * add new data to transfer.
	 * 
	 * @param transferData
	 *            new data to send
	 */
	public void sendFileData(JingleFileTransferData[] transferData) {
	
		addNewData(transferData);
		transmit = true;
	}
	
	private synchronized void transferData(OutputStream output, InputStream input) throws IOException{
		/* if no jobs in queue. */
		while(transferList.size() == 0){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
				
			logger.debug("send transfer number : "
					+ transferList.size());
			output.write(transferList.size());

		for (JingleFileTransferData data : transferList) {
			
			/* save current packet for error handling*/
			currentSending = data;
			
			/* send file meta data */
			logger.debug("send meta data for : "
					+ data.file_project_path);
			sendMetaData(output, data);

			if (data.type == FileTransferType.FILELIST_TRANSFER) {
				sendFileListData(output,
						data.file_list_content);
				/*
				 * if file list send, we expect remote file list to receive.
				 */
				receive = true;
				transmit = false;
			}
			if (data.type == FileTransferType.RESOURCE_TRANSFER) {
				logger.debug("send file : "
						+ data.file_project_path);
				/* file has been send by meta data object. */
//				sendFile(output, data);
			}
			
		}
		

		/* remove from queue */
		transferList.clear();
		currentSending = null;
		/* set monitor status complete :) */
//		monitor.setComplete(true);
		
		notifyAll();
	}
	
	/**
	 * add new transfer data to transfer queue
	 * 
	 * @param transferData
	 */
	private synchronized void addNewData(JingleFileTransferData[] transferData){
		/* if job in queue. */
		while(transferList.size() > 0){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/* add new jobs. */
		for(JingleFileTransferData d : transferData){
			this.transferList.add(d);
		}
		
		notifyAll();
	}

	public void addJingleFileTransferListener(
			IJingleFileTransferListener listener) {
		this.listener = listener;
	}

	public void removeJingleFileTransferListener(
			IJingleFileTransferListener listener) {
		this.listener = null;

	}
}
