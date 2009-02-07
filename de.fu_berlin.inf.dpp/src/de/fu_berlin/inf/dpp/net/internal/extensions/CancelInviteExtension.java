/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SarosDefaultPacketExtension;

public abstract class CancelInviteExtension extends SarosDefaultPacketExtension {

    public static final String ERROR = "CI_ERROR";

    public CancelInviteExtension() {
        super("cancelInvite");
    }

    public DefaultPacketExtension create(String sessionID, String error) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensions.SESSION_ID, sessionID);

        if ((error != null) && (error.length() > 0)) {
            extension.setValue(ERROR, error);
        }
        return extension;
    }

    /**
     * Returns a version of CancelInviteExtension that does nothing
     * 
     * @return
     */
    public static CancelInviteExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            CancelInviteExtension.class);
    }

    @Override
    public void processMessage(JID sender, Message message) {

        DefaultPacketExtension cancelInviteExtension = getExtension(message);

        String errorMsg = cancelInviteExtension.getValue(ERROR);

        invitationCanceled(sender, errorMsg);
    }

    public abstract void invitationCanceled(JID sender, String errorMessage);
}