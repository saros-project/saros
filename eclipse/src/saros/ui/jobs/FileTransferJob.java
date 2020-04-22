package saros.ui.jobs;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import saros.Saros;
import saros.monitoring.MonitorableFileTransfer;
import saros.monitoring.MonitorableFileTransfer.TransferStatus;
import saros.monitoring.ProgressMonitorAdapterFactory;
import saros.net.xmpp.JID;
import saros.net.xmpp.filetransfer.XMPPFileTransfer;

abstract class FileTransferJob extends Job {

  private static final Logger log = Logger.getLogger(FileTransferJob.class);
  final JID jid;

  FileTransferJob(String name, JID jid) {
    super(name);
    this.jid = jid;
  }

  IStatus monitorTransfer(XMPPFileTransfer transfer, IProgressMonitor monitor) {
    MonitorableFileTransfer mtf =
        new MonitorableFileTransfer(transfer, ProgressMonitorAdapterFactory.convert(monitor));
    TransferStatus result = mtf.monitorTransfer();

    switch (result) {
      case ERROR:
        /*
         * there is currently no chance to determine on the sender side if
         * the receiving side has canceled the transfer
         */
        IOException exception = transfer.getException().orElse(null);
        if (exception == null) {
          log.error(
              "File transfer from "
                  + jid
                  + " has an error status but no exception. This should not happen!");
          return new Status(
              IStatus.ERROR,
              Saros.PLUGIN_ID,
              "File transfer failed. Maybe the remote side canceled the transfer.");
        }

        log.error("file transfer from " + jid + " failed", exception);
        return new Status(
            IStatus.ERROR, Saros.PLUGIN_ID, exception.getMessage(), exception.getCause());

      case CANCEL:
        return Status.CANCEL_STATUS;

      case OK:
        // fall through
      default:
        return Status.OK_STATUS;
    }
  }
}
