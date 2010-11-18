/**
 * 
 */
package de.fu_berlin.inf.dpp.communication.multiUserChat;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * A MultiUserChat is a conversation that takes place among many users in a
 * virtual room. A room could have many occupants with different affiliation and
 * roles. Possible affiliations are "owner", "admin", "member", and "outcast".
 * Possible roles are "moderator", "participant", and "visitor". Each role and
 * affiliation guarantees different privileges (e.g. Send messages to all
 * occupants, Kick participants and visitors, Grant voice, Edit member list,
 * etc.).
 * <p>
 * In contrast to the super class
 * {@link org.jivesoftware.smackx.muc.MultiUserChat} it is possible to get a
 * list of listeners which will be notified of any new messages and were added
 * as such via the method {@link #addMessageListener(PacketListener)}.
 * 
 * @author mariaspg
 */
public class MultiUserChatManager extends MultiUserChat {

    private List<PacketListener> listeners = new ArrayList<PacketListener>();

    /**
     * Creates a new multi user chat with the specified connection and room
     * name. Note: no information is sent to or received from the server until
     * you attempt to join the chat room. On some server implementations, the
     * room will not be created until the first person joins it.
     * 
     * Most XMPP servers use a sub-domain for the chat service (e.g.
     * chat.example.com for the XMPP server example.com). You must ensure that
     * the room address you're trying to connect to includes the proper chat
     * sub-domain.
     * 
     * @param connection
     *            the XMPP connection
     * @param room
     *            the name of the room in the form "roomName@service", where
     *            "service" is the hostname at which the multi-user chat service
     *            is running. Make sure to provide a valid JID.
     */
    public MultiUserChatManager(Connection connection, String room) {
        super(connection, room);
    }

    /**
     * Adds a packet listener that will be notified of any new messages in the
     * group chat. Only "group chat" messages addressed to this group chat will
     * be delivered to the listener. If you wish to listen for other packets
     * that may be associated with this group chat, you should register a
     * PacketListener directly with the Connection with the appropriate
     * PacketListener.
     * 
     * @param listener
     *            a packet listener.
     */
    @Override
    public void addMessageListener(PacketListener listener) {
        super.addMessageListener(listener);
        listeners.add(listener);
    }

    /**
     * Removes a packet listener that was being notified of any new messages in
     * the multi user chat. Only "group chat" messages addressed to this multi
     * user chat were being delivered to the listener.
     * 
     * @param listener
     *            a packet listener.
     */
    @Override
    public void removeMessageListener(PacketListener listener) {
        super.removeMessageListener(listener);
        listeners.remove(listener);
    }

    /**
     * Get all listeners which are listening to incoming messages on the
     * connection. The listener must call
     * {@link #addMessageListener(PacketListener)} before.
     * 
     * @return a list of type {@link PacketListener}
     */
    public List<PacketListener> getListeners() {
        return listeners;
    }
}
