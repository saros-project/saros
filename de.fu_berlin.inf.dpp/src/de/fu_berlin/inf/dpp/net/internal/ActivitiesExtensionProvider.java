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

import de.fu_berlin.inf.dpp.activities.AbstractActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.TimedActivity;

@Component(module = "net")
public class ActivitiesExtensionProvider extends
    XStreamExtensionProvider<TimedActivities> {

    public ActivitiesExtensionProvider() {
        super("activities", TimedActivities.class, AbstractActivity.class,
            EditorActivity.class, FileActivity.class, FolderActivity.class,
            RoleActivity.class, TextEditActivity.class,
            TextSelectionActivity.class, ViewportActivity.class,
            TimedActivity.class, JupiterActivity.class,
            JupiterVectorTime.class, DeleteOperation.class,
            InsertOperation.class, NoOperation.class, SplitOperation.class,
            TimestampOperation.class);
    }

    public PacketExtension create(String sessionID,
        List<TimedActivity> activities) {
        return create(new TimedActivities(sessionID,
            new ArrayList<TimedActivity>(activities)));
    }
}
