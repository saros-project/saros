package saros.ui.jobs;

import java.io.File;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IProgressConstants;
import saros.Saros;
import saros.net.xmpp.filetransfer.XMPPFileTransferRequest;

/**
 * This job is intended to be used with a incoming {@linkplain XMPPFileTransferRequest}. It will
 * accept the request and monitor the status of the file transfer process.
 *
 * <p>This job supports cancellation.
 */
public final class IncomingFileTransferJob extends FileTransferJob {

  private static final Logger log = Logger.getLogger(IncomingFileTransferJob.class);

  private final XMPPFileTransferRequest request;
  private final File file;

  public IncomingFileTransferJob(XMPPFileTransferRequest request, File file) {
    super("File Transfer", request.getContact().getBareJid());
    setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    this.request = request;
    this.file = file;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    monitor.beginTask("Receiving file " + request.getFileName(), 100);

    try {
      return monitorTransfer(request.acceptFile(file), monitor);
    } catch (Exception e) {
      log.error("file transfer failed: " + jid, e);

      return new Status(IStatus.ERROR, Saros.PLUGIN_ID, "File transfer failed.", e);
    } finally {
      monitor.done();
    }
  }
}
