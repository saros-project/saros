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
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class UserListExtension extends SessionDefaultPacketExtension {

    private static final String COUNT_KEY = "Count";
    private static final String USER_KEY = "User";
    private static final String USER_PERMISSION_KEY = "UserPermission";
    private static final String USER_COLOR_KEY = "UserColor";

    /**
     * Simply value object class containing all information describing a single
     * user in the user list.
     */
    public static class UserListEntry {
        public UserListEntry(JID jid, int colorID, Permission permission) {
            this.jid = jid;
            this.colorID = colorID;
            this.permission = permission;
        }

        protected JID jid;

        protected int colorID;

        protected Permission permission;

        public int getColorID() {
            return colorID;
        }

        public Permission getPermission() {
            return permission;
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
            String permission = USER_PERMISSION_KEY + count;
            String color = USER_COLOR_KEY + count;
            extension.setValue(id, participant.getJID().toString());
            extension.setValue(permission, participant.getPermission().toString());
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
            Permission permission = Permission.valueOf(userlistExtension
                .getValue(USER_PERMISSION_KEY + i));

            users.add(new UserListEntry(jid, colorID, permission));
        }

        userListReceived(sender, users);
    }

    public void userListReceived(JID sender, List<UserListEntry> userList) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}