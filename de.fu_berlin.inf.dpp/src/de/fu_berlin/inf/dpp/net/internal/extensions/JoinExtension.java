/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class JoinExtension extends SessionDefaultPacketExtension {

    public JoinExtension() {
        super("join");
    }

    public PacketExtension create(int colorID) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensions.COLOR_ID, "" + colorID);
        return extension;
    }

    public static JoinExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(JoinExtension.class);
    }
}