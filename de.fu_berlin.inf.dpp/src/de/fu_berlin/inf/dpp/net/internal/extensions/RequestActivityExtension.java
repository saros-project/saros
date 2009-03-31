/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public abstract class RequestActivityExtension extends
    SessionDefaultPacketExtension {

    private static Logger log = Logger.getLogger(RequestActivityExtension.class
        .getName());

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
        DefaultPacketExtension extension = RequestActivityExtension
            .getDefault().getExtension(message);

        String tmp = extension.getValue("ID");
        boolean andUp = extension.getValue("ANDUP") != null;

        if (tmp == null) {
            log.error("No sequence number in request.");
            return;
        }

        int sequenceNumber = Integer.parseInt(tmp);
        requestForResendingActivitiesReceived(sender, sequenceNumber, andUp);

    }

    public abstract void requestForResendingActivitiesReceived(JID sender,
        int timeStamp, boolean andUp);
}