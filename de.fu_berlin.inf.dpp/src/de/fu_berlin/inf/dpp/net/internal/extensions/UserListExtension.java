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

    private static final String COUNT_KEY = "Count";
    private static final String USER_KEY = "User";
    private static final String USER_ROLE_KEY = "UserRole";
    private static final String USER_COLOR_KEY = "UserColor";

    public UserListExtension() {
        super("userList");
    }

    public PacketExtension create(Collection<User> userList) {
        DefaultPacketExtension extension = create();

        int count = 0;
        extension.setValue(COUNT_KEY, String.valueOf(userList.size()));
        for (User participant : userList) {
            String id = USER_KEY + count;
            String role = USER_ROLE_KEY + count;
            String color = USER_COLOR_KEY + count;
            extension.setValue(id, participant.getJID().toString());
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

        List<User> users = new LinkedList<User>();

        int n = Integer.parseInt(userlistExtension.getValue(COUNT_KEY));

        for (int i = 0; i < n; i++) {
            JID jid = new JID(userlistExtension.getValue(USER_KEY + i));
            int colorID = Integer.parseInt(userlistExtension
                .getValue(USER_COLOR_KEY + i));

            User user = new User(jid, colorID);

            String userRole = userlistExtension.getValue(USER_ROLE_KEY + i);
            user.setUserRole(UserRole.valueOf(userRole));

            users.add(user);
        }

        userListReceived(sender, users);
    }

    public abstract void userListReceived(JID sender, List<User> userList);

}