package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.communication.chat.single.SingleUserChatService;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.session.UserElement;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgets.chat.ChatRoomsComposite;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Inject;

public class OpenChatAction extends Action implements Disposable {

  public static final String ACTION_ID = OpenChatAction.class.getName();

  @Inject private XMPPConnectionService connectionService;

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
    Connection connection = connectionService.getConnection();
    if (connection == null) return;

    String localUser = connection.getUser();
    if (localUser == null) return;

    JID jid = getSelectedJID();
    if (jid == null) {
      return;
    }

    JID localJID = new JID(localUser);
    if (localJID.equals(jid)) return;

    chatRoomsComposite.openChat(chatService.createChat(jid), true);
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }

  private void updateEnablement() {
    Connection connection = connectionService.getConnection();
    if (connection == null) {
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
