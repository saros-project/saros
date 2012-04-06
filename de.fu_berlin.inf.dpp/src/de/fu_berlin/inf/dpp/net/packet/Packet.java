package de.fu_berlin.inf.dpp.net.packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Abstract base class for Saros packets. All implementing classed <b>must</b>
 * have a public no arguments constructor.
 */
public abstract class Packet {

    private PacketType type;

    private JID sender;

    private JID receiver;

    protected Packet(PacketType type) {
        this.type = type;
    }

    public PacketType getType() {
        return type;
    }

    public abstract void serialize(OutputStream out) throws IOException;

    public abstract void deserialize(InputStream in) throws IOException;

    /**
     * Returns the JID from which this packet was received.
     * 
     * @return the senders JID
     */
    public JID getSender() {
        return sender;
    }

    /**
     * Sets the JID of the sender.
     * 
     * @param sender
     *            the senders JID
     */
    public void setSender(JID sender) {
        this.sender = sender;
    }

    /**
     * Returns the JID from which this packet was send to.
     * 
     * @return the senders JID
     */

    public JID getReceiver() {
        return receiver;
    }

    /**
     * Sets the JID of the receiver.
     * 
     * @param receiver
     *            the JID of the receiver or <code>null</code> null if this
     *            packet should be broadcasted (includes the local user)
     */
    public void setReceiver(JID receiver) {
        this.receiver = receiver;
    }
}
