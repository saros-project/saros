package de.fu_berlin.inf.dpp.net.jingle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;
import org.limewire.nio.NIODispatcher;
import org.limewire.rudp.DefaultRUDPContext;
import org.limewire.rudp.DefaultRUDPSettings;
import org.limewire.rudp.DefaultUDPService;
import org.limewire.rudp.RudpMessageDispatcher;
import org.limewire.rudp.UDPMultiplexor;
import org.limewire.rudp.UDPSelectorProvider;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.impl.DefaultMessageFactory;

import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferData.FileTransferType;

/**
 * This class implements a file transfer session with jingle.
 * 
 * Jingle is a XMPP-extension with id XEP-0166. Documentation can be found at
 * http://xmpp.org/extensions/xep-0166.html .
 * 
 * This implementation uses TCP as transport protocol which fall back to UDP
 * when a TCP connection failed. To ensure no data loss when transmitting with
 * UDP the RUDP implementation from the Limewire project are used.
 * 
 * Documentation for the RUDP component from limewire can be found at:
 * http://wiki.limewire.org/index.php?title=Javadocs .
 * 
 * @author chjacob
 * 
 */
public class JingleFileTransferSession extends JingleMediaSession {

    private class Receive extends Thread {

	private InputStream input;

	public Receive(InputStream inputStream) {
	    this.input = inputStream;
	}

	public void run() {
	    try {
		while (true) {
		    logger.debug("waiting on port " + local.getPort());

		    /* get number of file to be transfer. */
		    int fileNumber;

		    if (-1 == (fileNumber = input.read())) {
			// end of stream
			logger.error("end of stream received!");
			break;
		    }
		    logger.debug("incoming file number: " + fileNumber);

		    for (int i = 0; i < fileNumber; i++) {

			ObjectInputStream ii = new ObjectInputStream(input);

			/* receive file data */
			JingleFileTransferData data = (JingleFileTransferData) ii
				.readObject();

			if (data.type == FileTransferType.FILELIST_TRANSFER) {
			    logger.debug("received file List");
			    logger.debug(data.file_list_content);
			    /* inform listener. */
			    listener.incomingFileList(data.file_list_content,
				    data.sender);

			} else if (data.type == FileTransferType.RESOURCE_TRANSFER) {
			    logger.debug("received resource "
				    + data.file_project_path);
			    listener.incomingResourceFile(data,
				    new ByteArrayInputStream(data.content));
			}
		    }
		}
	    } catch (IOException e) {
		logger.info("receive-thread interrupted");
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    }

	}
    }

    private static Logger logger = Logger
	    .getLogger(JingleFileTransferSession.class);
    private JingleSession jingleSession;
    private TransportCandidate local;
    private TransportCandidate remote;
    private Receive tcpReceiveThread;
    private Receive udpReceiveThread;
    private JingleFileTransferData[] transferList;
    private JingleFileTransferData currentSending;
    private IJingleFileTransferListener listener;
    private UDPSelectorProvider udpSelectorProvider;
    private Socket udpSocket;
    private Socket tcpSocket;

