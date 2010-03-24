package de.fu_berlin.inf.dpp.net.internal;

import java.util.ArrayList;
import java.util.Collection;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class UserListInfo extends DefaultInvitationInfo {
    // TODO: send the Collection<User>
    // It causes a marshallation exception... why??
    public ArrayList<UserListEntry> userList = new ArrayList<UserListEntry>();

    public UserListInfo(SessionIDObservable sessionID, String invitationID,
        Collection<User> users) {
        super(sessionID, invitationID);
        for (User user : users) {
            UserListEntry newUser = new UserListEntry(user.getJID(), user
                .getColorID(), user.getUserRole(), user.isInvitationComplete());
            userList.add(newUser);
        }
    }

    public static class UserListEntry {
        public JID jid;
        public int colorID;
        public UserRole userRole;
        public boolean invitationComplete;

        public UserListEntry(JID jid, int colorID, UserRole role,
            boolean invitationComplete) {
            this.jid = jid;
            this.colorID = colorID;
            this.userRole = role;
            this.invitationComplete = invitationComplete;
        }
    }

    public static class JoinExtensionProvider extends
        XStreamExtensionProvider<UserListInfo> {

        public JoinExtensionProvider() {
            super("userListInfo", UserListInfo.class);
        }
    }

}
