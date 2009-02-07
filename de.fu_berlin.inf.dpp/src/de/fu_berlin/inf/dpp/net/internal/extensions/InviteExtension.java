/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public abstract class InviteExtension extends SessionDefaultPacketExtension {

    public InviteExtension() {
        super("invite");
    }

    /**
     * Creates the packet extension for new invitations.
     * 
     * @param description
     *            an informal text that will be shown with the invitation.
     * @return the packet extension.
     */
    public PacketExtension create(String sender, String description, int colorID) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensions.PROJECTNAME, sender);
        extension.setValue(PacketExtensions.DESCRIPTION, description);
        extension.setValue(PacketExtensions.COLOR_ID, String.valueOf(colorID));

        return extension;
    }

    /**
     * @return Returns a default implementation of this extension that does
     *         nothing in invitationReceived(...)
     */
    public static InviteExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            InviteExtension.class);
    }

    @Override
    public void processMessage(JID sender, Message message) {
        DefaultPacketExtension inviteExtension = getExtension(message);

        String desc = inviteExtension.getValue(PacketExtensions.DESCRIPTION);
        String pName = inviteExtension.getValue(PacketExtensions.PROJECTNAME);
        String sessionID = inviteExtension
            .getValue(PacketExtensions.SESSION_ID);
        int colorID = Integer.parseInt(inviteExtension
            .getValue(PacketExtensions.COLOR_ID));

        invitationReceived(sender, sessionID, pName, desc, colorID);
    }

    public abstract void invitationReceived(JID sender, String sessionID,
        String projectName, String description, int colorID);
}
