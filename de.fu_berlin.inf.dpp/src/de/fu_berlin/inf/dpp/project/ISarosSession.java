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
package de.fu_berlin.inf.dpp.project;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.SubMonitor;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;

/**
 * A SarosSession consists of one or more shared projects, which are the central
 * concept of this plugin. They are associated with Eclipse projects and make
 * them available for synchronous/real-time collaboration.
 * 
 * TODO Is this interface really necessary?
 * 
 * @author rdjemili
 */
public interface ISarosSession extends IActivityListener {

    /**
     * @return a collection of all participants of the shared project. This
     *         collection includes the local user.
     */
    public Collection<User> getParticipants();

    /**
     * @return a snap-shot copy of all buddies.
     */
    public List<User> getRemoteUsers();

    /**
     * Initiates a {@link User.Permission} change. This method is called when
     * the user wants to change user {@link User.Permission}s via the UI.
     * 
     * @host This method may only called by the host.
     * @noSWT This method mustn't be called from the SWT UI thread
     * @param user
     *            The user which {@link User.Permission} has to be changed.
     * @blocking Returning after the {@link User.Permission} change is complete
     * @cancelable
     * 
     * @Throws CancellationException
     * @Throws InterruptedException
     */
    public void initiatePermissionChange(User user, Permission newPermission,
        SubMonitor progress) throws CancellationException, InterruptedException;

    /**
     * Set the {@link User.Permission} of the given user. This is called on
     * incoming activityDataObjects from the network.
     * 
     * @swt This method MUST to be called from the SWT UI thread
     * @param user
     *            the user which {@link User.Permission} has to be set.
     * @param permission
     *            The new {@link User.Permission} of the user.
     */
    public void setPermission(User user, Permission permission);

    /**
     * Sets the {@link User#invitationComplete} flag to true;
     * 
     * @param user
     *            The user whose invitation process has completed.
     */
    public void userInvitationCompleted(final User user);

    /**
     * @return <code>true</code> if the local client is a user with
     *         {@link User.Permission#WRITE_ACCESS} of this shared project.
     *         <code>false</code> otherwise.
     */
    public boolean hasWriteAccess();

    /**
     * The host is the person that initiated this SarosSession and holds all
     * original files.
     * 
     * @immutable This method will always return the same value for a session
     */
    public User getHost();

    /**
     * @return <code>true</code> if the local client is the host of this shared
     *         project. <code>false</code> otherwise.
     * 
     * @immutable This method will always return the same value for a session
     */
    public boolean isHost();

    /**
     * Adds the user.
     * 
     * @param user
     *            the user that is to be added.
     */
    public void addUser(User user);

    /**
     * Removes the user.
     * 
     * @param user
     *            the user that is to be removed.
     * 
     * @swt Must be called from SWT!
     */
    public void removeUser(User user);

    /**
     * Adds the given shared project listener. This call is ignored if the
     * listener is all a listener of this shared project.
     * 
     * @param listener
     *            The listener that is to be added.
     */
    public void addListener(ISharedProjectListener listener);

    /**
     * Removes the given shared project listener. This call is ignored if the
     * listener doesn't belongs to the current listeners of this shared project.
     * 
     * @param listener
     *            the listener that is to be removed.
     */
    public void removeListener(ISharedProjectListener listener);

    /**
     * @return the Eclipse projects associated with this shared project. This
     *         always returns a set non-null set, which contains at least one
     *         element.
     */
    public Set<IProject> getProjects();

    /**
     * @return the sequencer that is responsible for sending and receiving
     *         activityDataObjects.
     */
    public ActivitySequencer getSequencer();

    /**
     * Activates sending of activityDataObjects. The reason that this isn't done
     * automatically are unit tests.
     */
    public void start();

    /**
     * Deactivates sending of activityDataObjects.
     */
    public void stop();

    /**
     * Given a resource qualified JID, this method will return the user which
     * has the identical ID including resource.
     * 
     * Null is returned if no user is found with this JID (even if there is only
     * a resource mismatch).
     * 
     * Use getResourceQualifiedJID(JID) in the case if you do not know the
     * RQ-JID.
     */
    public User getUser(JID jid);

    /**
     * Given a JID (with resource or not), will return the resource qualified
     * JID associated with a User in this project or null if no user for the
     * given JID exists in this SarosSession.
     */
    public JID getResourceQualifiedJID(JID jid);

    /**
     * Returns the {@link User} who is representing the local eclipse instance
     * 
     * @immutable This method will always return the same value for a session
     */
    public User getLocalUser();

    /**
     * @return true, if there is exactly one user with
     *         {@link User.Permission#WRITE_ACCESS}, false otherwise.
     */
    public boolean hasExclusiveWriteAccess();

    /**
     * the concurrent document manager is responsible for all jupiter controlled
     * documents
     * 
     * @return the concurrent document manager
     */
    public ConcurrentDocumentServer getConcurrentDocumentServer();

