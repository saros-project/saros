package saros.ui.jobs;

import java.io.File;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IProgressConstants;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import saros.Saros;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;

/**
 * This job is intended to be used with a pending outgoing {@linkplain FileTransferRequest XMPP file
 * transfer}. It will start the file transfer and monitor the status of the process.
 *
 * <p>This job supports cancellation.
 */
public final class OutgoingFileTransferJob extends FileTransferJob {

  private static final Logger LOG = Logger.getLogger(OutgoingFileTransferJob.class);

  private final OutgoingFileTransfer transfer;
  private final File file;

  public OutgoingFileTransferJob(OutgoingFileTransfer transfer, File file, JID jid) {
    super("File Transfer", jid);
    setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    this.transfer = transfer;
    this.file = file;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {

    String nickname = XMPPUtils.getNickname(null, jid, jid.getBase());

    try {
      transfer.sendFile(file, file.getName());

      monitor.beginTask(
          "Waiting for " + nickname + " to accept the file transfer...", IProgressMonitor.UNKNOWN);

      while (!transfer.isDone()) {
        if (monitor.isCanceled()) break;

        boolean proceed = true;

        if (transfer.getStatus()
            == org.jivesoftware.smackx.filetransfer.FileTransfer.Status.negotiating_transfer)
          proceed = false;

        if (transfer.getStatus()
            == org.jivesoftware.smackx.filetransfer.FileTransfer.Status.initial) proceed = false;

        if (proceed) break;

        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          transfer.cancel();
          break;
        }
      }

      monitor.done();
      monitor.beginTask("Sending file " + file.getName(), 100);

      return monitorTransfer(transfer, monitor);
    } catch (RuntimeException e) {
      LOG.error("internal error in file transfer", e);
      throw e;
    } catch (XMPPException e) {
      LOG.error("file transfer failed: " + jid, e);

      return new Status(IStatus.ERROR, Saros.PLUGIN_ID, "file transfer failed", e);
    } finally {
      monitor.done();
    }
  }
}
