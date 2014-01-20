package de.fu_berlin.inf.dpp.ui.jobs;

import org.apache.commons.lang.time.StopWatch;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jivesoftware.smackx.filetransfer.FileTransfer;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.util.Utils;

abstract class FileTransferJob extends Job {

    protected FileTransferJob(String name) {
        super(name);
    }

    // modified copy from ProjectNegotation class
    protected final IStatus monitorFileTransfer(FileTransfer transfer,
        IProgressMonitor monitor) {

        if (monitor == null)
            monitor = new NullProgressMonitor();

        final long fileSize = transfer.getFileSize();

        int lastWorked = 0;

        StopWatch watch = new StopWatch();
        watch.start();

        while (!transfer.isDone()) {
            if (monitor.isCanceled()) {
                transfer.cancel();
                continue;
            }

            // may return -1 if the transfer has not yet started
            long bytesWritten = transfer.getAmountWritten();

            if (bytesWritten < 0)
                bytesWritten = 0;

            int worked = (int) ((100 * bytesWritten) / (fileSize == 0 ? 1
                : fileSize));

            int delta = worked - lastWorked;

            if (delta > 0) {
                lastWorked = worked;
                monitor.worked(delta);
            }

            long bytesPerSecond = watch.getTime();

            if (bytesPerSecond > 0)
                bytesPerSecond = (bytesWritten * 1000) / bytesPerSecond;

            long secondsLeft = 0;

            if (bytesPerSecond > 0)
                secondsLeft = (fileSize - bytesWritten) / bytesPerSecond;

            String remaingTime = "Remaining time: "
                + (bytesPerSecond == 0 ? "N/A" : Utils
                    .formatDuration(secondsLeft)
                    + " ("
                    + Utils.formatByte(bytesPerSecond) + "/s)");

            monitor.subTask(remaingTime);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                monitor.setCanceled(true);
                continue;
            }
        }

        org.jivesoftware.smackx.filetransfer.FileTransfer.Status status = transfer
            .getStatus();

        if (status
            .equals(org.jivesoftware.smackx.filetransfer.FileTransfer.Status.complete)) {

            long bytesWritten = transfer.getAmountWritten();

            if (bytesWritten < fileSize)
                return Status.CANCEL_STATUS;
            else
                return Status.OK_STATUS;
        }

        /*
         * there is currently no chance to determine on the sender side if the
         * receiving side has cancelled the transfer
         */

        if (status
            .equals(org.jivesoftware.smackx.filetransfer.FileTransfer.Status.error)) {
            FileTransfer.Error error = transfer.getError();
            return new Status(
                IStatus.ERROR,
                Saros.SAROS,
                error == null ? "File transfer failed. Maybe the remote side cancelled the transfer."
                    : error.getMessage(), transfer.getException());
        }

        // either cancelled or refused
        return Status.CANCEL_STATUS;
    }
}
