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
package de.fu_berlin.inf.dpp.net.internal;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.AbstractActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumErrorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.PermissionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ProgressActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.VCSActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;

@Component(module = "net")
public class ActivitiesExtensionProvider extends
    XStreamExtensionProvider<TimedActivities> {

    public ActivitiesExtensionProvider() {
        super("activityDataObjects", TimedActivities.class,
            AbstractActivityDataObject.class, EditorActivityDataObject.class,
            FileActivityDataObject.class, FolderActivityDataObject.class,
            PermissionActivityDataObject.class,
            TextEditActivityDataObject.class,
            TextSelectionActivityDataObject.class,
            ViewportActivityDataObject.class, TimedActivityDataObject.class,
            JupiterActivityDataObject.class, JupiterVectorTime.class,
            DeleteOperation.class, InsertOperation.class, NoOperation.class,
            SplitOperation.class, TimestampOperation.class, JID.class,
            SPathDataObject.class, ChecksumActivityDataObject.class,
            ChecksumErrorActivityDataObject.class,
            ProgressActivityDataObject.class, VCSActivityDataObject.class);
    }

    public PacketExtension create(String sessionID,
        List<TimedActivityDataObject> activities) {
        return create(new TimedActivities(sessionID,
            new ArrayList<TimedActivityDataObject>(activities)));
    }
}
