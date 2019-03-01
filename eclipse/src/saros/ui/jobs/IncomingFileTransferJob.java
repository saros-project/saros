package saros.ui.jobs;

import java.io.File;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IProgressConstants;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import saros.Saros;
import saros.net.xmpp.JID;

/**
 * This job is intended to be used with a pending incoming {@linkplain FileTransferRequest XMPP file
 * transfer request}. It will accept the request and monitor the status of the file transfer
 * process.
 *
 * <p>This job supports cancellation.
 */
public final class IncomingFileTransferJob extends FileTransferJob {

  private static final Logger LOG = Logger.getLogger(IncomingFileTransferJob.class);

  private final FileTransferRequest request;
  private final File file;

  public IncomingFileTransferJob(FileTransferRequest request, File file, JID jid) {

    super("File Transfer", jid);
    setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    this.request = request;
    this.file = file;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    monitor.beginTask("Receiving file " + request.getFileName(), 100);
    IncomingFileTransfer transfer = request.accept();

    try {
      transfer.recieveFile(file);

      return monitorTransfer(transfer, monitor);
    } catch (RuntimeException e) {
      LOG.error("internal error in file transfer", e);
      throw e;
    } catch (Exception e) {
      LOG.error("file transfer failed: " + jid, e);

      return new Status(IStatus.ERROR, Saros.PLUGIN_ID, "file transfer failed", e);
    } finally {
      monitor.done();
    }
  }
}
