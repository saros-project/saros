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
package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * A text load activityDataObject activates a new resource. If the path is
 * <code>null</code> no resource is currently active.
 * 
 * @author rdjemili
 */
@XStreamAlias("editorActivity")
public class EditorActivityDataObject extends AbstractProjectActivityDataObject {

    @XStreamAsAttribute
    protected final Type type;

    /**
     * @param path
     *            a valid project-relative path or <code>null</code> if former
     *            resource should be deactivated.
     */
    public EditorActivityDataObject(User source, Type type, SPath path) {
        super(source, path);

        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(type);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof EditorActivityDataObject))
            return false;

        EditorActivityDataObject other = (EditorActivityDataObject) obj;

        if (!ObjectUtils.equals(this.type, other.type))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "EditorActivityDO(type: " + type + ", path: " + getPath() + ")";
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        return new EditorActivity(getSource(), type, getPath());
    }

    @Deprecated
    public Type getType() {
        return type;
    }
}
