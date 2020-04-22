package saros.ui.jobs;

import java.io.File;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IProgressConstants;
import saros.Saros;
import saros.SarosPluginContext;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.filetransfer.XMPPFileTransfer;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.repackaged.picocontainer.annotations.Inject;

/**
 * This job will start a file transfer and monitor the status of the process.
 *
 * <p>This job supports cancellation.
 */
public final class OutgoingFileTransferJob extends FileTransferJob {
  private static final Logger log = Logger.getLogger(OutgoingFileTransferJob.class);

  @Inject private XMPPFileTransferManager transferManager;
  private final File file;

  public OutgoingFileTransferJob(JID jid, File file) {
    super("File Transfer", jid);
    setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    this.file = file;

    SarosPluginContext.initComponent(this);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      monitor.beginTask(
          "Waiting for "
              + XMPPUtils.getNickname(null, jid, jid.getBase())
              + " to accept the file transfer...",
          IProgressMonitor.UNKNOWN);

      XMPPFileTransfer transfer = transferManager.fileSendStart(jid, file, null);
      BooleanSupplier checkLocalCancel = () -> monitor.isCanceled();
      transfer.waitForTransferStart(checkLocalCancel);

      if (checkLocalCancel.getAsBoolean()) {
        log.info("Monitor canceled locally.");
        return Status.OK_STATUS;
      }

      monitor.done();

      monitor.beginTask("Sending file " + file.getName(), 100);
      return monitorTransfer(transfer.getSmackTransfer(), monitor);
    } catch (IllegalArgumentException | IOException e) {
      log.error("file transfer failed: " + jid, e);
      return new Status(IStatus.ERROR, Saros.PLUGIN_ID, e.getMessage(), e);
    } finally {
      monitor.done();
    }
  }
}
