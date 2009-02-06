/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class RequestForFileListExtension extends SessionDefaultPacketExtension {

    public RequestForFileListExtension() {
        super("requestList");
    }

    @Override
    public DefaultPacketExtension create() {
        return super.create();
    }

    public static RequestForFileListExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            RequestForFileListExtension.class);
    }

}