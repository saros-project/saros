package de.fu_berlin.inf.dpp.communication.multiUserChat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager.CommunicationPreferences;

public class MultiUserChatSession implements IMultiUserChatSessionProvider,
    PacketListener, MessageListener, IMultiUserChatStatusListener {
    private final Logger log = Logger.getLogger(MultiUserChatSession.class
        .getName());

    private final List<ChatLine> history = new ArrayList<ChatLine>();

    /** The chat room corresponding to the current session. */
    private MultiUserChatManager muc;

    private Set<User> chatUsers = new HashSet<User>();

    protected ChatStatusManager chatStatusManager;

    protected Saros saros;
    protected ISarosSession sarosSession;
    protected CommunicationPreferences communicationPreferences;
    protected MessagingManager messagingManager;

    public MultiUserChatSession(Saros saros, ISarosSession sarosSession,
        CommunicationPreferences communicationPreferences,
        MessagingManager messagingManager) {
        this.saros = saros;
        this.sarosSession = sarosSession;
        this.communicationPreferences = communicationPreferences;
        this.messagingManager = messagingManager;
    }

    public List<ChatLine> getHistory() {
        return history;
    }

    public void processPacket(Packet packet) {
        log.debug("processPacket called");

        final Message message = (Message) packet;
        if (message.getBody() == null || message.getBody().equals("")) {
            return;
        }

        JID jid = getJID(message.getFrom());
        User user = sarosSession.getUser(jid);

        if (user == null)
            return;

        this.messagingManager.chatMessageAdded(user, message.getBody());
    }

    public void processMessage(Chat chat, Message message) {
        log.debug("processMessage called.");
        processPacket(message);
    }

    /**
     * @see de.fu_berlin.inf.dpp.communication.multiUserChat.IMultiUserChatSessionProvider
     */
    public void sendMessage(String text, ChatState state) {
        if (muc == null) {
            log.error("MUC does not exist");
            return;
        }

        try {
            /*
             * Sends a message if there is one. If a message was composed then
             * the ChatState is always active.
             */
            chatStatusManager.setCurrentState(
                (state == null) ? ChatState.active : state, muc);
            if (text != null) {
                Message msg = muc.createMessage();
                msg.setBody(text);
                muc.sendMessage(msg);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            addChatLine("error", "Couldn't send message (" + e1.getMessage()
                + ")");
        }
    }

    /**
     * Fired when the state of another user changes.
     * 
     * @see IMultiUserChatStatusListener
     */
    public void stateChanged(String sender, ChatState state) {
        log.debug("stateChanged got fired with state: " + state.toString());

        JID jid = getJID(sender);
        User user = sarosSession.getUser(jid);

        if (!chatUsers.contains(user) && state == ChatState.active) {
            chatUsers.add(user);
            /*
             * Manually notified on local user in connectMultiUserChat.
             */
            if (!user.equals(sarosSession.getLocalUser()))
                this.messagingManager.notifyChatJoined(user);
        }

        if (chatUsers.contains(user) && state == ChatState.gone) {
            chatUsers.remove(user);
            /*
             * TODO: Remove notification as soon as TODO in
             * disconnectMultiUserChat is fixed
             */
            this.messagingManager.notifyChatLeft(user);
        }

        this.messagingManager.chatStateUpdated(user, state);
    }

    private void addChatLine(String sender, String text) {
        ChatLine chatLine = new ChatLine();
        chatLine.sender = sender;
        chatLine.text = text;
        chatLine.date = new Date();

        this.history.add(chatLine);

        JID jid = getJID(sender);
        User user = sarosSession.getUser(jid);

        for (IMultiUserChatListener chatListener : this.messagingManager.chatListeners) {
            chatListener.messageReceived(user, text);
        }
    }

    /*
     * Gets User who sent the message.
     */
    private JID getJID(String sender) {
        return new JID(sender.substring(sender.indexOf('/') + 1,
            sender.length()));
    }

    /**
     * Creates and joins the chat room.
     * 
     * @param connection
     * @param user
     * @throws XMPPException
     */
    private void initMUC(Connection connection, String user)
        throws XMPPException {

        if (communicationPreferences == null)
            throw new IllegalStateException("No comPrefs found!");

        /* create room domain of current connection. */
        // JID(connection.getUser()).getDomain();
        String host = communicationPreferences.chatroom + "@"
            + communicationPreferences.chatserver;

        // Create a MultiUserChat using an XMPPConnection for a room
        MultiUserChatManager muc = new MultiUserChatManager(connection, host);

        // try to join to room
        boolean joined = false;
        try {
            muc.join(user, communicationPreferences.password);
            joined = true;
        } catch (XMPPException e) {
            log.debug(e);
        }

        boolean createdRoom = false;
        if (!joined) {
            try {
                // Create the room
                muc.create(user);
                createdRoom = true;
                // try to join to room
                muc.join(user, communicationPreferences.password);
            } catch (XMPPException e) {
                log.debug(e);
            }
        }

        if (createdRoom) {
            try {
                // Get the the room's configuration form
                Form form = muc.getConfigurationForm();

                // Create a new form to submit based on the original form
                Form submitForm = form.createAnswerForm();

                // Add default answers to the form to submit
                for (Iterator<FormField> fields = form.getFields(); fields
                    .hasNext();) {
                    FormField field = fields.next();
                    if (!FormField.TYPE_HIDDEN.equals(field.getType())
                        && (field.getVariable() != null)) {
                        // Sets the default value as the answer
                        submitForm.setDefaultAnswer(field.getVariable());
                    }
                }

                // set configuration, see XMPP Specs
                submitForm.setAnswer("muc#roomconfig_moderatedroom", false);
                submitForm.setAnswer("muc#roomconfig_publicroom", false);
                submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
                    true);
                submitForm.setAnswer("muc#roomconfig_roomsecret",
                    communicationPreferences.password);
                submitForm.setAnswer("muc#roomconfig_allowinvites", true);
                submitForm.setAnswer("muc#roomconfig_persistentroom", false);

                // Send the completed form (with default values) to the
                // server to configure the room
                muc.sendConfigurationForm(submitForm);
            } catch (XMPPException e) {
                log.debug(e);
            }
        }
        muc.addMessageListener(this);

        log.debug("MUC joined. Server: " + communicationPreferences.chatserver
            + " Room: " + communicationPreferences.chatroom + " Password "
            + communicationPreferences.password);

        this.muc = muc;
    }

    public void disconnect() {
        assert muc != null;
        if (muc.isJoined() && saros.getConnection().isConnected()) {
            muc.leave();
        }
        muc = null;
    }

    public void connect() throws XMPPException {
        XMPPConnection connection = saros.getConnection();
        String user = connection.getUser();
        initMUC(connection, user);
        // listen to changes of users ChatState
        chatStatusManager = ChatStatusManager.getInstance(connection, muc);
    }
}