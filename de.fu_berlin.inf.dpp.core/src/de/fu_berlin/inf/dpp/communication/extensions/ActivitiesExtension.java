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
package de.fu_berlin.inf.dpp.communication.extensions;

import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.business.NOPActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.business.RecoveryFileActivity;
import de.fu_berlin.inf.dpp.activities.business.ShareConsoleActivity;
import de.fu_berlin.inf.dpp.activities.business.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.business.StopActivity;
import de.fu_berlin.inf.dpp.activities.business.StopFollowingActivity;
import de.fu_berlin.inf.dpp.activities.business.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("ADOS")
public class ActivitiesExtension extends SarosSessionPacketExtension {

    public static final Provider PROVIDER = new Provider();

    @XStreamImplicit
    private final List<IActivity> activities;

    @XStreamAlias("seq")
    @XStreamAsAttribute
    private final int sequenceNumber;

    /**
     * Creates an object that can be transformed into a
     * {@linkplain PacketExtension} using the provider of this extension. All
     * object parameters <b>must not be <code>null</code></b>.
     * 
     * @Note This constructor does not check for correctness of the input
     *       parameters.
     * 
     * @param sessionID
     *            the session id the {@linkplain IActivity activities} belong to
     * @param activities
     *            the {@linkplain IActivity activities} that should be included
     *            in this extension
     * @param sequenceNumber
     *            the sequence number of the <b>first</b> {@linkplain IActivity
     *            activity}
     */
    public ActivitiesExtension(String sessionID, List<IActivity> activities,
        int sequenceNumber) {
        super(sessionID);
        this.activities = activities;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Returns the {@linkplain IActivity activities} included in this extension.
     * 
     * @return
     */
    public List<IActivity> getActivities() {
        return activities;
    }

    /**
     * Returns the sequence number of the first {@linkplain IActivity
     * activities} in the list returned from {@link #getActivities()}.
     * 
     * <p>
     * All other sequence numbers can be recalculated where the sequence number
     * of the activity from <code>getActivities().get(x)</code> is
     * <code>getSequenceNumber() + x</code>.
     * </p>
     * 
     * @return
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * @JTourBusStop 7, Activity creation, {@link IActivity} registration:
     * 
     *               All {@link IActivity} implementations should be registered
     *               with the XStream extensions provider, otherwise annotations
     *               like XStreamAlias will not be honored.
     */

    public static class Provider extends
        SarosSessionPacketExtension.Provider<ActivitiesExtension> {
        private Provider() {
            super("ados", ActivitiesExtension.class,

            // Misc
                JID.class,

                User.class,

                // Jupiter classes

                DeleteOperation.class,

                InsertOperation.class,

                JupiterVectorTime.class,

                NoOperation.class,

                SplitOperation.class,

                TimestampOperation.class,

                // SPATH
                SPath.class,

                // Activities
                ChangeColorActivity.class,

                ChecksumActivity.class,

                ChecksumErrorActivity.class,

                EditorActivity.class,

                FileActivity.class,

                FolderActivity.class,

                JupiterActivity.class,

                NOPActivity.class,

                PermissionActivity.class,

                ProgressActivity.class,

                RecoveryFileActivity.class,

                ShareConsoleActivity.class,

                StartFollowingActivity.class,

                StopActivity.class,

                StopFollowingActivity.class,

                TextSelectionActivity.class,

                VCSActivity.class,

                ViewportActivity.class);
        }
    }
}
