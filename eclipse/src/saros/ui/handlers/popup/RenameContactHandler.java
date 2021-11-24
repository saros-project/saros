package saros.ui.handlers.popup;

import java.text.MessageFormat;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.util.ThreadUtils;

/** Renames the nickname of the selected roster entry. */
public class RenameContactHandler {

  public static final String ID = RenameContactHandler.class.getName();

  private static final Logger log = Logger.getLogger(RenameContactHandler.class);

  private IConnectionStateListener connectionListener = (state, error) -> updateEnablement();

  protected ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateEnablement();
        }
      };

  @Inject private ConnectionHandler connectionHandler;
  @Inject private XMPPContactsService contactsService;

  private MDirectMenuItem renameContactMenuItem;
  private ESelectionService selectionService;

  public RenameContactHandler() {
    SarosPluginContext.initComponent(this);
  }

  @PostConstruct
  public void postConstruct(
      EModelService modelService, ESelectionService selectionService, MPart sarosView) {
    this.selectionService = selectionService;

    MPopupMenu popupMenu = null;
    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }
    // Search for menu item renameContact
    MUIElement menuItem = modelService.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      renameContactMenuItem = (MDirectMenuItem) menuItem;
    }
    connectionHandler.addConnectionStateListener(connectionListener);

    selectionService.addSelectionListener(selectionListener);

    updateEnablement();
  }

  @CanExecute
  public boolean canExecute() {
    return getEnabledState();
  }

  protected void updateEnablement() {
    if (renameContactMenuItem == null) {
      return;
    }

    renameContactMenuItem.setEnabled(getEnabledState());
  }

  private boolean getEnabledState() {
    List<JID> contacts =
        SelectionUtils.getAdaptableObjects((ISelection) selectionService.getSelection(), JID.class);

    return connectionHandler.isConnected() && contacts.size() == 1;
  }

  @Execute
  public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection activeSelection) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            XMPPContact contact = null;
            List<XMPPContact> selectedRosterEntries =
                SelectionUtils.getAdaptableObjects(activeSelection, XMPPContact.class);

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

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
    connectionHandler.removeConnectionStateListener(connectionListener);
  }
}
