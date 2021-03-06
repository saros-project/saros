package saros.ui.handlers.popup;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.viewers.ISelection;
import saros.SarosPluginContext;
import saros.communication.extensions.JoinSessionRequestExtension;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.util.selection.SelectionUtils;

/**
 * Action for requesting an invitation to a session from a contact.
 *
 * <p>This currently relies on the fact, that only Saros/S has a working JoinSessionRequestHandler.
 * To make this feature generic in the future we need to add another XMPP namespace
 */
public class RequestSessionInviteHandler {

  @Inject private ISarosSessionManager sessionManager;
  @Inject private ITransmitter transmitter;

  private ESelectionService selectionService;
  private MDirectMenuItem requestSessionInviteMenuItem;

  private ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateActionState();
        }
      };
  public static final String ID = RequestSessionInviteHandler.class.getName();

  public RequestSessionInviteHandler() {
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

    Object menuElement = modelService.find(ID, popupMenu);
    if (menuElement instanceof MDirectMenuItem) {
      requestSessionInviteMenuItem = (MDirectMenuItem) menuElement;
    }

    selectionService.addSelectionListener(selectionListener);
    updateActionState();
  }

  @Execute
  public void execute() {
    ISarosSession session = sessionManager.getSession();
    JID jid = getSelectedJID();
    if (session != null || jid == null) {
      return;
    }

    transmitter.sendPacketExtension(
        jid, JoinSessionRequestExtension.PROVIDER.create(new JoinSessionRequestExtension()));
  }

  private JID getSelectedJID() {
    List<JID> selected =
        SelectionUtils.getAdaptableObjects((ISelection) selectionService.getSelection(), JID.class);

    if (selected.size() != 1) return null;

    return selected.get(0);
  }

  private void updateActionState() {
    if (requestSessionInviteMenuItem == null) return;

    ISarosSession session = sessionManager.getSession();
    requestSessionInviteMenuItem.setEnabled(session == null && getSelectedJID() != null);
  }

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
  }
}
