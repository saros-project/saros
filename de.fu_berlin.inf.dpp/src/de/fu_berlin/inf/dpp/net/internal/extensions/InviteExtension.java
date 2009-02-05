/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class InviteExtension extends SessionDefaultPacketExtension {

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
    public PacketExtension create(String projectName,
            String description, int colorID) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensions.PROJECTNAME, projectName);
        extension.setValue(PacketExtensions.DESCRIPTION, description);
        extension.setValue(PacketExtensions.COLOR_ID, "" + colorID);

        return extension;
    }

    public static InviteExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(InviteExtension.class);
    }
}