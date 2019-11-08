package saros.session;

import java.util.*;
import saros.net.xmpp.JID;
import saros.preferences.IPreferenceStore;
import saros.preferences.PreferenceStore;

/**
 * A user is a representation of a person sitting in front of an eclipse instance for the use in one
 * Saros session.
 *
 * <p>A user object always has the following immutable characteristics: He/she has a favorite color,
 * is either host or client, is either local or remote and has fixed JID.
 *
 * <p>There is one user who is the host, all others are clients.
 *
 * <p>There is one local user representing the person in front of the current eclipse instance, all
 * others are remote users.
 *
 * <p>The public and mutable properties are the {@link User.Permission} and {@link #isInSession()}.
 *
 * @entityObject A user is a entity object, i.e. it can change over time.
 */
public class User {

  public enum Permission {
    WRITE_ACCESS,
    READONLY_ACCESS
  }

  private final JID jid;
  private final boolean isHost;
  private final boolean isLocal;
  private final IPreferenceStore preferences;


  @Deprecated private volatile Permission permission = Permission.WRITE_ACCESS;

  /* More flexible Permissions also known as Privilege */
  private Map<UserPrivilege.Keys, UserPrivilege> privileges;

  public Map<UserPrivilege.Keys, UserPrivilege> getPrivileges() {
    return this.privileges;
  }

  public void setPrivileges(Map<UserPrivilege.Keys, UserPrivilege> privileges) {
    this.privileges = privileges;
  }

  public void addPrivilege(UserPrivilege privilege) {
    this.privileges.put(privilege.getKey(), privilege);
  }
  // get any privileges value or false
  public boolean hasPrivilege(UserPrivilege.Keys privilege) {
    // System.out.println("3 - hasPrivilege() " + privilege + " : " +
    // this.privileges.containsKey(privilege));
    if (this.privileges.containsKey(privilege)) {
      return this.privileges.get(privilege).getValue();
    }
    return false;

  }

  // convenience functions to privilege values
  public boolean hasReadOnlyAccessPrivilege() {

    // System.out.println("3 - User.hasReadOnlyAccessPrivilege() " +
    // hasPrivilege(UserPrivilege.Privilege.READONLY_ACCESS));
    return hasPrivilege(UserPrivilege.Keys.SESSION_READONLY_ACCESS);
  }

  public boolean hasWriteAccessPrivilege() {
    return hasPrivilege(UserPrivilege.Keys.SESSION_WRITE_ACCESS);
  }

  public boolean hasShareDocumentPrivilege() {
    return hasPrivilege(UserPrivilege.Keys.SESSION_SHARE_DOCUMENT);
  }

  public boolean hasInvitePrivilege() {
    return hasPrivilege(UserPrivilege.Keys.SESSION_INVITE_USER);
  }

  public boolean hasGrantPermissionPrivilege() {
    return hasPrivilege(UserPrivilege.Keys.SESSION_GRANT_PERMISSION);
  }

  public boolean hasJoinSessionPrivilege() {
    return hasPrivilege(UserPrivilege.Keys.SESSION_JOIN);
  }

  public boolean hasStartSessionServerPrivilege() {
    return hasPrivilege(UserPrivilege.Keys.SESSION_START_SERVER);
  }

  public boolean hasStopSessionServerPrivilege() {
    return hasPrivilege(UserPrivilege.Keys.SESSION_STOP_SERVER);
  }

  public boolean hasDeleteSessionDataPrivilege() {
    return hasPrivilege(UserPrivilege.Keys.SESSION_DELETE_DATA);
  }

  public boolean hasConfigureServerPrivilege() {
    return hasPrivilege(UserPrivilege.Keys.CONFIGURE_SERVER);

  }

  private volatile boolean isInSession;

  public User(JID jid, boolean isHost, boolean isLocal, IPreferenceStore preferences) {
    this.jid = jid;
    this.isHost = isHost;
    this.isLocal = isLocal;


    if (preferences == null) {
      this.preferences = new PreferenceStore();
    } else {
      this.preferences = preferences;
    }

    this.privileges = new HashMap<UserPrivilege.Keys, UserPrivilege>();

  }

  /**
   * @deprecated Will be replaced. Do not use this method in new code.
   * @return
   */
  @Deprecated
  public JID getJID() {
    return jid;
  }

  /**
   * set the current user {@link User.Permission} of this user inside the current project.
   *
   * @param permission
   */
  public void setPermission(Permission permission) {
    this.permission = permission;
  }

  /**
   * Gets current project {@link User.Permission} of this user.
   *
   * @return
   */
  public Permission getPermission() {
    return permission;
  }

  /**
   * Utility method to determine whether this user has {@link User.Permission#WRITE_ACCESS}
   *
   * @return <code>true</code> if this User has {@link User.Permission#WRITE_ACCESS}, <code>false
   *     </code> otherwise.
   *     <p>This is always !{@link #hasReadOnlyAccess()}
   */
  public boolean hasWriteAccess() {
    return permission == Permission.WRITE_ACCESS;
  }

  /**
   * Utility method to determine whether this user has {@link User.Permission#READONLY_ACCESS}
   *
   * @return <code>true</code> if this User has {@link User.Permission#READONLY_ACCESS}, <code>false
   *     </code> otherwise.
   *     <p>This is always !{@link #hasWriteAccess()}
   */
  public boolean hasReadOnlyAccess() {
    return permission == Permission.READONLY_ACCESS;
  }

  /**
   * Checks if the user is still part of the session. A user object that is no longer part of the
   * session will <b>never</b> be part of the session again. In other words: userA.equals(userB) <=>
   * true but userA == userB <=> false.
   *
   * @return <code>true</code> if the user is part of the session, <code>false</code> otherwise
   */
  public boolean isInSession() {
    return isInSession;
  }

  @Override
  public String toString() {
    return jid.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((jid == null) ? 0 : jid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    User other = (User) obj;
    if (jid == null) {
      if (other.jid != null) return false;
    } else if (!jid.equals(other.jid)) return false;
    return true;
  }

  public int getColorID() {
    return preferences.getInt(ColorNegotiationHook.KEY_INITIAL_COLOR);
  }

  public int getFavoriteColorID() {
    return preferences.getInt(ColorNegotiationHook.KEY_FAV_COLOR);
  }

  /**
   * Returns true if this User object identifies the user which is using the local Eclipse instance
   * as opposed to the remote users in different Eclipse instances.
   */
  public boolean isLocal() {
    return isLocal;
  }

  /** Returns true if this User is not the local user. */
  public boolean isRemote() {
    return !isLocal();
  }

  /**
   * Returns true if this user is the one that initiated the SarosSession session and thus is
   * responsible for synchronization, {@link User.Permission} management,
   */
  public boolean isHost() {
    return isHost;
  }

  /** Returns true if this user is not the host. */
  public boolean isClient() {
    return !isHost();
  }

  /** FOR INTERNAL USE ONLY */
  public void setInSession(boolean isInSession) {
    this.isInSession = isInSession;
  }

  public IPreferenceStore getPreferences() {
    return preferences;
  }
}
