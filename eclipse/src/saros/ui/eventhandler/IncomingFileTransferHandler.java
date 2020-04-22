package saros.ui.eventhandler;

import java.io.File;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.net.xmpp.filetransfer.XMPPFileTransferRequest;
import saros.ui.Messages;
import saros.ui.jobs.IncomingFileTransferJob;
import saros.ui.util.SWTUtils;
import saros.util.CoreUtils;

/** Handle incoming File Transfers. */
public class IncomingFileTransferHandler {

  public IncomingFileTransferHandler(XMPPFileTransferManager transferManager) {
    transferManager.setDefaultHandler(
        request -> SWTUtils.runSafeSWTAsync(null, () -> handleRequest(request)));
  }

  // TODO popping up dialogs can create a very bad UX but we have currently no
  // other awareness methods
  private void handleRequest(XMPPFileTransferRequest request) {
    String filename = request.getFileName();
    long fileSize = request.getFileSize();

    if (!MessageDialog.openQuestion(
        SWTUtils.getShell(),
        "File Transfer Request",
        request.getContact().getDisplayableName()
            + " wants to send a file."
            + "\nName: "
            + filename
            + "\nSize: "
            + CoreUtils.formatByte(fileSize)
            + (fileSize < 1000 ? "yte" : "")
            + "\n\nAccept the file?")) {
      request.reject();
      return;
    }

    FileDialog fd = new FileDialog(SWTUtils.getShell(), SWT.SAVE);
    fd.setText(Messages.SendFileAction_filedialog_text);
    fd.setOverwrite(true);
    fd.setFileName(filename);

    String destination = fd.open();
    if (destination == null) {
      request.reject();
      return;
    }

    File file = new File(destination);
    if (file.isDirectory()) {
      request.reject();
      return;
    }

    Job job = new IncomingFileTransferJob(request, file);
    job.setUser(true);
    job.schedule();
  }
}
