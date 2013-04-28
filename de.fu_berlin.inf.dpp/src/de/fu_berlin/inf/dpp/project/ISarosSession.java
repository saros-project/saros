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
package de.fu_berlin.inf.dpp.project;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.synchronize.StopManager;

/**
 * A Saros session consists of one or more shared projects, which are the
 * central concept of the Saros plugin. They are associated with Eclipse
 * projects and make them available for synchronous/real-time collaboration.
 * 
 * @author rdjemili
 */
public interface ISarosSession {

    /**
     * @return a list of all users of this session
     */
    public List<User> getUsers();

    /**
     * @return a list of all remote users of this session
     */
    public List<User> getRemoteUsers();

    /**
     * Initiates a {@link Permission} change.
     * 
     * @host This method may only called by the host.
     * @noSWT This method mustn't be called from the SWT UI thread
     * @param user
     *            The user which {@link Permission} has to be changed.
     * @blocking Returning after the {@link Permission} change is complete
     * @cancelable
     * 
     * @Throws CancellationException
     * @Throws InterruptedException
     */
    public void initiatePermissionChange(User user, Permission newPermission,
        IProgressMonitor progress) throws CancellationException,
        InterruptedException;

    /**
     * Set the {@link Permission} of the given user.
     * 
     * @swt This method MUST to be called from the SWT UI thread
     * @param user
     *            the user which {@link Permission} has to be set.
     * @param permission
     *            The new {@link Permission} of the user.
     */
    public void setPermission(User user, Permission permission);

    /**
     * @return <code>true</code> if the local user has
     *         {@link Permission#WRITE_ACCESS write access}, <code>false</code>
     *         otherwise
     */
    public boolean hasWriteAccess();

    /**
     * Returns the host of this session.
     * 
     * @immutable This method will always return the same value for this session
     */
    public User getHost();

    /**
     * @return <code>true</code> if the local user is the host of this session,
     *         <code>false</code> otherwise.
     * 
     */
    public boolean isHost();

    /**
     * Adds the user to this session. If the session currently serves as host
     * all other session users will be noticed about the new user.
     * 
     * @param user
     *            the user that is to be added
     */
    public void addUser(User user);

    /**
     * Removes a user from this session.
     * 
     * @param user
     *            the user that is to be removed
     * 
     * @swt Must be called from SWT!
     */
    public void removeUser(User user);

    /**
     * Kicks and removes the user out of the session.
     * 
     * @param user
     *            the user that should be kicked from the session
     * 
     * @throws IllegalStateException
     *             if the local user is not the host of the session
     * @throws IllegalArgumentException
     *             if the user to kick is the local user
     */
    public void kickUser(User user);

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
     * @return the shared projects associated with this session, never
     *         <code>null</code> but may be empty
     */
    public Set<IProject> getProjects();

    /**
     * FOR INTERNAL USE ONLY !
     */
    public void start();

    /**
     * FOR INTERNAL USE ONLY !
     */
    public void stop();

    /**
     * <p>
     * Given a resource qualified JID, this method will return the user which
     * has the identical ID including resource.
     * </p>
     * <p>
     * Use getResourceQualifiedJID(JID) in the case if you do not know the
     * RQ-JID.
     * </p>
     * 
     * @return the user with the given fully qualified JID or <code>null</code>
     *         if not user with such a JID exists in the session
     */
    public User getUser(JID jid);

    /**
     * <p>
     * Returns the resource qualified JID associated with a user of the session.
     * </p>
     * 
     * E.g:
     * 
     * <pre>
     * <code>
     * JID rqJID = session.getResourceQualifiedJID(new JID("alice@foo.com");
     * System.out.println(rqJID);
     * </code>
     * </pre>
     * 
     * <p>
     * Will print out something like alice@foo.com/Saros*****
     * </p>
     * 
     * @param jid
     *            the JID to retrieve the resource qualified JID for
     * 
     * @return the resource qualified JID or <code>null</code> if no user is
     *         found with this JID
     */
    public JID getResourceQualifiedJID(JID jid);

    /**
     * Returns the local user of this session.
     * 
     * 
     * @immutable This method will always return the same value for this session
     */
    public User getLocalUser();

    /**
     * @return true, if there is exactly one user with
     *         {@link Permission#WRITE_ACCESS}, false otherwise.
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
     * 
     * @deprecated will be removed, do not use this in new code
     */

    @Deprecated
    public Saros getSaros();

    /**
     * Returns a snapshot of the currently available (not in use) color IDs.
     * 
     * @return
     */
    public Set<Integer> getAvailableColors();

