/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class RequestActivityExtension extends SessionDefaultPacketExtension {

    private static Logger log = Logger.getLogger(RequestActivityExtension.class
        .getName());

    public RequestActivityExtension(SessionIDObservable sessionIDObservable) {
        super(sessionIDObservable, "requestActivity");
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

    @Override
    public void processMessage(JID sender, Message message) {
        DefaultPacketExtension extension = getExtension(message);

        String tmp = extension.getValue("ID");
        boolean andUp = extension.getValue("ANDUP") != null;

        if (tmp == null) {
            log.error("No sequence number in request.");
            return;
        }

        int sequenceNumber = Integer.parseInt(tmp);
        requestForResendingActivitiesReceived(sender, sequenceNumber, andUp);

    }

    public void requestForResendingActivitiesReceived(JID sender,
        int timeStamp, boolean andUp) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}