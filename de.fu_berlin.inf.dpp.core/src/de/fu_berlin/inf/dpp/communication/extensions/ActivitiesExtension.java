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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import de.fu_berlin.inf.dpp.activities.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.FolderMovedActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.NOPActivity;
import de.fu_berlin.inf.dpp.activities.PermissionActivity;
import de.fu_berlin.inf.dpp.activities.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.StopActivity;
import de.fu_berlin.inf.dpp.activities.StopFollowingActivity;
import de.fu_berlin.inf.dpp.activities.TargetedFileActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.User;
import java.util.List;
import org.jivesoftware.smack.packet.PacketExtension;

@XStreamAlias("ADOS")
public class ActivitiesExtension extends SarosSessionPacketExtension {

  public static final Provider PROVIDER = new Provider();

  @XStreamImplicit private final List<IActivity> activities;

  @XStreamAlias("seq")
  @XStreamAsAttribute
  private final int sequenceNumber;

  /**
   * Creates an object that can be transformed into a {@linkplain PacketExtension} using the
   * provider of this extension. All object parameters <b>must not be <code>null</code></b>. @Note
   * This constructor does not check for correctness of the input parameters.
   *
   * @param sessionID the session id the {@linkplain IActivity activities} belong to
   * @param activities the {@linkplain IActivity activities} that should be included in this
   *     extension
   * @param sequenceNumber the sequence number of the <b>first</b> {@linkplain IActivity activity}
   */
  public ActivitiesExtension(String sessionID, List<IActivity> activities, int sequenceNumber) {
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
   * Returns the sequence number of the first {@linkplain IActivity activities} in the list returned
   * from {@link #getActivities()}.
   *
   * <p>All other sequence numbers can be recalculated where the sequence number of the activity
   * from <code>getActivities().get(x)</code> is <code>getSequenceNumber() + x</code>.
   *
   * @return
   */
  public int getSequenceNumber() {
    return sequenceNumber;
  }

  /**
   * @JTourBusStop 5, Creating a new Activity type, XStream registration:
   *
   * <p>We use the XStream library to convert handy Java objects to easy-to-send XML string and vice
   * versa. To beautify this XML output, we make use of annotations (such as XStreamAlias or
   * XStreamAsAttribute).
   *
   * <p>To make a long story short: Just add your new activity type to the list below, so any
   * annotations you might want to use will be honored. And again, please remember the alphabet.
   *
   * <p>Since you now know about XStream annotations, you might want to go back to your new class
   * and add some of these? You can take a look at other activity classes for inspiration.
   */

  /** */
  public static class Provider extends SarosSessionPacketExtension.Provider<ActivitiesExtension> {
    private Provider() {
      super(
          "ados",
          ActivitiesExtension.class,

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
          FolderCreatedActivity.class,
          FolderDeletedActivity.class,
          FolderMovedActivity.class,
          JupiterActivity.class,
          NOPActivity.class,
          PermissionActivity.class,
          ProgressActivity.class,
          TargetedFileActivity.class,
          StartFollowingActivity.class,
          StopActivity.class,
          StopFollowingActivity.class,
          TextSelectionActivity.class,
          ViewportActivity.class);
    }
  }
}
