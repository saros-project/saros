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

import org.picocontainer.annotations.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

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
     * @param sPathDataObject
     *            a valid project-relative path or <code>null</code> if former
     *            resource should be deactivated.
     */
    public EditorActivityDataObject(JID source, Type type,
        @Nullable SPathDataObject sPathDataObject) {

        super(source, sPathDataObject);
        if ((type != Type.Activated) && (sPathDataObject == null)) {
            throw new IllegalArgumentException(
                "Null path for non-activation type editor activityDataObject given.");
        }

        this.type = type;
    }

    @Override
    public String toString() {
        return "EditorActivityDataObject(type:" + this.type + ",path:"
            + (this.path != null ? this.path : "no path") + ")";
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new EditorActivity(sarosSession.getUser(source), type,
            (path != null ? path.toSPath(sarosSession) : null));
    }
}
