package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.ArrayList;
import java.util.Collection;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;

@Component(module = "net")
public class UserListExtension extends SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();

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
                user.getColorID(), user.getFavoriteColorID(),
                user.getPermission());
            userList.add(newUser);
        }
    }

    public static class UserListEntry {
        public JID jid;
        public int colorID;
        public int favoriteColorID;
        public Permission permission;

        public UserListEntry(JID jid, int colorID, int favoriteColorID,
            Permission permission) {
            this.jid = jid;
            this.colorID = colorID;
            this.favoriteColorID = favoriteColorID;
            this.permission = permission;
        }
    }

    public static class Provider extends
        SarosSessionPacketExtension.Provider<UserListExtension> {

        private Provider() {
            super("userList", UserListExtension.class);
        }
    }

}
