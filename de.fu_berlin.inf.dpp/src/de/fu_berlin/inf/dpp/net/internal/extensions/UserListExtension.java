/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public abstract class UserListExtension extends SessionDefaultPacketExtension {

    public UserListExtension() {
        super("userList");
    }

    public PacketExtension create(Collection<User> userList) {
        DefaultPacketExtension extension = create();

        int count = 0;
        extension.setValue("Count", String.valueOf(userList.size()));
        for (User participant : userList) {
            JID jid = participant.getJID();
            String id = "User" + count;
            String role = "UserRole" + count;
            String color = "UserColor" + count;
            extension.setValue(id, jid.toString());
            extension.setValue(role, participant.getUserRole().toString());
            extension.setValue(color, String.valueOf(participant.getColorID()));
            count++;
        }

        return extension;
    }

    /**
     * @return Returns a default implementation of this extension that does
     *         nothing in userListReceived
     */
    public static UserListExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            UserListExtension.class);
    }

    @Override
    public void processMessage(JID sender, Message message) {

        DefaultPacketExtension userlistExtension = UserListExtension
            .getDefault().getExtension(message);

        List<User> result = new LinkedList<User>();

        int n = Integer.parseInt(userlistExtension.getValue("Count"));

        for (int i = 0; i < n; i++) {
            String jidS = userlistExtension.getValue("User" + i);
            if (jidS == null) {
                break;
            }
            JID jid = new JID(jidS);
            int colorID = Integer.parseInt(userlistExtension
                .getValue("UserColor" + i));

            // This user is new, we have to send him a message later
            // and add him to the project
            User user = new User(jid, colorID);

            String userRole = userlistExtension.getValue("UserRole" + i);
            user.setUserRole(UserRole.valueOf(userRole));

            result.add(user);
        }

        userListReceived(sender, result);
    }

    public abstract void userListReceived(JID sender, List<User> userList);

}