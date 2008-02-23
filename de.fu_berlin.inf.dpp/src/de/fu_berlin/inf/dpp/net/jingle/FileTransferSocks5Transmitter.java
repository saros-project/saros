package de.fu_berlin.inf.dpp.net.jingle;

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

public class FileTransferSocks5Transmitter implements IFileTransferTransmitter,
		Runnable {

	private InetAddress localHost;
	private InetAddress remoteHost;
	private int localPort;
	private int remotePort;
	public static final int tileWidth = 25;
	private boolean on = true;
	private boolean transmit = false;

	public FileTransferSocks5Transmitter(int localPort, InetAddress remoteHost,
			int remotePort) {

		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;

		transmit = true;

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

					
					sendFile(socket, "lib/smack.jar");

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

	private void sendString(Socket socket) throws IOException, ClassNotFoundException{
		ObjectOutputStream oo = new ObjectOutputStream(socket
				.getOutputStream());
		ObjectInputStream ii = new ObjectInputStream(socket
				.getInputStream());

		oo.writeObject("Test-String");
		oo.flush();
		System.out.println((String) ii.readObject());

		socket.close();
		ii.close();
		oo.close();
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
		System.out.println("File length: "+length);
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = bis.read(buffer,0,1024)) != -1) {
			output.write(buffer,0,1024);
			output.flush();
		}
		System.out.println("File has send");

		fis.close();
		output.close();
		socket.close();
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
