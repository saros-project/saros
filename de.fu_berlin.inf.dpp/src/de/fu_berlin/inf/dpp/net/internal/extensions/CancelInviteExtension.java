/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class CancelInviteExtension extends SessionDefaultPacketExtension {

    public static final String ERROR = "CI_ERROR";

    public CancelInviteExtension(SessionIDObservable sessionID) {
        super(sessionID, "cancelInvite");
    }

    public DefaultPacketExtension create(String sessionID, String error) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensionUtils.SESSION_ID, sessionID);

        if ((error != null) && (error.length() > 0)) {
            extension.setValue(ERROR, error);
        }
        return extension;
    }

    @Override
    public void processMessage(JID sender, Message message) {

        DefaultPacketExtension cancelInviteExtension = getExtension(message);

        String errorMsg = cancelInviteExtension.getValue(ERROR);

        invitationCanceledReceived(sender, errorMsg);
    }

    public void invitationCanceledReceived(JID sender, String errorMessage) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}