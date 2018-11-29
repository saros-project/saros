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
package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;

/** A PermissionActivity indicates that a {@link User} has a new {@link Permission}. */
@XStreamAlias("permissionActivity")
public class PermissionActivity extends AbstractActivity {

  @XStreamAsAttribute private final Permission permission;

  @XStreamAsAttribute private final User affectedUser;

  /**
   * Creates a new {@link PermissionActivity} which indicates that the given user should change into
   * the given {@link Permission}.
   */
  public PermissionActivity(User source, User affectedUser, Permission permission) {

    super(source);

    if (affectedUser == null) throw new IllegalArgumentException("affectedUser must not be null");

    this.affectedUser = affectedUser;
    this.permission = permission;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (affectedUser != null);
  }

  public User getAffectedUser() {
    return affectedUser;
  }

  public Permission getPermission() {
    return permission;
  }

  @Override
  public String toString() {
    return "PermissionActivity(user: "
        + getAffectedUser()
        + ", permission: "
        + getPermission()
        + ")";
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }
}
