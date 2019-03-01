package saros.ui.jobs;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Error;
import saros.Saros;
import saros.monitoring.MonitorableFileTransfer;
import saros.monitoring.MonitorableFileTransfer.TransferStatus;
import saros.monitoring.ProgressMonitorAdapterFactory;
import saros.net.xmpp.JID;

abstract class FileTransferJob extends Job {

  private static final Logger LOG = Logger.getLogger(FileTransferJob.class);
  final JID jid;

  FileTransferJob(String name, JID jid) {
    super(name);
    this.jid = jid;
  }

  IStatus monitorTransfer(FileTransfer transfer, IProgressMonitor monitor) {
    MonitorableFileTransfer mtf =
        new MonitorableFileTransfer(transfer, ProgressMonitorAdapterFactory.convert(monitor));
    TransferStatus result = mtf.monitorTransfer();

    switch (result) {
      case ERROR:
        Error error = transfer.getError();

        /*
         * there is currently no chance to determine on the sender side if
         * the receiving side has canceled the transfer
         */
        String errMsg =
            error == null
                ? "File transfer failed. Maybe the remote side canceled the transfer."
                : error.getMessage();
        Status status = new Status(IStatus.ERROR, Saros.PLUGIN_ID, errMsg, transfer.getException());

        LOG.error("file transfer from " + jid + " failed: " + errMsg, transfer.getException());

        return status;
      case CANCEL:
        return Status.CANCEL_STATUS;

      case OK:
        // fall through
      default:
        return Status.OK_STATUS;
    }
  }
}
