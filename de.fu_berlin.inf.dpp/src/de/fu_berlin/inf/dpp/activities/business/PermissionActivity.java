/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.PermissionActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A {@link PermissionActivityDataObject} indicates that a user has a new
 * {@link User.Permission}.
 */
public class PermissionActivity extends AbstractActivity {

    protected final Permission permission;
    protected final User affectedUser;

    /**
     * Creates a new {@link PermissionActivity} which indicates that the given
     * user should change into the given {@link User.Permission}.
     */
    public PermissionActivity(User source, User affectedUser,
        Permission permission) {

        super(source);
        this.affectedUser = affectedUser;
        this.permission = permission;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
            + ((affectedUser == null) ? 0 : affectedUser.hashCode());
        result = prime * result
            + ((permission == null) ? 0 : permission.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PermissionActivity))
            return false;
        PermissionActivity other = (PermissionActivity) obj;
        if (affectedUser == null) {
            if (other.affectedUser != null)
                return false;
        } else if (!affectedUser.equals(other.affectedUser))
            return false;
        if (permission == null) {
            if (other.permission != null)
                return false;
        } else if (!permission.equals(other.permission))
            return false;
        return true;
    }

    public User getAffectedUser() {
        return affectedUser;
    }

    public Permission getPermission() {
        return permission;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(user:" + this.getAffectedUser()
            + ",new permission:" + this.getPermission() + ")";
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new PermissionActivityDataObject(source.getJID(),
            affectedUser.getJID(), permission);
    }
}
