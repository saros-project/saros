/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package saros.session;

import saros.net.xmpp.JID;

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

  private final boolean isHost;

  private final boolean isLocal;

  private final JID jid;

  private volatile int colorID;

  private final int favoriteColorID;

  private volatile Permission permission = Permission.WRITE_ACCESS;

  private volatile boolean isInSession;

  public User(JID jid, boolean isHost, boolean isLocal, int colorID, int favoriteColorID) {

    this.jid = jid;
    this.isHost = isHost;
    this.isLocal = isLocal;
    this.colorID = colorID;
    this.favoriteColorID = favoriteColorID;
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
    return colorID;
  }

  public int getFavoriteColorID() {
    return favoriteColorID;
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

  /**
   * FOR INTERNAL USE ONLY
   *
   * @param colorID
   * @deprecated this must only be called by the component that handles color changes
   */
  @Deprecated
  public void setColorID(int colorID) {
    this.colorID = colorID;
  }

  /** FOR INTERNAL USE ONLY */
  public void setInSession(boolean isInSession) {
    this.isInSession = isInSession;
  }
}
