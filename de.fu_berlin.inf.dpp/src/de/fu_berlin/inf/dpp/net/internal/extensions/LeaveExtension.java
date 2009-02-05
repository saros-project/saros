/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class LeaveExtension extends SessionDefaultPacketExtension {

    public LeaveExtension() {
        super("leave");
    }

    @Override
    public DefaultPacketExtension create() {
        return super.create();
    }

    public static LeaveExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(LeaveExtension.class);
    }
}