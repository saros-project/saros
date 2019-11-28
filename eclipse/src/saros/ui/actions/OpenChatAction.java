package saros.ui.actions;

import java.util.List;
import java.util.Objects;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import saros.SarosPluginContext;
import saros.communication.chat.single.SingleUserChatService;
import saros.communication.connection.ConnectionHandler;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.User;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.model.session.UserElement;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.ui.widgets.chat.ChatRoomsComposite;

public class OpenChatAction extends Action implements Disposable {

  public static final String ACTION_ID = OpenChatAction.class.getName();

  @Inject private ConnectionHandler connectionHandler;
  @Inject private SingleUserChatService chatService;

  private ChatRoomsComposite chatRoomsComposite;

  private ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  public OpenChatAction(ChatRoomsComposite chatRoomsComposite) {
    super(Messages.OpenChatAction_MenuItem);
    SarosPluginContext.initComponent(this);
    this.chatRoomsComposite = chatRoomsComposite;

    setId(ACTION_ID);
    setImageDescriptor(ImageManager.getImageDescriptor("icons/view16/chat_misc.png"));

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    updateEnablement();
  }

  @Override
  public void run() {
    JID localJID = connectionHandler.getLocalJID();
    JID jid = getSelectedJID();

    if (Objects.equals(localJID, jid)) return;

    chatRoomsComposite.openChat(chatService.createChat(jid), true);
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }

  private void updateEnablement() {
    if (!connectionHandler.isConnected()) {
      setEnabled(false);
      return;
    }

    if (getSelectedJID() != null) {
      setEnabled(true);
    }
  }

  private JID getSelectedJID() {
    List<UserElement> users =
        SelectionRetrieverFactory.getSelectionRetriever(UserElement.class).getSelection();
    List<JID> contacts = SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

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
