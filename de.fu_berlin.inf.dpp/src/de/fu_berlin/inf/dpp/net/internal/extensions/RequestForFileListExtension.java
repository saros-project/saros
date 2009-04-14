/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public abstract class RequestForFileListExtension extends
    SessionDefaultPacketExtension {

    public RequestForFileListExtension() {
        super("requestList");
    }

    @Override
    public DefaultPacketExtension create() {
        return super.create();
    }

    /**
     * @return Returns a default implementation of this extension that does
     *         nothing in requestForFileListReceived(...)
     */
    public static RequestForFileListExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            RequestForFileListExtension.class);
    }

    @Override
    public void processMessage(JID sender, Message message) {
        requestForFileListReceived(sender);
    }

    public abstract void requestForFileListReceived(JID sender);
}