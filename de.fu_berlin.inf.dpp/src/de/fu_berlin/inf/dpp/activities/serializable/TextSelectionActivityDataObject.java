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

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.TextSelectionActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("textSelectionActivity")
public class TextSelectionActivityDataObject extends
    AbstractProjectActivityDataObject {

    @XStreamAsAttribute
    private final int offset;

    @XStreamAsAttribute
    private final int length;

    public TextSelectionActivityDataObject(JID source, int offset, int length,
        SPathDataObject path) {
        super(source, path);
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        this.offset = offset;
        this.length = length;
    }

    @Override
    public String toString() {
        return "TextSelectionActivityDataObject(offset:" + this.offset
            + ",length:" + this.length + ",src:" + getSource() + ",path:"
            + this.path + ")";
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new TextSelectionActivity(sarosSession.getUser(source), offset,
            length, path.toSPath(sarosSession));
    }
}
