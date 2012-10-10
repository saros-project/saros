package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.communication.chat.single.SingleUserChatService;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.UserElement;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgets.session.ChatRoomsComposite;

public class OpenChatAction extends Action {

    @Inject
    private SarosNet sarosNet;

    @Inject
    private SingleUserChatService chatService;

    private ChatRoomsComposite chatRoomsComposite;

    private ISelectionListener selectionListener = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    public static final String ACTION_ID = OpenChatAction.class.getName();

    public OpenChatAction(ChatRoomsComposite chatRoomsComposite) {
        super(Messages.OpenChatAction_MenuItem);
        SarosPluginContext.initComponent(this);
        this.chatRoomsComposite = chatRoomsComposite;

        this.setImageDescriptor(ImageManager
            .getImageDescriptor("icons/view16/chat_misc.png"));

        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);

        updateEnablement();
    }

    @Override
    public void run() {
        Connection connection = sarosNet.getConnection();
        if (connection == null)
            return;

        String localUser = connection.getUser();
        if (localUser == null)
            return;

        JID jid = getSelectedJID();
        if (jid == null) {
            return;
        }

        JID localJID = new JID(localUser);
        if (localJID.equals(jid))
            return;

        chatRoomsComposite.openChat(chatService.createChat(jid), true);
    }

    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
    }

    private void updateEnablement() {
        Connection connection = sarosNet.getConnection();
        if (connection == null) {
            setEnabled(false);
            return;
        }

        if (getSelectedJID() != null) {
            setEnabled(true);
        }
    }

    private JID getSelectedJID() {
        List<UserElement> sessionBuddies = SelectionRetrieverFactory
            .getSelectionRetriever(UserElement.class).getSelection();
        List<JID> rosterBuddies = SelectionRetrieverFactory
            .getSelectionRetriever(JID.class).getSelection();

        if (sessionBuddies.size() + rosterBuddies.size() == 1) {
            if (sessionBuddies.size() == 1) {
                User user = (User) sessionBuddies.get(0).getUser();
                if (user == null) {
                    return null;
                }

                return user.getJID();
            } else {
                return rosterBuddies.get(0);
            }
        }

        return null;
    }

}
