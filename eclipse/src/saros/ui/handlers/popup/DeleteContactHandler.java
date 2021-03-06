package saros.ui.handlers.popup;

import java.text.MessageFormat;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
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
import saros.util.ThreadUtils;

public class DeleteContactHandler {

  public static final String ID = DeleteContactHandler.class.getName();

  private static final Logger log = Logger.getLogger(DeleteContactHandler.class);

  protected IConnectionStateListener connectionListener = (state, error) -> updateEnablement();

  protected ISelectionListener selectionListener = (part, selection) -> updateEnablement();

  @Inject private ConnectionHandler connectionHandler;
  @Inject private XMPPContactsService contactsService;
  @Inject private ISarosSessionManager sessionManager;

  private MDirectMenuItem deleteContact;
  private ESelectionService selectionService;

  private boolean workbenchIsClosing = false;

  protected final String DELETE_ERROR_IN_SESSION =
      Messages.DeleteContactAction_delete_error_in_session;

  public DeleteContactHandler() {
    SarosPluginContext.initComponent(this);

    connectionHandler.addConnectionStateListener(connectionListener);
  }

  @PostConstruct
  public void postConstruct(
      MPart sarosView,
      EModelService modelService,
      ESelectionService selectionService,
      IEventBroker eb) {
    this.selectionService = selectionService;
    MPopupMenu popupMenu = null;

    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }
    MUIElement menuItem = modelService.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      deleteContact = (MDirectMenuItem) menuItem;
    }

    eb.subscribe(
        UILifeCycle.APP_SHUTDOWN_STARTED,
        new EventHandler() {

          @Override
          public void handleEvent(Event event) {
            workbenchIsClosing = true;
          }
        });

    selectionService.addSelectionListener(selectionListener);

    updateEnablement();
  }

  @CanExecute
  public boolean canExecute() {
    return getEnabledState();
  }

  private boolean getEnabledState() {
    List<JID> contacts =
        SelectionUtils.getAdaptableObjects((ISelection) selectionService.getSelection(), JID.class);

    return connectionHandler.isConnected() && contacts.size() == 1;
  }

  protected void updateEnablement() {
    if (deleteContact == null) {
      return;
    }
    try {
      deleteContact.setEnabled(getEnabledState());
    } catch (NullPointerException e) {
      deleteContact.setEnabled(false);
    } catch (Exception e) {
      if (!workbenchIsClosing)
        log.error("Unexpected error while updating enablement", e); // $NON-NLS-1$
    }
  }

  /** @review runSafe OK */
  @Execute
  public void run() {
    ThreadUtils.runSafeSync(
        log,
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
        SelectionUtils.getAdaptableObjects(
            (ISelection) selectionService.getSelection(), XMPPContact.class);
    if (selectedRosterEntries.size() == 1) {
      contact = selectedRosterEntries.get(0);
    }

    if (contact == null) {
      log.error("XMPPContact should not be null at this point!"); // $NON-NLS-1$
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

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
    connectionHandler.removeConnectionStateListener(connectionListener);
  }
}
