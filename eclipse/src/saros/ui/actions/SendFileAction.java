package saros.ui.actions;

import java.io.File;
import java.util.List;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.jobs.OutgoingFileTransferJob;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

/** Action for sending a file over XMPP. */
public class SendFileAction extends Action implements Disposable {

  public static final String ACTION_ID = SendFileAction.class.getName();

  private ISelectionListener selectionListener = (part, selection) -> updateEnablement();

  public SendFileAction() {
    super(Messages.SendFileAction_title);

    setImageDescriptor(
        PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
    setId(ACTION_ID);
    setToolTipText(Messages.SendFileAction_tooltip);

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    updateEnablement();
  }

  @Override
  public void run() {
    JID jid = getSelectedJID();
    if (jid == null) return;

    FileDialog fd = new FileDialog(SWTUtils.getShell(), SWT.OPEN);
    fd.setText(Messages.SendFileAction_filedialog_text);

    String filename = fd.open();
    if (filename == null) return;

    File file = new File(filename);
    if (file.isDirectory()) return;

    Job job = new OutgoingFileTransferJob(jid, file);
    job.setUser(true);
    job.schedule();
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }

  private void updateEnablement() {
    setEnabled(getSelectedJID() != null);
  }

  private JID getSelectedJID() {
    List<User> sessionUsers =
        SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    List<XMPPContact> contacts =
        SelectionRetrieverFactory.getSelectionRetriever(XMPPContact.class).getSelection();

    // currently only one transfer per click (maybe improved later)
    if (contacts.size() + sessionUsers.size() != 1) return null;

    if (sessionUsers.size() == 1) {
      if (sessionUsers.get(0).isLocal()) return null;
      return sessionUsers.get(0).getJID();
    }

    return contacts.get(0).getOnlineJid().orElse(null);
  }
}
