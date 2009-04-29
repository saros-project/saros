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
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

public class UserListExtension extends SessionDefaultPacketExtension {

    private static final String COUNT_KEY = "Count";
    private static final String USER_KEY = "User";
    private static final String USER_ROLE_KEY = "UserRole";
    private static final String USER_COLOR_KEY = "UserColor";

    /**
     * Simply value object class containing all information describing a single
     * user in the user list.
     */
    public static class UserListEntry {
        public UserListEntry(JID jid, int colorID, UserRole role) {
            this.jid = jid;
            this.colorID = colorID;
            this.role = role;
        }

        protected JID jid;

        protected int colorID;

        protected UserRole role;

        public int getColorID() {
            return colorID;
        }

        public UserRole getUserRole() {
            return role;
        }

        public JID getJID() {
            return jid;
        }
    }

    public UserListExtension(SessionIDObservable sessionIDObservable) {
        super(sessionIDObservable, "userList");
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

    @Override
    public void processMessage(JID sender, Message message) {

        DefaultPacketExtension userlistExtension = getExtension(message);

        List<UserListEntry> users = new LinkedList<UserListEntry>();

        int n = Integer.parseInt(userlistExtension.getValue(COUNT_KEY));

        for (int i = 0; i < n; i++) {
            JID jid = new JID(userlistExtension.getValue(USER_KEY + i));
            int colorID = Integer.parseInt(userlistExtension
                .getValue(USER_COLOR_KEY + i));
            UserRole userRole = UserRole.valueOf(userlistExtension
                .getValue(USER_ROLE_KEY + i));

            users.add(new UserListEntry(jid, colorID, userRole));
        }

        userListReceived(sender, users);
    }

    public void userListReceived(JID sender, List<UserListEntry> userList) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}