package de.fu_berlin.inf.dpp.net.jingle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferData.FileTransferType;

public class JingleFileTransferSession extends JingleMediaSession {

    private class ReceiveTCP extends Thread {

	public void run() {

	    ServerSocket serverSocket;
	    try {
		// create TCP Socket and listen
		serverSocket = new ServerSocket(local.getPort());

		serverSocket.setSoTimeout(2000);
		Socket socket = serverSocket.accept();

		while (true) {
		    InputStream input = socket.getInputStream();
		    OutputStream output = socket.getOutputStream();

		    /* get number of file to be transfer. */
		    int fileNumber = input.read();
		    logger.debug("incomming file numbers: " + fileNumber);

		    for (int i = 0; i < fileNumber; i++) {

			ObjectInputStream ii = new ObjectInputStream(input);

			/* receive file data */
			JingleFileTransferData data = (JingleFileTransferData) ii
				.readObject();

			if (data.type == FileTransferType.FILELIST_TRANSFER) {
			    logger.debug("received file List with TCP");
			    logger.debug(data.file_list_content);
			    /* inform listener. */
			    listener.incomingFileList(data.file_list_content,
				    data.sender);

			} else if (data.type == FileTransferType.RESOURCE_TRANSFER) {
			    logger.debug("received resource "
				    + data.file_project_path + " with TCP");
			    listener.incomingResourceFile(data,
				    new ByteArrayInputStream(data.content));
			}
		    }
		    // serverSocket.setSoTimeout(0);
		    // socket = serverSocket.accept();
		}

	    } catch (SocketTimeoutException e) {
		logger.debug("TCP socket timeout");
	    } catch (IOException e) {
		logger.debug(e.getLocalizedMessage()
			+ " while receiving with TCP");
	    } catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    private class ReceiveUDP extends Thread {
	public void run() {
	    try {
		DatagramSocket udpSocket = new DatagramSocket(local.getPort());
		byte[] buffer = new byte[5];
		DatagramPacket p = new DatagramPacket(buffer, buffer.length);
		udpSocket.receive(p);
		logger.debug(new String(p.getData()) + " received with UDP");

	    } catch (SocketException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    private static Logger logger = Logger
	    .getLogger(JingleFileTransferSession.class);
    private JingleSession jingleSession;
    private TransportCandidate local;
    private TransportCandidate remote;
    private ReceiveTCP tcpReceiveThread;
    private ReceiveUDP udpReceiveThread;
    private JingleFileTransferData[] transferList;
    private JingleFileTransferData currentSending;
    private IJingleFileTransferListener listener;
    private Socket socket;

    public JingleFileTransferSession(PayloadType payloadType,
	    TransportCandidate remote, TransportCandidate local,
	    String mediaLocator, JingleSession jingleSession,
	    JingleFileTransferData[] transferData,
	    IJingleFileTransferListener listener) {
	super(payloadType, remote, local, mediaLocator, jingleSession);
	this.jingleSession = jingleSession;
	this.local = local;
	this.remote = remote;
	this.transferList = transferData;
	this.listener = listener;
	logger.debug("JingleFileTransferSesseion created");
	initialize();
    }

    @Override
    public void initialize() {
	logger.debug("JingleFileTransferSesseion initialized");
    }

    @Override
    public void setTrasmit(boolean active) {
	logger
		.debug("JingleFileTransferSesseion activity was set to "
			+ active);
	if (active && socket != null) {
	    logger.debug("sending with TCP..");
	    try {
		transmitTCP(socket);
	    } catch (IOException e) {
		logger.debug(e.getLocalizedMessage());
		logger.debug("sending with TCP failed, use UDP instead");

	    }
	} else {
	    logger.debug("sending with UDP..");
	    transmitUDP();
	}
    }

    @Override
    public void startReceive() {
	if (jingleSession.getInitiator().equals(
		jingleSession.getConnection().getUser()))
	    return;

	logger.debug("JingleFileTransferSesseion: start receiving");

	// start TCP Thread
	this.tcpReceiveThread = new ReceiveTCP();
	this.tcpReceiveThread.start();

	// start UDP Thread
	this.udpReceiveThread = new ReceiveUDP();
	this.udpReceiveThread.start();
    }

    @Override
    public void startTrasmit() {
	if (!jingleSession.getInitiator().equals(
		jingleSession.getConnection().getUser()))
	    return;

	logger.debug("JingleFileTransferSesseion: start transmitting");

	// try to use tcp
	try {
	    logger.debug("trying to send with TCP");
	    this.socket = new Socket(remote.getIp(), remote.getPort());
	    transmitTCP(socket);
	} catch (IOException e) {
	    e.getLocalizedMessage();
	    // TCP don't work, use UDP instead
	    logger.debug("sending with TCP fails, trying to use UDP..");
	    transmitUDP();
	}

    }

    private synchronized void transmitTCP(Socket socket) throws IOException {
	if (transferList == null) {
	    logger.error("TransferList is empty, send nothing..");
	    return;
	}
	InputStream input = socket.getInputStream();
	OutputStream output = socket.getOutputStream();

	logger.debug("send transfer number : " + transferList.length);
	output.write(transferList.length);

	for (JingleFileTransferData data : transferList) {

	    /* save current packet for error handling */
	    currentSending = data;

	    /* send data */
	    logger.debug("send data for : " + data.file_project_path);
	    ObjectOutputStream oo = new ObjectOutputStream(output);
	    oo.writeObject(data);
	    oo.flush();

	}
	transferList = null;

    }

    private void transmitUDP() {
	try {

	    DatagramSocket ds = new DatagramSocket();
	    byte[] outData = "Hello".getBytes();
	    DatagramPacket p = new DatagramPacket(outData, outData.length,
		    InetAddress.getByName(remote.getIp()), remote.getPort());

	} catch (SocketException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    @Override
    public void stopReceive() {
	logger.debug("JingleFileTransferSesseion: stop receiving");
    }

    @Override
    public void stopTrasmit() {
	// TODO Auto-generated method stub
	logger.debug("JingleFileTransferSesseion: stop transmitting");
    }

    public synchronized void setTransferData(
	    JingleFileTransferData[] transferData) {
	this.transferList = transferData;
    }
}
