package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smackx.filetransfer.FileTransfer;

import de.fu_berlin.inf.dpp.net.IFileTransferCallback;

/**
 * for information on monitoring the process of a file tranfer
 * 
 * @author troll
 * @author oezbek
 * 
 */
public class FileTransferProgressMonitor extends Thread {

    protected FileTransfer transfer;

    protected RuntimeException exception;

    protected boolean running = true;

    protected IFileTransferCallback callback;

    public FileTransferProgressMonitor(FileTransfer transfer) {
        this.transfer = transfer;
        start();
    }

    public FileTransferProgressMonitor(FileTransfer transfer,
        IFileTransferCallback callback) {
        this.transfer = transfer;
        this.callback = callback;
        start();
    }

    public boolean isRunning() {
        return this.running;
    }

    /**
     * Returns an Exception that might have occurred while Monitoring the
     * progress
     * 
     * @return
     */
    public RuntimeException getMonitoringException() {
        return exception;
    }

    public void close() {
        this.running = false;
    }

    @Override
    public void run() {
        try {
            while (this.running && !this.transfer.isDone()
                && this.transfer.getProgress() < 1.0) {

                if (this.callback != null) {
                    this.callback.transferProgress((int) (this.transfer
                        .getProgress() * 100));
                }

                Thread.sleep(100);
            }
        } catch (RuntimeException e) {
            this.exception = e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            this.running = false;
        }
    }
}
