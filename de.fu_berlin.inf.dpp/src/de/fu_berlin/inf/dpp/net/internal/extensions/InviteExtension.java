/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class InviteExtension extends SessionDefaultPacketExtension {

    protected static final String SAROS_VERSION = "SAROS_VERSION";

    public InviteExtension(SessionIDObservable sessionIDObservable) {
        super(sessionIDObservable, "invite");
    }

    /**
     * Creates the packet extension for new invitations.
     * 
     * @param description
     *            an informal text that will be shown with the invitation.
     * @return the packet extension.
     */
    public PacketExtension create(String projectName, String description,
        int colorID, Version version) {

        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensionUtils.PROJECTNAME, projectName);
        extension.setValue(PacketExtensionUtils.DESCRIPTION, description);
        extension.setValue(PacketExtensionUtils.COLOR_ID, String
            .valueOf(colorID));
        extension.setValue(SAROS_VERSION, version.toString());

        return extension;
    }

    @Override
    public void processMessage(JID sender, Message message) {
        DefaultPacketExtension inviteExtension = getExtension(message);

        String desc = inviteExtension
            .getValue(PacketExtensionUtils.DESCRIPTION);
        String pName = inviteExtension
            .getValue(PacketExtensionUtils.PROJECTNAME);
        String sessionID = inviteExtension
            .getValue(PacketExtensionUtils.SESSION_ID);
        int colorID = Integer.parseInt(inviteExtension
            .getValue(PacketExtensionUtils.COLOR_ID));
        String sarosVersion = inviteExtension.getValue(SAROS_VERSION);

        invitationReceived(sender, sessionID, pName, desc, colorID,
            sarosVersion);
    }

    public void invitationReceived(JID sender, String sessionID,
        String projectName, String description, int colorID, String sarosVersion) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }
}
