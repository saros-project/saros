package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.ArrayList;
import java.util.Collection;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;

@Component(module = "net")
public class UserListExtension extends SarosSessionPacketExtension {
    // TODO: send the Collection<User>
    // It causes a marshallation exception... why??

    /*
     * Stefan Rossbach: because a user object contains a SarosSession object.
     * That object has so many references to other objects that may not be
     * serializeable. Luckily you got the exception instead of sending approx.
     * 50 MB serialized data per user object !
     */

    public ArrayList<UserListEntry> userList = new ArrayList<UserListEntry>();

    public UserListExtension(String sessionID, Collection<User> users) {
        super(sessionID);
        for (User user : users) {
            UserListEntry newUser = new UserListEntry(user.getJID(),
                user.getColorID(), user.getPermission(),
                user.isInvitationComplete());
            userList.add(newUser);
        }
    }

    public static class UserListEntry {
        public JID jid;
        public int colorID;
        public Permission permission;
        public boolean invitationComplete;

        public UserListEntry(JID jid, int colorID, Permission permission,
            boolean invitationComplete) {
            this.jid = jid;
            this.colorID = colorID;
            this.permission = permission;
            this.invitationComplete = invitationComplete;
        }
    }

    public static class Provider extends
        XStreamExtensionProvider<UserListExtension> {

        public Provider() {
            super("userList", UserListExtension.class);
        }
    }

}