    /**
     * the concurrent document manager is responsible for all jupiter controlled
     * documents
     * 
     * @return the concurrent document manager
     */
    public ConcurrentDocumentClient getConcurrentDocumentClient();

    /**
     * The Saros Plugin this SarosSession is running in.
     */
    public Saros getSaros();

    /**
     * Get a free color from the pool.
     * 
     * @return an unused color ID or a default ID if all color IDs are in use.
     */
    public int getFreeColor();

    /**
     * Returns a color to the pool of available colors.
     * 
     * @param colorID
     *            the color id that should be returned to the pool.
     */
    public void returnColor(int colorID);

    /**
     * Excutes the given activityDataObjects locally.
     */
    public void exec(List<IActivityDataObject> activityDataObjects);

    /**
     * All the ActivityProviders will call this method when new events occurred
     * in the UI.
     * 
     * The given activity will be transformed by the ConcurrentDocumentManager
     * and then send to all buddies.
     * 
     * @see IActivityListener
     * 
     * @swt MUST be called from the SWT thread to ensure synchronization!
     */
    public void activityCreated(IActivity activity);

    /**
     * Sends the given activity to the given list of users.
     * 
     * This method will by-pass the ConcurrentDocumentManager.
     */
    public void sendActivity(List<User> recipient, IActivity activity);

    /**
     * Convenience method to address a single recipient.
     * 
     * @see #sendActivity(List, IActivity)
     */
    public void sendActivity(User recipient, IActivity activity);

    /**
     * Adds an {@link IActivityProvider} and also registers itself as
     * {@link IActivityListener} at the given provider.
     * 
     * If the given provider was already added this method does not add it again
     * but silently returns.
     */
    public void addActivityProvider(IActivityProvider provider);

    /**
     * Removes the given provider and deregisters itself as
     * {@link IActivityListener} on that provider.
     */
    public void removeActivityProvider(IActivityProvider provider);

    /**
     * Returns a list of all users in this project which have
     * {@link User.Permission#WRITE_ACCESS} right now.
     * 
     * @snapshot This is a snapshot copy. This list does not change if users'
     *           {@link User.Permission} change.
     * 
     *           There is no guarantee that the users in this list will be part
     *           of the project after you exit the SWT thread context.
     */
    public List<User> getUsersWithWriteAccess();

    /**
     * Returns a list of all users in this project have
     * {@link User.Permission#READONLY_ACCESS} right now.
     * 
     * @snapshot This is a snapshot copy. This list does not change if users'
     *           {@link User.Permission} change.
     * 
     *           There is no guarantee that the users in this list will be part
     *           of the project after you exit the SWT thread context.
     */
    public List<User> getUsersWithReadOnlyAccess();

    /**
     * Returns all users in this project which are both remotely and have
     * {@link User.Permission#READONLY_ACCESS} right now.
     * 
     * @snapshot This is a snapshot copy. This list does not change if users'
     *           {@link User.Permission} change.
     * 
     *           There is no guarantee that the users in this list will be part
     *           of the project after you exit the SWT thread context.
     */
    public List<User> getRemoteUsersWithReadOnlyAccess();

    /**
     * Returns the DateTime at which this SarosSession was started on the host.
     */
    public DateTime getSessionStart();

    /**
     * Returns true if the given IProject is currently shared using this Saros
     * session.
     */
    public boolean isShared(IProject project);

    /**
     * Returns true if VCS support is enabled for this session.<br>
     * <br>
     * This setting can be changed in the Preferences. VCS support can be
     * disabled during a running session, but enabling VCS support doesn't have
     * any effect.
     * 
     * @return true if this session uses Version Control, otherwise false.
     */
    public boolean useVersionControl();

    /**
     * Returns the {@link SharedProject} associated with the {@link IProject}
     * <code>project</code>, or <code>null</code> if the project is not shared.
     */
    public SharedProject getSharedProject(IProject project);

    /**
     * Returns the global ID of the <code>project</code>.
     */
    public String getProjectID(IProject project);

    /**
     * Returns the project with the given global ID.
     */
    public IProject getProject(String projectID);

    /**
     * Adds the specified project as a shared project to this session.
     * 
     * @param project
     *            The project to share.
     * @param projectID
     *            The global project ID.
     */
    public void addSharedProject(IProject project, String projectID);

    /**
     * Returns all shared Projects in their current state
     * 
     * @return
     */
    public List<SharedProject> getSharedProjects();

    /**
     * Synchronizing the userList with <code>peer</code>. This happens during
     * invitation and while other participants are joining or leaving the
     * session
     * 
     * @param peer
     * @param invitationID
     * @throws SarosCancellationException
     */
    public void synchronizeUserList(ITransmitter transmitter, JID peer,
        String invitationID, SubMonitor monitor)
        throws SarosCancellationException;
}