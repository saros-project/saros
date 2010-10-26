package de.fu_berlin.inf.dpp;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;

import de.fu_berlin.inf.dpp.MessagingManager.ChatStatusListener;

/**
 * Handles chat state for all chats on a particular XMPPConnection. This class
 * manages both the packet extensions and the disco response necessary for
 * compliance with XEP-0085.
 * <p>
 * Note: {@link #getInstance(Connection, MultiUserChatManager)} needs to be
 * called in order for the listeners to be registered appropriately with the
 * connection. If this does not occur you will not receive the update
 * notifications.
 * 
 * @author mariaspg
 */
public class ChatStatusManager {

    private static final Logger log = Logger.getLogger(ChatStatusManager.class);

    private static final Map<Connection, ChatStatusManager> managers = new WeakHashMap<Connection, ChatStatusManager>();

    private MultiUserChatManager muc;

    /**
     * Returns the ChatStatusManager related to the Connection and it will
     * create one if it does not yet exist.
     * 
     * @param connection
     *            the connection to return the ChatStatusManager
     * @return the ChatStatusManager related the the connection.
     */
    public static ChatStatusManager getInstance(final Connection connection,
        final MultiUserChatManager muc) {

        if (connection == null) {
            return null;
        }

        // a ChatStatusManager for each connection
        synchronized (managers) {
            ChatStatusManager manager = managers.get(connection);
            if (manager == null) {
                manager = new ChatStatusManager(connection);
                manager.init(muc);
                managers.put(connection, manager);
            }

            return manager;
        }
    }

    private final Connection connection;

    private final IncomingMessageInterceptor incomingInterceptor = new IncomingMessageInterceptor();

    private ChatState lastState = null;

    private ChatStatusManager(Connection connection) {
        this.connection = connection;
        log.setLevel(Level.TRACE);
    }

    private void init(MultiUserChatManager muc) {
        this.muc = muc;

        // intercepting incoming messages
        this.muc.addMessageListener(incomingInterceptor);

        ServiceDiscoveryManager.getInstanceFor(connection).addFeature(
            "http://jabber.org/protocol/chatstates");
    }

    /**
     * Sets the current state of the provided chat. This method will send an
     * empty bodied Message packet with the state attached as a
     * {@link org.jivesoftware.smack.packet.PacketExtension}, if and only if the
     * new chat state is different than the last state.
     * 
     * @param newState
     *            the new state of the chat
     * @throws org.jivesoftware.smack.XMPPException
     *             when there is an error sending the message packet.
     */
    public void setCurrentState(ChatState newState, MultiUserChatManager muc)
        throws XMPPException {
        if (muc == null || newState == null) {
            throw new IllegalArgumentException("Arguments cannot be null.");
        }

        // last and new ChatState are the same
        if (this.lastState == newState) {
            return;
        }

        /*
         * Creates a Message with an empty body. A body is needed because the
         * Message.Type.groupchat requires one.
         */
        try {
            Message message = muc.createMessage();
            message.setBody("");
            ChatStateExtension extension = new ChatStateExtension(newState);
            message.addExtension(extension);
            muc.sendMessage(message);
        } catch (Exception e1) {
            log.debug("Couldn't send state (" + e1.getMessage() + ")");
        }

        this.lastState = newState;
    }

    /*
     * Calls the method stateChanged of classes listening via the
     * MultiUserChatManager which implement the ChatStateListener.
     */
    private void fireNewChatStatus(String sender, ChatState state) {
        // TODO stateChanged needs sender Information
        for (PacketListener listener : this.muc.getListeners()) {
            if (listener instanceof ChatStatusListener) {
                ((ChatStatusListener) listener).stateChanged(sender, state);
            }
        }
    }

    /**
     * Checks every incoming Message for PacketExtension
     * "http://jabber.org/protocol/chatstates" and calls fireNewChatStatus with
     * the sent ChatState.
     */
    private class IncomingMessageInterceptor implements PacketListener {

        public void processPacket(Packet packet) {
            Message message = (Message) packet;
            PacketExtension extension = message
                .getExtension("http://jabber.org/protocol/chatstates");

            if (extension == null) {
                return;
            }

            ChatState state;
            try {
                state = ChatState.valueOf(extension.getElementName());
            } catch (Exception ex) {
                return;
            }

            log.debug("Incoming Message from "
                + message.getFrom()
                + " with state: "
                + message.getExtension("http://jabber.org/protocol/chatstates")
                    .getElementName());

            fireNewChatStatus(message.getFrom(), state);
        }
    }
}
