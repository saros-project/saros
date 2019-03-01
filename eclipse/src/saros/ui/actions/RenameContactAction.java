package saros.ui.actions;

import java.text.MessageFormat;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.util.ThreadUtils;

/**
 * Renames the nickname of the selected roster entry.
 *
 * @author rdjemili
 */
public class RenameContactAction extends Action {

  public static final String ACTION_ID = RenameContactAction.class.getName();

  private static final Logger LOG = Logger.getLogger(RenameContactAction.class);

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

  @Inject private XMPPConnectionService connectionService;

  public RenameContactAction() {
    super(Messages.RenameContactAction_title);

    setId(ACTION_ID);
    setToolTipText(Messages.RenameContactAction_tooltip);
    setImageDescriptor(ImageManager.ETOOL_EDIT);

    SarosPluginContext.initComponent(this);

    connectionService.addListener(connectionListener);

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
    updateEnablement();
  }

  protected void updateEnablement() {
    List<JID> contacts = SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    setEnabled(connectionService.isConnected() && contacts.size() == 1);
  }

  @Override
  public void run() {
    ThreadUtils.runSafeSync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            RosterEntry rosterEntry = null;
            List<RosterEntry> selectedRosterEntries =
                SelectionRetrieverFactory.getSelectionRetriever(RosterEntry.class).getSelection();
            if (selectedRosterEntries.size() == 1) {
              /*
               * TODO Why forbid renaming self? Is the own entry displayed
               * at all?
               */
              // Compare the plain-JID portion of the XMPP address
              if (!new JID(selectedRosterEntries.get(0).getUser())
                  .equals(connectionService.getJID())) {
                rosterEntry = selectedRosterEntries.get(0);
              }
            }

            if (rosterEntry == null) {
              LOG.error("RosterEntry should not be null at this point!"); // $NON-NLS-1$
              return;
            }

            Shell shell = SWTUtils.getShell();

            assert shell != null
                : "Action should not be run if the display is disposed"; //$NON-NLS-1$

            String message =
                MessageFormat.format(
                    Messages.RenameContactAction_rename_message, rosterEntry.getUser());

            if (rosterEntry.getName() != null
                && !rosterEntry.getName().equals(rosterEntry.getUser())) {
              message +=
                  MessageFormat.format(
                      Messages.RenameContactAction_rename_current_nickname_message,
                      rosterEntry.getName());
            }
            message += ":"; // $NON-NLS-1$

            InputDialog dialog =
                new InputDialog(
                    shell,
                    Messages.RenameContactAction_new_nickname_dialog_title,
                    message,
                    rosterEntry.getName(),
                    null);

            if (dialog.open() == Window.OK) {
              String newName = dialog.getValue();
              rosterEntry.setName(newName.length() == 0 ? null : newName);
            }
          }
        });
  }

  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
    connectionService.removeListener(connectionListener);
  }
}
