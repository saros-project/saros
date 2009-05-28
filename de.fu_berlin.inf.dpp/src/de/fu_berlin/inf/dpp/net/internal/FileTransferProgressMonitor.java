package de.fu_berlin.inf.dpp.net.internal;

import org.apache.log4j.Logger;
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

    private static final Logger log = Logger
        .getLogger(FileTransferProgressMonitor.class.getName());

    protected FileTransfer transfer;

    protected RuntimeException exception;

    protected boolean running = true;

    protected IFileTransferCallback callback;

    protected long filesize;

    public FileTransferProgressMonitor(FileTransfer transfer,
        IFileTransferCallback callback, long filesize) {
        this.transfer = transfer;
        this.callback = callback;
        this.filesize = filesize;
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

    public void cancel() {
        this.transfer.cancel();
        this.running = false;
    }

    public double getProgress() {
        // transfer.getProgress() has a bug, when sending a stream
        return ((double) this.transfer.getAmountWritten()) / this.filesize;
    }

    public int getProgressPercent() {
        return (int) (100 * getProgress());
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        try {
            while (this.running && !this.transfer.isDone()) {

                log.trace("Progress " + getProgressPercent() + "%");

                if (this.callback != null) {
                    this.callback.transferProgress(getProgressPercent());
                }
                Thread.sleep(100);
            }
        } catch (RuntimeException e) {
            this.exception = e;
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
            Thread.currentThread().interrupt();
        } finally {
            this.running = false;
        }
    }
}
