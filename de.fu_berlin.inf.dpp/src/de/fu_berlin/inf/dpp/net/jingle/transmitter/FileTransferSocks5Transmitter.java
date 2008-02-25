package de.fu_berlin.inf.dpp.net.jingle.transmitter;

import java.awt.Robot;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.IBBTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.Socks5TransferNegotiatorManager;

import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData.FileTransferType;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter.FileTransferData;
import de.fu_berlin.inf.dpp.net.jingle.IFileTransferTransmitter;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferProcessMonitor;

public class FileTransferSocks5Transmitter implements IFileTransferTransmitter,
		Runnable {

	private InetAddress localHost;
	private InetAddress remoteHost;
	private int localPort;
	private int remotePort;
	public static final int tileWidth = 25;
	private boolean on = true;
	private boolean transmit = false;

	/* transfer information */
	private JingleFileTransferData[] transferData;
	private JingleFileTransferProcessMonitor monitor;

	private FileTransferSocks5Transmitter(int localPort,
			InetAddress remoteHost, int remotePort) {

		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;

		transmit = true;

	}

	public FileTransferSocks5Transmitter(int localPort, InetAddress remoteHost,
			int remotePort, JingleFileTransferData[] transferData, JingleFileTransferProcessMonitor monitor) {

		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;

		transmit = true;
		this.transferData = transferData;
		this.monitor = monitor;
	}

	public void run() {
		start();
	}

	public void start() {

		Socket socket = null;

		while (on) {
			if (transmit) {
				try {

					/* Übertragung zwischen zwei Partnern. */
					socket = new Socket(remoteHost, remotePort);
					
					/*send file number.*/
					OutputStream os = socket.getOutputStream();
					os.write(transferData.length);
					
					for (int i = 0; i < transferData.length; i++) {

						/*testing. only*/
//						 sendFile(socket, "/home/troll/Saros_DPP_1.0.2.jar");
						
						/* send file meta data */
						sendMetaData(socket, transferData[i]);
						
						if(transferData[i].type == FileTransferType.FILELIST_TRANSFER){
							sendFileListData(socket, transferData[i].file_list_content);
						}
						if(transferData[i].type == FileTransferType.RESOURCE_TRANSFER){
							sendFile(socket, transferData[i].file);
						}
						
					}
					
					socket.close();
					
					/*set monitor status complete :) */
					monitor.setComplete(true);
					
					// Thread.sleep(2000);
					transmit = false;
					// socket.close();
					on = false;
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}
			}
		}
	}

	private void sendFileListData(Socket socket, String file_list_content) throws IOException {
		ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());

		oo.writeObject(file_list_content);
		oo.flush();
	}

	private void sendMetaData(Socket socket, JingleFileTransferData data)
			throws IOException {
		ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
		// ObjectInputStream ii = new ObjectInputStream(socket
		// .getInputStream());

		oo.writeObject(data);
		oo.flush();
	}



	private void readByteArray(InputStream input, OutputStream output)
			throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(output);
		ObjectInputStream iis = new ObjectInputStream(input);

		/* Einlesen der gepufferten Daten. */

	}

	private void sendFile(Socket socket, String fileName) throws IOException {

		OutputStream output = socket.getOutputStream();
		File file = new File(fileName);
		int length = (int) file.length();
		System.out.println("File length: " + length);
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
			output.write(buffer, 0, 1024);
			output.flush();
		}
		System.out.println("File has send");

		fis.close();
//		output.close();
	}

	private void sendFile(Socket socket, File file) throws IOException {
		OutputStream output = socket.getOutputStream();
		int length = (int) file.length();
		System.out.println("File length: " + length);
		FileInputStream fis = new FileInputStream(file.getAbsolutePath());
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
			output.write(buffer, 0, 1024);
			output.flush();
		}

		fis.close();
//		output.close();
	}

	
	@Deprecated
	private void sendString(Socket socket) throws IOException,
			ClassNotFoundException {
		ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream ii = new ObjectInputStream(socket.getInputStream());

		oo.writeObject("Test-String");
		oo.flush();
		System.out.println((String) ii.readObject());

//		socket.close();
//		ii.close();
//		oo.close();
	}
	
	@Deprecated
	private void startByteFileTransfer() {

		// System.out.println("Angemeldet: ");
		//		
		// // input = (ByteArrayInputStream) socket.getInputStream();
		// output = (ByteArrayOutputStream)socket.getOutputStream();
		//		
		// File file = new File("/home/troll/test.txt");
		// FileInputStream fileInputStream = new FileInputStream(file);
		// byte[] buffer = new byte[256];
		// for (int len = fileInputStream.read(buffer); len > 0; len =
		// fileInputStream
		// .read(buffer)) {
		// output.write(buffer, 0, len);
		// }
		// fileInputStream.close();

	}

	@Deprecated
	private void testNumberTransfer() {
		Socket socket = null;
		// ByteArrayInputStream input = null;
		// ByteArrayOutputStream output = null;
		while (on) {
			if (transmit) {
				try {
					/* Übertragung zwischen zwei Partnern. */
					socket = new Socket(remoteHost, remotePort);

					InputStream input = socket.getInputStream();
					OutputStream output = socket.getOutputStream();

					output.write(5);
					output.write(23);
					output.flush();

					System.out.println("Server antwort: " + input.read());
					socket.close();
					input.close();
					output.close();

					// Thread.sleep(2000);
					transmit = false;
					// socket.close();
					on = false;
				} catch (Exception e1) {

					e1.printStackTrace();
				}
			}
		}
	}

	// private static void method0(File file) throws Exception, IOException,
	// UnsupportedEncodingException {
	//
	// FileInputStream fileInputStream = new FileInputStream(file);
	// byte[] data = new byte[(int) file.length()];
	// fileInputStream.read(data);
	// fileInputStream.close();
	// System.out.println(new String(data, "UTF-8"));
	// }

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

	/**
	 * Set Transmit Enabled/Disabled
	 * 
	 * @param transmit
	 *            boolean Enabled/Disabled
	 */
	public void setTransmit(boolean transmit) {
		this.transmit = transmit;
	}

	/**
	 * Stops Transmitter
	 */
	public void stop() {
		this.transmit = false;
		this.on = false;
	}
}
