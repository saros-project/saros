package de.fu_berlin.inf.dpp.net.internal;

import org.apache.log4j.Logger;
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

    private static Logger logger = Logger
	    .getLogger(FileTransferProcessMonitor.class);

    private IFileTransferCallback callback;
    private boolean closeMonitor = false;

    private boolean running = true;

    private final int TIMEOUT = 10000;

    FileTransfer transfer;

    public FileTransferProcessMonitor(FileTransfer transfer) {
	this.transfer = transfer;
	start();
    }

    public FileTransferProcessMonitor(FileTransfer transfer,
	    IFileTransferCallback callback) {
	this.transfer = transfer;
	this.callback = callback;
	start();
    }

    public void closeMonitor(boolean close) {
	this.closeMonitor = close;
    }

    public String getException() {
	return null;
    }

    public boolean isRunning() throws XMPPException {
	return this.running;
    }

    @Override
    public void run() {
	int time = 0;

	while (!this.closeMonitor) {
	    try {
		while (!this.transfer.isDone()
			&& (this.transfer.getProgress() < 1.0)) {

		    /* check negotiator process */
		    FileTransferProcessMonitor.logger.debug("Status: "
			    + this.transfer.getStatus() + " Progress : "
			    + this.transfer.getProgress());
		    if (this.callback != null) {
			this.callback.transferProgress((int) (this.transfer
				.getProgress() * 100));
		    }
		    if (this.closeMonitor) {
			return;
		    }
		    Thread.sleep(500);
		}
		this.running = false;
		time = time + 500;

		if (time > this.TIMEOUT) {
		    this.closeMonitor = true;
		    return;
		}
		Thread.sleep(500);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

    }

}
