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

import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Activity that for activating, closing, and saving editors. If the
 * {@link #getPath()} returns <code>null</code> then no resource is currently
 * active.
 * 
 * Saving is not document but editor specific because one editor might perform
 * changes on the document before actually saving while others just save. An
 * example is a Java editor with save actions enabled vs. a plain text editor
 * for the very same document.
 * 
 * @author rdjemili
 */
public class EditorActivity extends AbstractActivity {

    public static enum Type {
        Activated, Closed, Saved
    }

    protected final Type type;
    protected final SPath path;

    /**
     * @param path
     *            an {@link SPath} or type {@link Type#Activated} and
     *            <code>null</code> as path if there is no editor activated
     *            anymore.
     */
    public EditorActivity(User source, Type type, @Nullable SPath path) {

        super(source);
        if (path == null) {
            if (type != Type.Activated) {
                throw new IllegalArgumentException(
                    "Null path for non-activation type editor activityDataObject given.");
            }
        } else {
            if (path.getEditorType() == null) {
                throw new IllegalArgumentException("No editor ID set on "
                    + path + ".");
            }
        }

        this.type = type;
        this.path = path;
    }

    public SPath getPath() {
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
        if (!(obj instanceof EditorActivity))
            return false;
        EditorActivity other = (EditorActivity) obj;
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
        return "EditorActivity(type:" + this.type + ",path:"
            + (this.path != null ? this.path : "no path") + ")";
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new EditorActivityDataObject(source.getJID(), type,
            (path != null ? path.toSPathDataObject(sarosSession) : null));
    }
}
