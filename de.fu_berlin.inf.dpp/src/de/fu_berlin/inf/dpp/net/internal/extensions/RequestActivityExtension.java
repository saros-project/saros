/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public abstract class RequestActivityExtension extends
    SessionDefaultPacketExtension {

    public RequestActivityExtension() {
        super("requestActivity");
    }

    public PacketExtension create(int timestamp, boolean andup) {
        DefaultPacketExtension extension = create();

        // TODO create string constants for the used keys
        extension.setValue("ID", String.valueOf(timestamp));

        if (andup) {
            extension.setValue("ANDUP", "true");
        }

        return extension;
    }

    /**
     * @return Returns a default implementation of this extension that does
     *         nothing in requestForSendingActivitiesReceived(...)
     */
    public static RequestActivityExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            RequestActivityExtension.class);
    }

    @Override
    public void processMessage(JID sender, Message message) {
        DefaultPacketExtension rae = RequestActivityExtension.getDefault()
            .getExtension(message);

        String sID = rae.getValue("ID");
        boolean andUp = rae.getValue("ANDUP") != null;

        if (sID == null)
            return;

        int timeStamp = (new Integer(sID)).intValue();

        requestForResendingActivitiesReceived(sender, timeStamp, andUp);

    }

    public abstract void requestForResendingActivitiesReceived(JID sender,
        int timeStamp, boolean andUp);
}