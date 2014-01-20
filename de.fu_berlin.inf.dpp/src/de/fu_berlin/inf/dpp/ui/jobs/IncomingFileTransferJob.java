package de.fu_berlin.inf.dpp.ui.jobs;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IProgressConstants;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This job is intended to be used with a pending incoming
 * {@linkplain FileTransferRequest XMPP file transfer request}. It will accept
 * the request and monitor the status of the file transfer process.
 * <p>
 * This job supports cancellation.
 */
public final class IncomingFileTransferJob extends FileTransferJob {

    private static final Logger LOG = Logger
        .getLogger(IncomingFileTransferJob.class);

    private final FileTransferRequest request;
    private final File file;
    private final JID jid;

    public IncomingFileTransferJob(FileTransferRequest request, File file,
        JID jid) {
        super("File Transfer");
        setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
        this.request = request;
        this.file = file;
        this.jid = jid;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask("Receiving file " + request.getFileName(), 100);
        IncomingFileTransfer transfer = request.accept();

        try {
            transfer.recieveFile(file);
            final IStatus status = monitorFileTransfer(transfer, monitor);

            // TODO UX this may be to annoying
            if (status.getCode() == IStatus.OK)
                showFileInOSGui(file);

            if (status.getCode() == IStatus.ERROR)
                LOG.error(
                    "file transfer from " + jid + " failed: "
                        + status.getMessage(), status.getException());

            return status;
        } catch (RuntimeException e) {
            LOG.error("internal error in file transfer", e);
            throw e;
        } catch (Exception e) {
            LOG.error("file transfer failed: " + jid, e);
            return new Status(IStatus.ERROR, Saros.SAROS,
                "filetransfer failed", e);
        } finally {
            monitor.done();
        }
    }

    private static void showFileInOSGui(File file) {
        String osName = System.getProperty("os.name");
        if (osName == null || !osName.toLowerCase().contains("windows"))
            return;

        try {
            new ProcessBuilder("explorer.exe", "/select,"
                + file.getAbsolutePath()).start();
        } catch (IOException e) {
            // ignore
        }
    }
}
