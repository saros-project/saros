package de.fu_berlin.inf.dpp.communication.muc.session.states;

import java.util.ArrayList;
import java.util.List;
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
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Handles the one's own {@link ChatState} of a particular {@link MultiUserChat}
 * and the propagation of incoming {@link ChatState} changes. . This class
 * manages both the packet extensions and the response necessary for compliance
 * with XEP-0085.
 * 
 * @author mariaspg
 * @author bkahlert
 */
public class MUCStateManager {
    private static final Logger log = Logger.getLogger(MUCStateManager.class);
    protected static final Map<MultiUserChat, MUCStateManager> managers = new WeakHashMap<MultiUserChat, MUCStateManager>();

    /**
     * Official name for the chat states feature
     * 
     * @see <a href="http://xmpp.org/registrar/namespaces.html">Jabber/XMPP
     *      Protocol Namespaces</a>
     * @see <a href="http://xmpp.org/extensions/xep-0085.html">XEP-0085: Chat
     *      State Notifications</a>
     */
    private static final String CHATSTATES_FEATURE = "http://jabber.org/protocol/chatstates";

    /**
     * Returns the {@link MUCStateManager} associated to the
     * {@link MultiUserChat}. Creates one if it does not yet exist.
     * 
     * @param connection
     * @param muc
     * @return
     */
    public static MUCStateManager getInstance(final Connection connection,
        final MultiUserChat muc) {

        if (connection == null) {
            return null;
        }

        synchronized (managers) {
            MUCStateManager manager = managers.get(muc);
            if (manager == null) {
                manager = new MUCStateManager(connection, muc);
                managers.put(muc, manager);
            }
            return manager;
        }
    }

    protected Connection connection;
    protected MultiUserChat muc;
    protected ChatState lastState = null;
    protected List<IMUCStateListener> mucStateListeners = new ArrayList<IMUCStateListener>();

    /**
     * Checks every incoming {@link Message} for the {@link PacketExtension}
     * {@link MUCStateManager#CHATSTATES_FEATURE} and notifies all
     * {@link IMUCStateListener}s in case of a {@link ChatState} change.
     */
    protected PacketListener incomingMessageInterceptor = new PacketListener() {
        public void processPacket(Packet packet) {
            assert packet instanceof Message : "This interceptor is only intended to handle XMPP Messages";
            Message message = (Message) packet;
            PacketExtension extension = message
                .getExtension(CHATSTATES_FEATURE);

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

            notifyMUCStateChanged(
                JID.createFromServicePerspective(message.getFrom()), state);
        }
    };

    protected MUCStateManager(Connection connection, MultiUserChat muc) {
        log.setLevel(Level.TRACE);

        this.connection = connection;
        this.muc = muc;

        // intercepting incoming messages
        this.muc.addMessageListener(incomingMessageInterceptor);

        ServiceDiscoveryManager.getInstanceFor(connection).addFeature(
            CHATSTATES_FEATURE);
    }

    /**
     * Sets the current state of the provided chat. This method will send an
     * empty bodied Message packet with the state attached as a
     * {@link org.jivesoftware.smack.packet.PacketExtension}, iff the new chat
     * state is different than the last state.
     * 
     * @param newState
     *            the new state of the chat
     * @throws org.jivesoftware.smack.XMPPException
     *             when there is an error sending the message packet.
     */
    public void setState(ChatState newState) throws XMPPException {
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

    /**
     * Adds a {@link IMUCStateListener}
     * 
     * @param mucStateListener
     */
    public void addMUCStateListener(IMUCStateListener mucStateListener) {
        this.mucStateListeners.add(mucStateListener);
    }

    /**
     * Removes a {@link IMUCStateListener}
     * 
     * @param mucStateListener
     */
    public void removeMUCStateListener(IMUCStateListener mucStateListener) {
        this.mucStateListeners.remove(mucStateListener);
    }

    /**
     * Notify all {@link IMUCStateListener}s about a changed chat status
     */
    public void notifyMUCStateChanged(JID sender, ChatState state) {
        for (IMUCStateListener mucStateListener : this.mucStateListeners) {
            mucStateListener.stateChanged(sender, state);
        }
    }
}
