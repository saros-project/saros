/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public abstract class LeaveExtension extends SessionDefaultPacketExtension {

    public LeaveExtension() {
        super("leave");
    }

    @Override
    public DefaultPacketExtension create() {
        return super.create();
    }

    /**
     * @return Returns a default implementation of this extension that does
     *         nothing in leaveReceived(...)
     */
    public static LeaveExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            LeaveExtension.class);
    }

    @Override
    public void processMessage(JID sender, Message message) {
        leaveReceived(sender);
    }

    public abstract void leaveReceived(JID sender);
}