/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SarosDefaultPackageExtension;

public class CancelInviteExtension extends SarosDefaultPackageExtension {

    public CancelInviteExtension() {
        super("cancelInvite");
    }

    public DefaultPacketExtension create(String sessionID, String error) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensions.SESSION_ID, sessionID);

        if ((error != null) && (error.length() > 0)) {
            extension.setValue(PacketExtensions.ERROR, error);
        }
        return extension;
    }

    public static CancelInviteExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(CancelInviteExtension.class);
    }
}