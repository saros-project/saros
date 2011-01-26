package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

public class CancelProjectSharingExtension extends
    SessionDefaultPacketExtension {

    public static final String ERROR = "CPS_ERROR";

    public CancelProjectSharingExtension(SessionIDObservable sessionID) {
        super(sessionID, "cancelProjectSharing");
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

        projectSharingCanceledReceived(sender, errorMsg);
    }

    public void projectSharingCanceledReceived(JID sender, String errorMessage) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}
