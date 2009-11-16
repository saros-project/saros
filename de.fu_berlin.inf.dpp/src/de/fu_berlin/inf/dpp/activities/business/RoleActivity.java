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

import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.RoleActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * A role activityDataObject indicates that a user has a new Role in the
 * Driver/Observer schemes of things.
 */
public class RoleActivity extends AbstractActivity {

    protected final UserRole role;
    protected final JID affectedUser;

    /**
     * Creates a new RoleActivity which indicates that the given user should
     * change into the given role.
     */
    public RoleActivity(JID source, JID affectedUser, UserRole role) {
        super(source);
        this.affectedUser = affectedUser;
        this.role = role;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
            + ((affectedUser == null) ? 0 : affectedUser.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof RoleActivity))
            return false;
        RoleActivity other = (RoleActivity) obj;
        if (affectedUser == null) {
            if (other.affectedUser != null)
                return false;
        } else if (!affectedUser.equals(other.affectedUser))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        return true;
    }

    public JID getAffectedUser() {
        return affectedUser;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "RoleActivity(user:" + this.getAffectedUser() + ",new role:"
            + this.getRole() + ")";
    }

    public boolean dispatch(IActivityConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject() {
        return new RoleActivityDataObject(source, affectedUser, role);
    }
}
