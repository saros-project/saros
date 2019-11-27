package saros.ui.actions;

import java.text.MessageFormat;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.util.ThreadUtils;

public class DeleteContactAction extends Action implements Disposable {

  public static final String ACTION_ID = DeleteContactAction.class.getName();

  private static final Logger LOG = Logger.getLogger(DeleteContactAction.class);

  protected IConnectionStateListener connectionListener = (state, error) -> updateEnablement();

  protected ISelectionListener selectionListener = (part, selection) -> updateEnablement();

  @Inject private ConnectionHandler connectionHandler;
  @Inject private XMPPContactsService contactsService;
  @Inject private ISarosSessionManager sessionManager;

  protected final String DELETE_ERROR_IN_SESSION =
      Messages.DeleteContactAction_delete_error_in_session;

  public DeleteContactAction() {
    super(Messages.DeleteContactAction_title);

    setId(ACTION_ID);
    setToolTipText(Messages.DeleteContactAction_tooltip);

    IWorkbench workbench = PlatformUI.getWorkbench();
    setImageDescriptor(
        workbench.getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

    SarosPluginContext.initComponent(this);

    connectionHandler.addConnectionStateListener(connectionListener);
    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
    updateEnablement();
  }

  protected void updateEnablement() {
    try {
      List<JID> contacts =
          SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();
      this.setEnabled(connectionHandler.isConnected() && contacts.size() == 1);
    } catch (NullPointerException e) {
      this.setEnabled(false);
    } catch (Exception e) {
      if (!PlatformUI.getWorkbench().isClosing())
        LOG.error("Unexpected error while updating enablement", e); // $NON-NLS-1$
    }
  }

  /** @review runSafe OK */
  @Override
  public void run() {
    ThreadUtils.runSafeSync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            runDeleteAction();
          }
        });
  }

  public void runDeleteAction() {
    XMPPContact contact = null;
    List<XMPPContact> selectedRosterEntries =
        SelectionRetrieverFactory.getSelectionRetriever(XMPPContact.class).getSelection();
    if (selectedRosterEntries.size() == 1) {
      contact = selectedRosterEntries.get(0);
    }

    if (contact == null) {
      LOG.error("XMPPContact should not be null at this point!"); // $NON-NLS-1$
      return;
    }

    if (sessionManager != null) {
      // Is the chosen user currently in the session?
      ISarosSession sarosSession = sessionManager.getSession();
      if (sarosSession != null) {
        for (User p : sarosSession.getUsers()) {
          // If so, stop the deletion from completing
          if (contact.getBareJid().equals(p.getJID())) {
            MessageDialog.openError(
                null, Messages.DeleteContactAction_error_title, DELETE_ERROR_IN_SESSION);
            return;
          }
        }
      }
    }

    if (MessageDialog.openQuestion(
        null,
        Messages.DeleteContactAction_confirm_title,
        MessageFormat.format(
            Messages.DeleteContactAction_confirm_message, contact.getDisplayableNameLong()))) {

      contactsService.removeContact(contact);
    }
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
    connectionHandler.removeConnectionStateListener(connectionListener);
  }
}
