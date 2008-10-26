package de.fu_berlin.inf.dpp.net.jingle.receiver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData.FileTransferType;
import de.fu_berlin.inf.dpp.net.jingle.IFileTransferReceiver;
import de.fu_berlin.inf.dpp.net.jingle.IJingleFileTransferListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferProcessMonitor;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferTCPConnection;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;

public class FileTransferTCPReceiver extends JingleFileTransferTCPConnection
	implements IFileTransferReceiver {

    private static Logger logger = Logger
	    .getLogger(FileTransferTCPReceiver.class);

    public static final int tileWidth = 25;
    private InetAddress localHost;
    private final int localPort;
    private final JingleFileTransferProcessMonitor monitor;
    // private IJingleFileTransferListener listener;
    private boolean on = true;
    private boolean receive = false;
    private final InetAddress remoteHost;
    private final int remotePort;

    /* transfer information */
    // private JingleFileTransferData receiveTransferData;
    private ServerSocket serverSocket = null;

    /* transmit transfer data */
    private JingleFileTransferData[] transferData;

    private boolean transmit = false;

    public FileTransferTCPReceiver(final InetAddress remoteHost,
	    final int remotePort, final int localPort) throws IOException {
	// try {

	this.remoteHost = remoteHost;
	this.remotePort = remotePort;
	this.localPort = localPort;

	try {
	    this.serverSocket = new ServerSocket(localPort);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	this.transmit = false;
	this.receive = true;

	/* init process monitor. */
	this.monitor = new JingleFileTransferProcessMonitor();

	new Thread(new Runnable() {

	    public void run() {

		try {
		    final Socket socket = FileTransferTCPReceiver.this.serverSocket
			    .accept();

		    InputStream input = socket.getInputStream();
		    OutputStream output = socket.getOutputStream();

		    while (FileTransferTCPReceiver.this.on) {
			if (FileTransferTCPReceiver.this.receive) {

			    /* get number of file to be transfer. */
			    int fileNumber = input.read();

			    if (fileNumber > 0) {
				FileTransferTCPReceiver.logger
					.debug("file number: " + fileNumber);
			    }

			    for (int i = 0; i < fileNumber; i++) {
				/* receive file meta data */
				FileTransferTCPReceiver.logger
					.debug("receive meta data for");
				receiveMetaData(input);

				if (FileTransferTCPReceiver.this.receiveTransferData.type == FileTransferType.FILELIST_TRANSFER) {
				    receiveFileListData(input);
				    /*
				     * file list receive, in the next step
				     * remote file list have to be send.
				     */
				    FileTransferTCPReceiver.this.receive = false;
				}
				if (FileTransferTCPReceiver.this.receiveTransferData.type == FileTransferType.RESOURCE_TRANSFER) {
				    /* receive file. */
				    FileTransferTCPReceiver.logger
					    .info("File incoming: "
						    + FileTransferTCPReceiver.this.receiveTransferData.file_project_path);
				    /* received by meta data. */
				    // receiveFile(input,output);
				    FileTransferTCPReceiver.this.listener
					    .incomingResourceFile(
						    FileTransferTCPReceiver.this.receiveTransferData,
						    new ByteArrayInputStream(
							    FileTransferTCPReceiver.this.receiveTransferData.content));

				}

			    }

			    FileTransferTCPReceiver.this.monitor
				    .setComplete(true);

			}
			if (FileTransferTCPReceiver.this.transmit) {

			    // if(transferData == null){
			    // throw new
			    // JingleSessionException("No Transfer Data for sending remote filelist.");
			    // }

			    int waitcount = 5;
			    while (FileTransferTCPReceiver.this.transferData == null) {
				/* wait for transferData */
				FileTransferTCPReceiver.logger
					.info("No TransferData available. Try again.");
				Thread.sleep(50);
				waitcount--;
				if ((waitcount < 1)
					&& (FileTransferTCPReceiver.this.transferData == null)) {
				    throw new JingleSessionException(
					    "No Transfer Data for sending remote filelist.");
				}
			    }

			    /* send file number. */
			    output
				    .write(FileTransferTCPReceiver.this.transferData.length);

			    for (JingleFileTransferData element : FileTransferTCPReceiver.this.transferData) {

				/* send file meta data */
				sendMetaData(output, element);

				if (element.type == FileTransferType.FILELIST_TRANSFER) {
				    sendFileListData(output,
					    element.file_list_content);
				    /*
				     * if file list send, we expect remote file
				     * list to receive.
				     */
				    FileTransferTCPReceiver.this.receive = true;
				}
				// if (transferData[i].type ==
				// FileTransferType.RESOURCE_TRANSFER) {
				// sendFile(socket, transferData[i].file);
				// }

			    }

			    /* set monitor status complete :) */
			    FileTransferTCPReceiver.this.monitor
				    .setComplete(true);

			    FileTransferTCPReceiver.this.transmit = false;

			}
		    }
		    output.close();
		    input.close();
		    socket.close();

		}

		catch (SocketException se) {
		    if (FileTransferTCPReceiver.this.listener != null) {
			FileTransferTCPReceiver.this.listener
				.exceptionOccured(new JingleSessionException(se
					.getMessage()));
		    }
		    FileTransferTCPReceiver.logger
			    .error("Socket Exception", se);
		    se.printStackTrace();
		    return;
		} catch (JingleSessionException jse) {
		    if (FileTransferTCPReceiver.this.listener != null) {
			FileTransferTCPReceiver.this.listener
				.exceptionOccured(jse);
		    }
		    FileTransferTCPReceiver.logger.error(
			    "JingleSessionException", jse);
		    return;
		} catch (Exception e1) {
		    if (FileTransferTCPReceiver.this.listener != null) {
			FileTransferTCPReceiver.this.listener
				.exceptionOccured(new JingleSessionException(e1
					.getMessage()));
		    }
		    FileTransferTCPReceiver.logger.error("Exception", e1);
		    return;
		}
	    }
	}).start();

    }

    public void addJingleFileTransferListener(
	    IJingleFileTransferListener listener) {
	this.listener = listener;

    }

    public InetAddress getLocalHost() {
	return this.localHost;
    }

    public int getLocalPort() {
	return this.localPort;
    }

    public JingleFileTransferProcessMonitor getMonitor() {
	return this.monitor;
    }

    public InetAddress getRemoteHost() {
	return this.remoteHost;
    }

    public int getRemotePort() {
	return this.remotePort;
    }

    public void removeJingleFileTransferListener(
	    IJingleFileTransferListener listener) {
	this.listener = null;
    }

    public void sendFileData(JingleFileTransferData[] transferData) {
	this.transmit = true;
	this.transferData = transferData;
    }

    public void stop() {
	this.on = false;
	// socket.close();
    }
}
