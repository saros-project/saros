package de.fu_berlin.inf.dpp.net.jingle.receiver;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IBBTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.Socks5TransferNegotiatorManager;

import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData.FileTransferType;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter.FileTransferData;
import de.fu_berlin.inf.dpp.net.jingle.IFileTransferReceiver;
import de.fu_berlin.inf.dpp.net.jingle.IJingleFileTransferListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferProcessMonitor;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;

public class FileTransferTCPReceiver implements IFileTransferReceiver {

	private static Logger logger = Logger
			.getLogger(FileTransferTCPReceiver.class);

	private InetAddress localHost;
	private InetAddress remoteHost;
	private int localPort;
	private int remotePort;
	public static final int tileWidth = 25;
	private boolean on = true;
	private boolean transmit = false;
	private boolean receive = false;

	
	/* transfer information */
	private JingleFileTransferData receiveTransferData;

	/* transmit transfer data */
	private JingleFileTransferData[] transferData;

	private ServerSocket serverSocket = null;

	private final JingleFileTransferProcessMonitor monitor;
	private IJingleFileTransferListener listener;

	public FileTransferTCPReceiver(final InetAddress remoteHost,
			final int remotePort, final int localPort) throws IOException {
		// try {

		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;

		try {
			serverSocket = new ServerSocket(localPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		transmit = false;
		receive = true;

		/* init process monitor. */
		monitor = new JingleFileTransferProcessMonitor();

		new Thread(new Runnable() {

			public void run() {

				try {
					final Socket socket = serverSocket.accept();

					InputStream input = socket.getInputStream();
					OutputStream output = socket.getOutputStream();
					
					while (on) {
						if (receive) {

							/* get number of file to be transfer. */
//							InputStream input = socket.getInputStream();
							int fileNumber = input.read();

							if(fileNumber > 0){
								logger.debug("file number: "+fileNumber);
							}
//							System.out.println("incomming file numbers: "
//									+ fileNumber);

							for (int i = 0; i < fileNumber; i++) {
								/* receive file meta data */
								logger.debug("receive meta data for");
								receiveMetaData(input);

								if (receiveTransferData.type == FileTransferType.FILELIST_TRANSFER) {
									receiveFileListData(input);
									/* file list receive, in the next step remote file list have to be send. */
									receive = false;
								}
								if (receiveTransferData.type == FileTransferType.RESOURCE_TRANSFER) {
									/* receive file. */
									logger.info("File incoming: "+receiveTransferData.file_project_path);
									receiveFile(input);
//									logger.info("File incoming: "+receiveTransferData.file_project_path);
//									listener.incomingResourceFile(receiveTransferData, input);
									
								}

							}
//							input.close();

							monitor.setComplete(true);
							
						}
						if (transmit) {
							/* send file number. */
//							OutputStream os = socket.getOutputStream();
							output.write(transferData.length);

							for (int i = 0; i < transferData.length; i++) {

								/* testing. only */
								// sendFile(socket,
								// "/home/troll/Saros_DPP_1.0.2.jar");
								/* send file meta data */
								sendMetaData(output, transferData[i]);

								if (transferData[i].type == FileTransferType.FILELIST_TRANSFER) {
									sendFileListData(output,
											transferData[i].file_list_content);
									/*
									 * if file list send, we expect remote file
									 * list to receive.
									 */
									receive = true;
								}
								// if (transferData[i].type ==
								// FileTransferType.RESOURCE_TRANSFER) {
								// sendFile(socket, transferData[i].file);
								// }

							}

							/* set monitor status complete :) */
							monitor.setComplete(true);

							// Thread.sleep(2000);
							transmit = false;
//							os.close();

						}
//						Thread.sleep(100);
					}
					output.close();
					input.close();
					socket.close();

				}

				catch (SocketException se) {
					if (listener != null) {
						listener.exceptionOccured(new JingleSessionException(se
								.getMessage()));
					}
					se.printStackTrace();
					return;
				} catch (Exception e1) {
					if (listener != null) {
						listener.exceptionOccured(new JingleSessionException(e1
								.getMessage()));
					}
					e1.printStackTrace();
					return;
				}
			}
		}).start();

		System.out.println("receiver started.");
	}

	private void sendFileListData(OutputStream output, String file_list_content)
			throws IOException {
		ObjectOutputStream oo = new ObjectOutputStream(output);

		oo.writeObject(file_list_content);
		oo.flush();
	}

	private void sendMetaData(OutputStream output, JingleFileTransferData data)
			throws IOException {
		ObjectOutputStream oo = new ObjectOutputStream(output);
		// ObjectInputStream ii = new ObjectInputStream(socket
		// .getInputStream());

		oo.writeObject(data);
		oo.flush();
	}

	private void receiveFileListData(InputStream input) throws IOException,
			ClassNotFoundException {
		logger.debug("receive file List");
		ObjectInputStream ii = new ObjectInputStream(input);

		String fileListData = (String) ii.readObject();

		/* inform listener. */
		listener.incommingFileList(fileListData, receiveTransferData.sender);

		// System.out.println("File List Data : " + fileListData.toString());
	}

	private void receiveMetaData(InputStream input) throws IOException,
			ClassNotFoundException {
		// ObjectOutputStream oo = new ObjectOutputStream(
		// socket.getOutputStream());
		ObjectInputStream ii = new ObjectInputStream(input);

		JingleFileTransferData meta = (JingleFileTransferData) ii.readObject();
		this.receiveTransferData = meta;

		// ii.close();

	}

	private void receiveFile(InputStream input) throws IOException {
//		InputStream input = socket.getInputStream();

		/* on the first receive data into stream. */
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 byte[] buffer = new byte[1024];
		 int length = 0;
		 long filesize = receiveTransferData.filesize;
		 long currentSize = 0;
		 int readSize = 1024;
		 /* zuvor informationen schicken, wie groß die Datei ist.*/
		 while(currentSize < filesize){
			 
			 /* check end of file*/
			 if((currentSize + readSize)>= filesize){
				readSize = (int)(filesize - currentSize); 
			 }
			 
			 if((length = input.read(buffer, 0, readSize))!= 0){
				 bos.write(buffer, 0, readSize);
				 currentSize += readSize;
			 }
//		 System.out.println(new String(buffer,0,length));
		 }
		
		 /* inform listener */
		listener.incomingResourceFile(receiveTransferData, new ByteArrayInputStream(bos.toByteArray()));

		//		byte[] buffer = new byte[1024];
//		// System.out.println("Binded");
//
//		
//		
//		// FileOutputStream fos = new
//		// FileOutputStream(transferData.path.toFile());
//		FileOutputStream fos = new FileOutputStream(new File(
//				receiveTransferData.filePath));
//		while (input.read(buffer, 0, 1024) != -1) {
//			fos.write(buffer);
//			fos.flush();
//		}
//
//		fos.close();
		// input.close();
		// socket.close();

	}

	/**
	 * Return given file as byte array representation.
	 */
	private static byte[] readFile(File file) throws Exception {
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[256];
		for (int len = fileInputStream.read(buffer); len > 0; len = fileInputStream
				.read(buffer)) {
			byteArrayOutputStream.write(buffer, 0, len);
		}
		fileInputStream.close();
		// System.out.println(new String(byteArrayOutputStream.toByteArray(),
		// "UTF-8"));
		return byteArrayOutputStream.toByteArray();
	}
	
	@Deprecated
	private void receiveString(Socket socket) throws IOException,
			ClassNotFoundException {
		ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream ii = new ObjectInputStream(socket.getInputStream());

		String s1 = (String) ii.readObject();

		oo.writeObject("Result " + s1);
		oo.flush();
		ii.close();
		oo.close();
	}

	@Deprecated
	private void receiveInt(Socket socket) throws IOException {
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();

		int zahl1 = input.read();
		int zahl2 = input.read();

		output.write(zahl1 + zahl2);
		output.flush();
		input.close();
		output.close();
	}

	@Deprecated
	private void startByteReceiver() {
		// /* Übertragung zwischen zwei Partnern. */
		// final Socket socket = serverSocket.accept();
		// // System.out.println("Angemeldet: ");
		//		
		// System.out.println("Incomming Message: ");
		// ByteArrayInputStream input = (ByteArrayInputStream)
		// socket.getInputStream();
		// byte[] buffer = new byte[256];
		// int length = 0;
		// /* zuvor informationen schicken, wie groß die Datei ist.*/
		// while((length = input.read(buffer, 0, 256))!= 0){
		// System.out.println(new String(buffer,0,length));
		// }
		// on = false;
		//		
		// Thread.sleep(2000);
		// transmit = false;
		// socket.close();
	}

	@Deprecated
	private void printContent(byte[] data) throws Exception {

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				data);
		byte[] buffer = new byte[4000];
		int len = byteArrayInputStream.read(buffer, 0, 4000);
		String str = new String(buffer, 0, len);

		System.out.println(str);
	}

	/**
	 * Return given file as byte array representation.
	 */
	private static void writeFile(byte[] data) throws Exception {

		/* testing area */
		File f = new File("/home/troll/text.txt");
		if (f.exists()) {
			f.delete();
		}
		/* end of testing area */

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				data);
		// FileOutputStream fileOutputStream = new
		// FileOutputStream(f.getAbsolutePath());
		byte[] buffer = new byte[4000];
		int len = byteArrayInputStream.read(buffer, 0, 4000);
		String str = new String(buffer, 0, len);

		System.out.println(str);
		// fileOutputStream.write(buffer);

		// fileOutputStream.close();
		// System.out.println(new String(byteArrayOutputStream.toByteArray(),
		// "UTF-8"));

	}

	public InetAddress getLocalHost() {
		return localHost;
	}

	public InetAddress getRemoteHost() {
		return remoteHost;
	}

	public int getLocalPort() {
		return localPort;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void stop() {
		this.on = false;
		// socket.close();
	}

	public JingleFileTransferProcessMonitor getMonitor() {
		return this.monitor;
	}

	public void sendFileData(JingleFileTransferData[] transferData) {
		transmit = true;
		this.transferData = transferData;
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
