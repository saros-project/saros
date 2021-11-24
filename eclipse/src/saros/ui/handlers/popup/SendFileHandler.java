package saros.ui.handlers.popup;

import java.io.File;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.jobs.OutgoingFileTransferJob;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;

/** Action for sending a file over XMPP. */
public class SendFileHandler {

  public static final String ID = SendFileHandler.class.getName();

  private ISelectionListener selectionListener = (part, selection) -> updateEnablement();

  private MDirectMenuItem sendFileMenuItem;
  private ESelectionService selectionService;

  @PostConstruct
  public void postConstruct(
      MPart sarosView, EModelService service, ESelectionService selectionService) {
    selectionService.addSelectionListener(selectionListener);
    this.selectionService = selectionService;
    // TODO: create icon

    MPopupMenu popupMenu = null;

    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }
    MUIElement menuItem = service.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      sendFileMenuItem = (MDirectMenuItem) menuItem;
    }

    updateEnablement();
  }

  @Execute
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

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
  }

  private void updateEnablement() {
    if (sendFileMenuItem == null) return;

    sendFileMenuItem.setEnabled(getSelectedJID() != null);
  }

  @CanExecute
  public boolean canExecute() {
    return getSelectedJID() != null;
  }

  private JID getSelectedJID() {
    List<User> sessionUsers =
        SelectionUtils.getAdaptableObjects(
            (ISelection) selectionService.getSelection(), User.class);

    List<XMPPContact> contacts =
        SelectionUtils.getAdaptableObjects(
            (ISelection) selectionService.getSelection(), XMPPContact.class);

    // currently only one transfer per click (maybe improved later)
    if (contacts.size() + sessionUsers.size() != 1) return null;

    if (sessionUsers.size() == 1) {
      if (sessionUsers.get(0).isLocal()) return null;
      return sessionUsers.get(0).getJID();
    }

    return contacts.get(0).getOnlineJid().orElse(null);
  }
}
