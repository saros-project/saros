package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * A Saros Packet Extension is responsible for converting between the network
 * component (XMPPTransmitter) and the business logic
 */
public abstract class SarosDefaultPacketExtension implements PacketListener {

    protected String element;

    public SarosDefaultPacketExtension(String element) {
        this.element = element;
    }

    /**
     * Dispatch all Packets that pass the filter to the processMessage method,
     * because we always work with Messages.
     */
    public void processPacket(Packet packet) {
        if (!getFilter().accept(packet))
            return;

        processMessage(new JID(packet.getFrom()), (Message) packet);
    }

    /**
     * Every subclass that represents a PackageExtension is supposed to
     * implement this method by unpacking the data in message an calling a
     * method that subclasses in PacketExtensions can implement.
     */
    public abstract void processMessage(JID sender, Message message);

    public PacketFilter getFilter() {
        return new PacketExtensionFilter(element,
            PacketExtensionUtils.NAMESPACE);
    }

    public DefaultPacketExtension create() {
        DefaultPacketExtension extension = new DefaultPacketExtension(element,
            PacketExtensionUtils.NAMESPACE);
        return extension;
    }

    public DefaultPacketExtension getExtension(Message message) {
        return (DefaultPacketExtension) message.getExtension(element,
            PacketExtensionUtils.NAMESPACE);
    }
}