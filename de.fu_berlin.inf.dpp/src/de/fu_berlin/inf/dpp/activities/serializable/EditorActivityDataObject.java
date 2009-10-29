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

import org.eclipse.core.runtime.IPath;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity.Type;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * A text load activityDataObject activates a new resource. If the path is
 * <code>null</code> no resource is currently active.
 * 
 * @author rdjemili
 */
@XStreamAlias("editorActivity")
public class EditorActivityDataObject extends AbstractActivityDataObject {

    @XStreamAsAttribute
    protected final Type type;

    @XStreamAlias("editor")
    @XStreamAsAttribute
    protected final IPath path;

    /**
     * @param path
     *            a valid project-relative path or <code>null</code> if former
     *            resource should be deactivated.
     */
    public EditorActivityDataObject(JID source, Type type, IPath path) {
        super(source);
        if ((type != Type.Activated) && (path == null)) {
            throw new IllegalArgumentException(
                "Null path for non-activation type editor activityDataObject given.");
        }

        this.type = type;
        this.path = path;
    }

    /**
     * @return the project-relative path to the resource that should be
     *         activated.
     */
    public IPath getPath() {
        return this.path;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EditorActivityDataObject(type:" + this.type + ",path:"
            + (this.path != null ? this.path : "no path") + ")";
    }

    public boolean dispatch(IActivityDataObjectConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityDataObjectReceiver receiver) {
        receiver.receive(this);
    }

    public IActivity getActivity() {
        return new EditorActivity(source, type, path);
    }
}
