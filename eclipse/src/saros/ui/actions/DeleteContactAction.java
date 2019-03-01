package saros.ui.actions;

import java.text.MessageFormat;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.net.ConnectionState;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
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

  protected IConnectionListener connectionListener =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection, final ConnectionState newState) {
          updateEnablement();
        }
      };

  protected ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  @Inject protected XMPPConnectionService connectionService;

  @Inject protected ISarosSessionManager sessionManager;

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

    connectionService.addListener(connectionListener);
    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
    updateEnablement();
  }

  protected void updateEnablement() {
    try {
      List<JID> contacts =
          SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();
      this.setEnabled(connectionService.isConnected() && contacts.size() == 1);
    } catch (NullPointerException e) {
      this.setEnabled(false);
    } catch (Exception e) {
      if (!PlatformUI.getWorkbench().isClosing())
        LOG.error("Unexpected error while updating enablement", e); // $NON-NLS-1$
    }
  }

  public static String toString(RosterEntry entry) {
    StringBuilder sb = new StringBuilder();
    String name = entry.getName();
    if (name != null && name.trim().length() > 0) {
      sb.append(Messages.DeleteContactAction_name_begin_deco)
          .append(name)
          .append(Messages.DeleteContactAction_name_end_deco);
    }
    sb.append(entry.getUser());
    return sb.toString();
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
    RosterEntry rosterEntry = null;
    List<RosterEntry> selectedRosterEntries =
        SelectionRetrieverFactory.getSelectionRetriever(RosterEntry.class).getSelection();
    if (selectedRosterEntries.size() == 1) {
      rosterEntry = selectedRosterEntries.get(0);
    }

    if (rosterEntry == null) {
      LOG.error("RosterEntry should not be null at this point!"); // $NON-NLS-1$
      return;
    }

    if (sessionManager != null) {
      // Is the chosen user currently in the session?
      ISarosSession sarosSession = sessionManager.getSession();
      String entryJid = rosterEntry.getUser();

      if (sarosSession != null) {
        for (User p : sarosSession.getUsers()) {
          String pJid = p.getJID().getBase();

          // If so, stop the deletion from completing
          if (entryJid.equals(pJid)) {
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
            Messages.DeleteContactAction_confirm_message, toString(rosterEntry)))) {

      try {
        XMPPUtils.removeFromRoster(connectionService.getConnection(), rosterEntry);
      } catch (XMPPException e) {
        LOG.error(
            "could not delete contact "
                + toString(rosterEntry) // $NON-NLS-1$
                + ":",
            e); //$NON-NLS-1$
      }
    }
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
    connectionService.removeListener(connectionListener);
  }
}
