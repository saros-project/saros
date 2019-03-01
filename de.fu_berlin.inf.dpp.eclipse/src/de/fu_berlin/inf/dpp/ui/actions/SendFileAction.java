package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.jobs.IncomingFileTransferJob;
import de.fu_berlin.inf.dpp.ui.jobs.OutgoingFileTransferJob;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.CoreUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.picocontainer.annotations.Inject;

/**
 * Action for sending and receiving a file over XMPP.
 *
 * @author srossbach
 */
/*
 * TODO the receiving and file transfer creation part is misplaced here ... wrap
 * those calls and put them in the dpp.net package e.g XMPPFileTransfer class
 * hiding the need for tracking the XMPPConnection status etc.
 */

/*
 * FIXME as the roster currently does not support multiple resources it can be
 * random which presence will receive the file
 */
public class SendFileAction extends Action implements Disposable {

  public static final String ACTION_ID = SendFileAction.class.getName();

  private static final Logger LOG = Logger.getLogger(SendFileAction.class);

  // static smack ****
  static {
    OutgoingFileTransfer.setResponseTimeout(5 * 60 * 1000);
  }

  private FileTransferListener fileTransferListener =
      new FileTransferListener() {

        @Override
        public void fileTransferRequest(final FileTransferRequest request) {

          final String description = request.getDescription();

          if (description != null && description.startsWith(ProjectNegotiation.TRANSFER_ID_PREFIX))
            return;

          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {
                @Override
                public void run() {
                  handleIncomingFileTransferRequest(request);
                }
              });
        }
      };

  private ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  private IConnectionListener connectionListener =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(
            final Connection connection, final ConnectionState state) {
          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {

                @Override
                public void run() {

                  switch (state) {
                    case CONNECTING:
                      break;
                    case CONNECTED:
                      updateFileTransferManager(connection);
                      break;
                    case DISCONNECTING:
                    case ERROR:
                    case NOT_CONNECTED:
                      updateFileTransferManager(null);
                      break;
                  }

                  updateEnablement();
                }
              });
        }
      };

  @Inject private XMPPConnectionService connectionService;

  private FileTransferManager fileTransferManager;

  private Connection connection;

  public SendFileAction() {
    super(Messages.SendFileAction_title);
    SarosPluginContext.initComponent(this);

    setImageDescriptor(
        PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
    setId(ACTION_ID);
    setToolTipText(Messages.SendFileAction_tooltip);

    SarosPluginContext.initComponent(this);

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    connectionService.addListener(connectionListener);
    updateFileTransferManager(connectionService.getConnection());

    updateEnablement();
  }

  @Override
  public void run() {

    if (!canRun()) return;

    final JID jid = getSelectedJID();

    final FileDialog fd = new FileDialog(SWTUtils.getShell(), SWT.OPEN);
    fd.setText(Messages.SendFileAction_filedialog_text);

    final String filename = fd.open();

    if (filename == null) return;

    final File file = new File(filename);

    if (file.isDirectory()) return;

    // connection changes are executed while the dialog is open !
    if (fileTransferManager == null) return;

    final OutgoingFileTransfer transfer =
        fileTransferManager.createOutgoingFileTransfer(jid.getRAW());

    Job job = new OutgoingFileTransferJob(transfer, file, jid);
    job.setUser(true);
    job.schedule();
  }

  @Override
  public void dispose() {
    connectionService.removeListener(connectionListener);

    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }

  private void updateEnablement() {
    setEnabled(canRun());
  }

  private boolean canRun() {
    return fileTransferManager != null && getSelectedJID() != null;
  }

  private JID getSelectedJID() {
    List<User> sessionUsers =
        SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    List<JID> contacts = SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    // currently only one transfer per click (maybe improved later)
    if (contacts.size() + sessionUsers.size() != 1) return null;

    if (sessionUsers.size() == 1 && sessionUsers.get(0).isLocal()) return null;

    if (contacts.size() == 1 && !isOnline(contacts.get(0))) return null;

    if (sessionUsers.size() == 1) return sessionUsers.get(0).getJID();

    // FIXME see TODO at class level ... this currently does not work well
    // if (contacts.size() == 1 && !isOnline(contacts.get(0)))
    // return null;
    // return contacts.get(0);

    // workaround
    if (connection == null) return null;

    Presence presence = connection.getRoster().getPresence(contacts.get(0).getBase());

    if (!presence.isAvailable() || presence.getFrom() == null) return null;

    return new JID(presence.getFrom());
  }

  private void updateFileTransferManager(Connection connection) {
    if (connection == null) {
      if (fileTransferManager != null)
        fileTransferManager.removeFileTransferListener(fileTransferListener);

      fileTransferManager = null;
    } else {
      fileTransferManager = new FileTransferManager(connection);
      fileTransferManager.addFileTransferListener(fileTransferListener);
    }

    this.connection = connection;
  }

  private boolean isOnline(JID jid) {
    if (connection == null) return false;

    return connection.getRoster().getPresenceResource(jid.getRAW()).isAvailable();
  }

  // TODO popping up dialogs can create a very bad UX but we have currently no
  // other awareness methods
  private void handleIncomingFileTransferRequest(final FileTransferRequest request) {

    final String filename = request.getFileName();
    final long fileSize = request.getFileSize();
    final JID jid = new JID(request.getRequestor());

    String nickname = XMPPUtils.getNickname(null, jid, new JID(request.getRequestor()).getBase());

    final boolean accept =
        MessageDialog.openQuestion(
            SWTUtils.getShell(),
            "File Transfer Request",
            nickname
                + " wants to send a file."
                + "\nName: "
                + filename
                + "\nSize: "
                + CoreUtils.formatByte(fileSize)
                + (fileSize < 1000 ? "yte" : "")
                + "\n\nAccept the file?");

    if (!accept) {
      request.reject();
      return;
    }

    final FileDialog fd = new FileDialog(SWTUtils.getShell(), SWT.SAVE);
    fd.setText(Messages.SendFileAction_filedialog_text);
    fd.setOverwrite(true);
    fd.setFileName(filename);

    final String destination = fd.open();

    if (destination == null) {
      request.reject();
      return;
    }

    final File file = new File(destination);

    if (file.isDirectory()) {
      request.reject();
      return;
    }

    Job job = new IncomingFileTransferJob(request, file, jid);
    job.setUser(true);
    job.addJobChangeListener(
        new JobChangeAdapter() {
          @Override
          public void done(IJobChangeEvent event) {
            event.getJob().removeJobChangeListener(this);

            // TODO UX this may be to annoying
            if (event.getResult().getCode() == IStatus.OK) showFileInOSGui(file);
          }
        });
    job.schedule();
  }

  private static void showFileInOSGui(File file) {
    String osName = System.getProperty("os.name");
    if (osName == null || !osName.toLowerCase().contains("windows")) return;

    try {
      new ProcessBuilder("explorer.exe", "/select," + file.getAbsolutePath()).start();
    } catch (IOException e) {
      // ignore
    }
  }
}
