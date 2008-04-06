package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransfer;

import de.fu_berlin.inf.dpp.net.IFileTransferCallback;

/**
 * for information on monitoring the process of a file tranfer
 * 
 * @author troll
 * 
 */
public class FileTransferProcessMonitor extends Thread {

	FileTransfer transfer;
	private int TIMEOUT = 100000;

	private boolean running = true;

	private boolean closeMonitor = false;
	
	private IFileTransferCallback callback;

	public FileTransferProcessMonitor(FileTransfer transfer) {
		this.transfer = transfer;
		start();
	}
	
	public FileTransferProcessMonitor(FileTransfer transfer, IFileTransferCallback callback) {
		this.transfer = transfer;
		this.callback = callback;
		start();
	}

	public boolean isRunning() throws XMPPException {
		return this.running;
	}

	public String getException() {
		return null;
	}

	public void closeMonitor(boolean close) {
		this.closeMonitor = close;
	}

	public void run() {
		int time = 0;

		while (!closeMonitor ) {
			try {
				while (!transfer.isDone() && (transfer.getProgress()<1.0)) {

					/* check negotiator process */
					System.out.println("Status: " + transfer.getStatus()
							+ " Progress : " + transfer.getProgress());
					if(callback != null){
						callback.transferProgress((int)(transfer.getProgress()*100));
					}

					Thread.sleep(500);
				}
				this.running = false;
				time = time + 500;
				
				if(time>TIMEOUT){
					closeMonitor = true;
					return;
				}
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
