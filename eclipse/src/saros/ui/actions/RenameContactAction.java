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
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.util.ThreadUtils;

/** Renames the nickname of the selected roster entry. */
public class RenameContactAction extends Action {

  public static final String ACTION_ID = RenameContactAction.class.getName();

  private static final Logger log = Logger.getLogger(RenameContactAction.class);

  private IConnectionStateListener connectionListener = (state, error) -> updateEnablement();

  protected ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  @Inject private ConnectionHandler connectionHandler;
  @Inject private XMPPContactsService contactsService;

  public RenameContactAction() {
    super(Messages.RenameContactAction_title);

    setId(ACTION_ID);
    setToolTipText(Messages.RenameContactAction_tooltip);
    setImageDescriptor(ImageManager.ETOOL_EDIT);

    SarosPluginContext.initComponent(this);

    connectionHandler.addConnectionStateListener(connectionListener);

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
    updateEnablement();
  }

  protected void updateEnablement() {
    List<JID> contacts = SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    setEnabled(connectionHandler.isConnected() && contacts.size() == 1);
  }

  @Override
  public void run() {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            XMPPContact contact = null;
            List<XMPPContact> selectedRosterEntries =
                SelectionRetrieverFactory.getSelectionRetriever(XMPPContact.class).getSelection();
            if (selectedRosterEntries.size() == 1) {
              contact = selectedRosterEntries.get(0);
              /*
               * TODO Why forbid renaming self? Is the own entry displayed
               * at all?
               */
              if (contact.getBareJid().equals(connectionHandler.getLocalJID())) {
                log.error("Rename of own contact is forbidden!");
                return;
              }
            }

            if (contact == null) {
              log.error("XMPPContact should not be null at this point!"); // $NON-NLS-1$
              return;
            }

            Shell shell = SWTUtils.getShell();

            assert shell != null
                : "Action should not be run if the display is disposed"; //$NON-NLS-1$

            String message =
                MessageFormat.format(
                    Messages.RenameContactAction_rename_message, contact.getDisplayableNameLong());

            InputDialog dialog =
                new InputDialog(
                    shell,
                    Messages.RenameContactAction_new_nickname_dialog_title,
                    message,
                    contact.getNickname().orElse(""),
                    null);

            if (dialog.open() == Window.OK) {
              contactsService.renameContact(contact, dialog.getValue());
            }
          }
        });
  }

  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
    connectionHandler.removeConnectionStateListener(connectionListener);
  }
}
