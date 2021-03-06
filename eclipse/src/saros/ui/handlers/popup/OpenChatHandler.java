package saros.ui.handlers.popup;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
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
import org.eclipse.jface.viewers.ISelection;
import saros.SarosPluginContext;
import saros.communication.chat.single.SingleUserChatService;
import saros.communication.connection.ConnectionHandler;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.User;
import saros.ui.model.session.UserElement;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.widgets.chat.ChatRoomsComposite;

public class OpenChatHandler {

  public static final String ID = OpenChatHandler.class.getName();

  @Inject private ConnectionHandler connectionHandler;
  @Inject private SingleUserChatService chatService;

  private ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateEnablement();
        }
      };

  private MDirectMenuItem openChatMenuItem;
  private ISelection selection;

  public OpenChatHandler() {
    SarosPluginContext.initComponent(this);
  }

  @PostConstruct
  public void postConstruct(
      ESelectionService selectionService,
      EModelService modelService,
      MPart sarosView,
      @Named(IServiceConstants.ACTIVE_SELECTION) ISelection activeSelection) {
    this.selection = activeSelection;

    MPopupMenu popupMenu = null;
    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }

    MUIElement menuItem = modelService.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      openChatMenuItem = (MDirectMenuItem) menuItem;
    }

    selectionService.addSelectionListener(selectionListener);

    updateEnablement();
  }

  @Execute
  public void execute() {
    JID localJID = connectionHandler.getLocalJID();
    JID jid = getSelectedJID();

    if (Objects.equals(localJID, jid)) return;

    Map<String, Object> data = openChatMenuItem.getTransientData();
    Object object = data.get("chatRoomsComposite");

    if (object instanceof ChatRoomsComposite) {
      ChatRoomsComposite chatRoomsComposite = (ChatRoomsComposite) object;
      chatRoomsComposite.openChat(chatService.createChat(jid), true);
    }
  }

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
  }

  @CanExecute
  public boolean canExecute() {
    return connectionHandler.isConnected() && getSelectedJID() != null;
  }

  private void updateEnablement() {
    if (openChatMenuItem == null) {
      return;
    }

    if (!connectionHandler.isConnected()) {
      openChatMenuItem.setEnabled(false);
      return;
    }

    if (getSelectedJID() != null) {
      openChatMenuItem.setEnabled(true);
    }
  }

  private JID getSelectedJID() {
    List<UserElement> users = SelectionUtils.getAdaptableObjects(selection, UserElement.class);
    List<JID> contacts = SelectionUtils.getAdaptableObjects(selection, JID.class);

    if (users.size() + contacts.size() == 1) {
      if (users.size() == 1) {
        User user = users.get(0).getUser();
        if (user == null) {
          return null;
        }

        return user.getJID();
      } else {
        return contacts.get(0);
      }
    }

    return null;
  }
}
