/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class RequestActivityExtension extends SessionDefaultPacketExtension {

    public RequestActivityExtension() {
        super("requestActivity");
    }

    public PacketExtension create(int timestamp, boolean andup) {
        DefaultPacketExtension extension = create();

        extension.setValue("ID", (new Integer(timestamp)).toString());

        if (andup) {
            extension.setValue("ANDUP", "true");
        }

        return extension;
    }

    public static RequestActivityExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(RequestActivityExtension.class);
    }
}