    /**
     * TODO CJ: write javadoc
     * 
     * @param payloadType
     * @param remote
     * @param local
     * @param mediaLocator
     * @param jingleSession
     * @param transferData
     * @param listener
     */
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
	logger.debug("JingleFileTransferSesseion created " + local.getIp()
		+ ":" + local.getPort() + " <-> " + remote.getIp() + ":"
		+ remote.getPort());
	initialize();
    }

    /**
     * Initialization of the session. It tries to create sockets for both, TCP
     * and UDP. The UDP Socket is a reliable implementation from the Limewire
     * project. Documentation can be found at http://wiki.limewire.org.
     */
    @Override
    public void initialize() {

	try { // to create a tcp socket
	    // server side
	    if (jingleSession.getInitiator().equals(
		    jingleSession.getConnection().getUser())) {
		// create TCP Socket and listen
		ServerSocket serverSocket = new ServerSocket(local.getPort());
		serverSocket.setSoTimeout(1000);
		this.tcpSocket = serverSocket.accept();
	    } else { // client side
		this.tcpSocket = new Socket(remote.getIp(), remote.getPort());
	    }
	    logger.debug("successfully connected with TCP");
	} catch (UnknownHostException e) {
	    logger.debug("Invalid IP-address of jingle remote (TCP)");
	} catch (IOException e) {
	    logger.debug("Failed to connect with TCP");
	}

	try { // to create a udp socket
	    RudpMessageDispatcher dispatcher = new RudpMessageDispatcher();
	    DefaultUDPService service = new DefaultUDPService(dispatcher);
	    RUDPMessageFactory factory = new DefaultMessageFactory();
	    udpSelectorProvider = new UDPSelectorProvider(
		    new DefaultRUDPContext(factory, NIODispatcher.instance()
			    .getTransportListener(), service,
			    new DefaultRUDPSettings()));
	    UDPMultiplexor udpMultiplexor = udpSelectorProvider.openSelector();
	    dispatcher.setUDPMultiplexor(udpMultiplexor);
	    NIODispatcher.instance().registerSelector(udpMultiplexor,
		    udpSelectorProvider.getUDPSocketChannelClass());

	    service.start(local.getPort());

	    // server side
	    if (jingleSession.getInitiator().equals(
		    jingleSession.getConnection().getUser())) {
		Socket usock = udpSelectorProvider.openAcceptorSocketChannel()
			.socket();
		usock.connect(new InetSocketAddress(InetAddress
			.getByName(remote.getIp()), remote.getPort()));
		usock.setSoTimeout(0);
		usock.setKeepAlive(true);
		this.udpSocket = usock;
	    } else { // client side
		Socket usock = udpSelectorProvider.openSocketChannel().socket();
		usock.setSoTimeout(0);
		usock.setKeepAlive(true);
		usock.connect(new InetSocketAddress(InetAddress
			.getByName(remote.getIp()), remote.getPort()));
		this.udpSocket = usock;
	    }
	    logger.debug("successfully connected with UDP");
	} catch (UnknownHostException e1) {
	    logger.debug("Invalid IP-address of jingle remote (UDP)");
	} catch (IOException e1) {
	    logger.debug("Failed to connect with UDP");
	}
	logger.debug("JingleFileTransferSesseion initialized");
    }

    /**
     * This method is called from the JingleFileTransferManager to send files
     * with this session. This method tries to transmit the files with TCP. When
     * this fails it tries to send the files with UDP/RUDP.
     */
    public void sendFiles(JingleFileTransferData[] transferData) {

	this.transferList = transferData;

	if (tcpSocket != null) {
	    logger.debug("sending with TCP to " + remote.getIp() + ":"
		    + remote.getPort());
	    try {
		logger.debug("sending with TCP..");
		transmit(tcpSocket.getOutputStream());
		return;
	    } catch (IOException e) {
		logger.debug("sending with TCP failed, use UDP instead..", e);
	    }
	}
	if (udpSocket != null) {
	    logger.debug("sending with UDP to " + remote.getIp() + ":"
		    + remote.getPort());
	    try {
		logger.debug("sending with UDP..");
		transmit(udpSocket.getOutputStream());
	    } catch (IOException e) {
		logger.debug("sending with UDP failed, use IBB instead..", e);
		// TODO CJ: fallback to IBB
	    }
	}
    }

    /**
     * This method is called from Jingle when a jingle session is established.
     * Two threads are started, one for receiving with TCP, the other for
     * receiving with UDP/RUDP.
     */
    @Override
    public void startReceive() {

	logger.debug("JingleFileTransferSesseion: start receiving");

	if (this.tcpSocket != null) {
	    try { // start TCP Thread
		this.tcpReceiveThread = new Receive(tcpSocket.getInputStream());
		this.tcpReceiveThread.start();
	    } catch (IOException e) {
		logger.error("Error while creating TCP-receiver thread");
	    }
	}

	if (this.udpSocket != null) {
	    try { // start UDP Thread
		this.udpReceiveThread = new Receive(udpSocket.getInputStream());
		this.udpReceiveThread.start();
	    } catch (IOException e) {
		logger.error("Error while creating UDP-receiver thread");
	    }
	}
    }

    /**
     * This method is called from Jingle when a jingle session is established.
     * This method tries to transmit the files with TCP. When this fails it
     * tries to send the files with UDP/RUDP.
     */
    @Override
    public void startTrasmit() {
	if (transferList == null)
	    return;

	logger.debug("JingleFileTransferSesseion: start transmitting");

	if (tcpSocket != null) {
	    try {
		logger.debug("sending with TCP..");
		transmit(tcpSocket.getOutputStream());
		return;
	    } catch (IOException e) {
		logger.debug("sending with TCP failed, use UDP instead..", e);
	    }
	}
	if (udpSocket != null) {
	    try {
		logger.debug("sending with UDP..");
		transmit(udpSocket.getOutputStream());
	    } catch (IOException e) {
		logger.warn("sending with UDP failed, use UDP instead..", e);
		// TODO CJ: fallback to IBB
	    }
	}

    }

    private void transmit(OutputStream output) throws IOException {

	output.write(transferList.length);
	logger.debug("sent transfer number : " + transferList.length);

	for (JingleFileTransferData data : transferList) {

	    /* save current packet for error handling */
	    currentSending = data;

	    /* send data */
	    ObjectOutputStream oo = new ObjectOutputStream(output);
	    oo.writeObject(data);
	    oo.flush();
	    logger.debug("sent data for : " + data.file_project_path);

	}
	transferList = null;
    }

    @Override
    public void stopReceive() {
	logger.debug("JingleFileTransferSesseion: stop receiving");
	if (tcpReceiveThread != null)
	    tcpReceiveThread.interrupt();
	if (udpReceiveThread != null)
	    udpReceiveThread.interrupt();
    }

    @Override
    public void stopTrasmit() {
	logger.debug("JingleFileTransferSesseion: stop transmitting");
	closeSocket(tcpSocket);
	closeSocket(udpSocket);
    }

    private void closeSocket(Socket s) {
	if (s != null)
	    try {
		s.close();
	    } catch (IOException e) {
		logger.warn("Failed to close socket");
	    }
    }

    @Override
    public void setTrasmit(boolean active) {
	logger.debug("JingleFileTransferSesseion: set transmit to " + active);
	// TODO CJ: What have to do here?
    }
}
