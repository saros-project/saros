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
package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.xstream.JIDConverter;

/**
 * A {@link PermissionActivityDataObject} indicates that a user has a new
 * {@link User.Permission}.
 */
@XStreamAlias("permissionActivity")
public class PermissionActivityDataObject extends AbstractActivityDataObject {

    @XStreamAsAttribute
    protected final Permission permission;

    @XStreamAsAttribute
    @XStreamConverter(JIDConverter.class)
    protected final JID affectedUser;

    /**
     * Creates a new PermissionActivityDataObject which indicates that the given
     * user should change into the given permission.
     */
    public PermissionActivityDataObject(JID source, JID affectedUser,
        Permission permission) {
        super(source);
        this.affectedUser = affectedUser;
        this.permission = permission;
    }

    @Override
    public String toString() {
        return "PermissionActivityDataObject(user:" + affectedUser
            + ",new permission:" + permission + ")";
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new PermissionActivity(sarosSession.getUser(source),
            sarosSession.getUser(affectedUser), permission);
    }
}
