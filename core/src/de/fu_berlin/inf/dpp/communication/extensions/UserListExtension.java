package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias(/* UserListStatusUpdate */ "ULSUP")
public class UserListExtension extends SarosSessionPacketExtension {

  public static final Provider PROVIDER = new Provider();

  private ArrayList<UserListEntry> userList = new ArrayList<UserListEntry>();

  public UserListExtension(String sessionID) {
    super(sessionID);
  }

  public void addUser(User user, long flags) {
    userList.add(UserListEntry.create(user, flags));
  }

  public List<UserListEntry> getEntries() {
    return userList;
  }

  public static class UserListEntry {
    public static final long USER_ADDED = 0x1L;
    public static final long USER_REMOVED = 0x2L;

    public long flags;
    public JID jid;
    public int colorID;
    public int favoriteColorID;
    public Permission permission;

    private static UserListEntry create(User user, long flags) {
      return new UserListEntry(
          user.getJID(), user.getColorID(), user.getFavoriteColorID(), user.getPermission(), flags);
    }

    private UserListEntry(
        final JID jid,
        final int colorID,
        final int favoriteColorID,
        final Permission permission,
        final long flags) {
      this.jid = jid;
      this.colorID = colorID;
      this.favoriteColorID = favoriteColorID;
      this.permission = permission;
      this.flags = flags;
    }
  }

  public static class Provider extends SarosSessionPacketExtension.Provider<UserListExtension> {

    private Provider() {
      super("ulsup", UserListExtension.class);
    }
  }
}
