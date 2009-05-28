/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class InviteExtension extends SessionDefaultPacketExtension {

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
        int colorID) {

        DefaultPacketExtension extension = create();
        /*
         * TODO PROJECTNAME and DESCRIPTION have the same value and there isn't
         * a description for projects available.
         * 
         * Both arguments contain the shared project's name when this method is
         * called.
         */
        extension.setValue(PacketExtensionUtils.PROJECTNAME, projectName);
        extension.setValue(PacketExtensionUtils.DESCRIPTION, description);
        extension.setValue(PacketExtensionUtils.COLOR_ID, String
            .valueOf(colorID));

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

        invitationReceived(sender, sessionID, pName, desc, colorID);
    }

    public void invitationReceived(JID sender, String sessionID,
        String projectName, String description, int colorID) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}
