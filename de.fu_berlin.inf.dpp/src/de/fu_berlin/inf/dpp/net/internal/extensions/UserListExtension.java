/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.Collection;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class UserListExtension extends SessionDefaultPacketExtension {

    public UserListExtension() {
        super("userList");
    }

    public PacketExtension create(Collection<User> list) {
        DefaultPacketExtension extension = create();

        int count = 0;
        for (User participant : list) {
            JID jid = participant.getJID();
            String id = "User" + count;
            String role = "UserRole" + count;
            String color = "UserColor" + count;
            extension.setValue(id, jid.toString());
            extension.setValue(role, participant.getUserRole().toString());
            extension.setValue(color, participant.getColorID() + "");
            count++;
        }

        return extension;
    }

    public static UserListExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            UserListExtension.class);
    }
}