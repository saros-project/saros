package de.fu_berlin.inf.dpp.net.jingle;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.jingle.IncomingJingleSession;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.mediaimpl.sshare.api.ImageDecoder;
import org.jivesoftware.smackx.jingle.mediaimpl.sshare.api.ImageEncoder;
import org.jivesoftware.smackx.jingle.mediaimpl.sshare.api.ImageReceiver;
import org.jivesoftware.smackx.jingle.mediaimpl.sshare.api.ImageTransmitter;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter.FileTransferData;
import de.fu_berlin.inf.dpp.net.jingle.receiver.FileTransferSocks5Receiver;
import de.fu_berlin.inf.dpp.net.jingle.transmitter.FileTransferSocks5Transmitter;

public class FileTransferSession extends JingleMediaSession {

	private IFileTransferTransmitter transmitter = null;
	private IFileTransferReceiver receiver = null;
	private XMPPConnection connection;

	/* transfer information */
	private JingleFileTransferData[] transferData;
	private JingleFileTransferProcessMonitor monitor;
	private IJingleFileTransferListener listener;

	public FileTransferSession(PayloadType payloadType,
			TransportCandidate remote, TransportCandidate local,
			String mediaLocator, JingleSession jingleSession) {
		super(payloadType, remote, local, mediaLocator, jingleSession);
		initialize();
	}

	public FileTransferSession(PayloadType payloadType,
			TransportCandidate remote, TransportCandidate local,
			String mediaLocator, JingleSession jingleSession,
			JingleFileTransferData[] transferData,
			JingleFileTransferProcessMonitor monitor) {
		super(payloadType, remote, local, mediaLocator, jingleSession);
		
		this.transferData = transferData;
		this.monitor = monitor;
		
		initialize();
	}

	@Override
	public void initialize() {
		if (this.getJingleSession() instanceof IncomingJingleSession) {
			try {

				receiver = new FileTransferSocks5Receiver(InetAddress
						.getByName("0.0.0.0"), getRemote().getPort(),
						getLocal().getPort());
				/* call listener. */
				if (listener != null) {
					listener
							.incommingFileTransfer(((FileTransferSocks5Receiver) receiver)
									.getMonitor());
				}
				System.out.println("Receiving on:" + receiver.getLocalPort());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {

				InetAddress remote = InetAddress.getByName(getRemote().getIp());
				System.out.println("local Port: " + getLocal().getPort());
				transmitter = new FileTransferSocks5Transmitter(getLocal()
						.getPort(), remote, getRemote().getPort(),
						transferData, monitor);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Set transmit activity. If the active is true, the instance should
	 * trasmit. If it is set to false, the instance should pause transmit.
	 * 
	 * @param active
	 *            active state
	 */
	public void setTrasmit(boolean active) {
		transmitter.setTransmit(true);

	}

	/**
	 * For NAT Reasons this method does nothing. Use startTransmit() to start
	 * transmit and receive jmf
	 */
	public void startReceive() {
		// Do nothing.

	}

	/**
	 * Starts transmission and for NAT Traversal reasons start receiving also.
	 */
	public void startTrasmit() {
		new Thread(transmitter).start();
	}

	@Override
	public void stopReceive() {
		if (receiver != null) {
			receiver.stop();
		}
	}

	/**
	 * Stops transmission and for NAT Traversal reasons stop receiving also.
	 */
	public void stopTrasmit() {
		if (transmitter != null) {
			transmitter.stop();
		}
	}

	/**
	 * Obtain a free port we can use.
	 * 
	 * @return A free port number.
	 */
	protected int getFreePort() {
		ServerSocket ss;
		int freePort = 0;

		for (int i = 0; i < 10; i++) {
			freePort = (int) (10000 + Math.round(Math.random() * 10000));
			freePort = freePort % 2 == 0 ? freePort : freePort + 1;
			try {
				ss = new ServerSocket(freePort);
				freePort = ss.getLocalPort();
				ss.close();
				return freePort;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			ss = new ServerSocket(0);
			freePort = ss.getLocalPort();
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return freePort;
	}

	public JingleFileTransferProcessMonitor getProcessMonitor() {
		return this.monitor;
	}

	/*
	 * TODO: 1. Diese beiden Methoden auslagern 
	 * 		2. Listener Liste umsetzen.
	 */
	
	public void addJingleFileTransferListener(
			IJingleFileTransferListener listener) {
		this.listener = listener;
	}
	
	public void removeJingleFileTransferListener(IJingleFileTransferListener listener){
		this.listener = null;
	}

}
