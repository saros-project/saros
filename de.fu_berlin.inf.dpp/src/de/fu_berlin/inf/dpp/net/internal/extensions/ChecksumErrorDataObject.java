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
package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;

/**
 * Data object for ChecksumsErrors
 */
@XStreamAlias("TimedActivities")
public class ChecksumErrorDataObject {

    @XStreamAsAttribute
    protected String sessionID;

    @XStreamImplicit
    protected List<SPathDataObject> paths;

    @XStreamAsAttribute
    protected boolean resolved;

    public String getSessionID() {
        return sessionID;
    }

    public List<SPathDataObject> getPaths() {
        return paths;
    }

    public boolean isResolved() {
        return resolved;
    }

    public ChecksumErrorDataObject(String sessionID,
        List<SPathDataObject> paths, boolean resolved) {
        this.sessionID = sessionID;
        this.paths = paths;
        this.resolved = resolved;
    }

    @Component(module = "net")
    public static class ChecksumErrorExtensionProvider extends
        XStreamExtensionProvider<ChecksumErrorDataObject> {

        public ChecksumErrorExtensionProvider() {
            super("checksumError", ChecksumErrorDataObject.class);
        }

        public PacketExtension create(String sessionID,
            List<SPathDataObject> paths, boolean resolved) {
            return create(new ChecksumErrorDataObject(sessionID, paths,
                resolved));
        }
    }
}