    /**
     * FOR INTERNAL USE ONLY !
     */
    public void exec(List<IActivityDataObject> activityDataObjects);

    /**
     * Sends the given activity to the given list of users.
     * 
     * This method will by-pass the ConcurrentDocumentManager and should not be
     * used in new code.
     */
    @Deprecated()
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
     * Returns a list of all users in this session which have
     * {@link Permission#WRITE_ACCESS} right now.
     * 
     * @snapshot This is a snapshot copy. This list does not change if users'
     *           {@link Permission} change.
     * 
     *           There is no guarantee that the users in this list will be part
     *           of the project after you exit the SWT thread context.
     */
    public List<User> getUsersWithWriteAccess();

    /**
     * Returns a list of all users in this session have
     * {@link Permission#READONLY_ACCESS} right now.
     * 
     * @snapshot This is a snapshot copy. This list does not change if users'
     *           {@link Permission} change.
     * 
     *           There is no guarantee that the users in this list will be part
     *           of the project after you exit the SWT thread context.
     */
    public List<User> getUsersWithReadOnlyAccess();

    /**
     * Returns all users in this project which are both remotely and have
     * {@link Permission#READONLY_ACCESS} right now.
     * 
     * @snapshot This is a snapshot copy. This list does not change if users'
     *           {@link Permission} change.
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
     * @return <code>true</code> if the given {@link IResource resource} is
     *         currently shared in this session, <code>false</code> otherwise
     */
    public boolean isShared(IResource resource);

    /**
     * Checks if selected project is a complete shared one or partial shared.
     * 
     * @param project
     * @return <code>true</code> if complete, <code>false</code> if partial
     */
    public boolean isCompletelyShared(IProject project);

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
     * Returns the global ID of the project.
     * 
     * @return the global ID of the project or <code>null</code> if this project
     *         is not shared
     */
    public String getProjectID(IProject project);

    /**
     * Returns the project with the given ID.
     * 
     * @return the project with the given ID or <code>null</code> if no project
     *         with this ID is shared
     */
    public IProject getProject(String projectID);

    /**
     * Adds the specified project and/or resources to this session.
     * 
     * @param project
     *            The project to share.
     * @param projectID
     *            The global project ID.
     * @param dependentResources
     *            The project dependent resources.
     */
    public void addSharedResources(IProject project, String projectID,
        List<IResource> dependentResources);

    /**
     * Returns all shared Projects in their current state
     * 
     * @return
     */
    public List<SharedProject> getSharedProjects();

    /**
     * Returns all shared resources in this session.
     * 
     * @return a list of all shared resources (excluding projects) from this
     *         session.
     */
    public List<IResource> getSharedResources();

    /**
     * Returns a map with the mapping of shared resources to their project.
     * 
     * @return project-->resource mapping
     */
    public Map<IProject, List<IResource>> getProjectResourcesMapping();

    /**
     * Returns the shared resources of the project in this session.
     * 
     * @param project
     * @return the shared resources or <code>null</code> if this project is not
     *         or fully shared.
     */
    public List<IResource> getSharedResources(IProject project);

    /**
     * Adds to the SarosProjectMapper the mapping of JID to project. This is
     * done to identify the resources host.
     * 
     * @param projectID
     *            ID of the project
     * @param project
     *            the IProject itself
     * @param ownerJID
     *            the inviter to this project
     */
    public void addProjectOwnership(String projectID, IProject project,
        JID ownerJID);

    /**
     * Removes the mapping of the project from the SarosProjectMapper.
     * 
     * @param projectID
     *            ID of the project
     * @param project
     *            the IProject itself
     * @param ownerJID
     *            the inviter to this project
     */
    public void removeProjectOwnership(String projectID, IProject project,
        JID ownerJID);

    /**
     * Return the stop manager of this session.
     * 
     * @return
     */
    public StopManager getStopManager();

    /**
     * Changes the color for the current session.
     * 
     * @param colorID
     *            the new color id that should be used to select the new color
     * 
     * @throws IllegalArgumentException
     *             if the color id is negative or exceeds the available color id
     *             limit
     */
    public void changeColor(int colorID);

    /**
     * FOR INTERNAL USE ONLY !
     * 
     * Starts queuing project related activities (e.g {@link FileActivity} or
     * {@link JupiterActivity}) for a shared project.
     * 
     * @param projectId
     *            the id of the project for which project related activities
     *            should be queued
     */
    public void enableQueuing(String projectId);

    /**
     * FOR INTERNAL USE ONLY !
     * 
     * Disables queuing for all shared projects and flushes all queued
     * activities.
     */
    public void disableQueuing();
}
