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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Shared projects are the central concept of this plugin. They are associated
 * with usual Eclipse projects and make them available for synchronous/real-time
 * collaboration.
 * 
 * @author rdjemili
 */
public interface ISharedProject {

    /**
     * @return a list of all participants of the shared project. This list
     *         includes yourself.
     */
    public Collection<User> getParticipants();

    /**
     * Set the role of the given user.
     * 
     * @param user
     *            the user which role has to be set.
     * @param role
     *            The new role of the user.
     * @param replicated
     *            <code>false</code> if this event was created by this client.
     *            <code>true</code> if it was created by another client and only
     *            replicated to this client.
     */
    public void setUserRole(User user, UserRole role, boolean replicated);

    /**
     * @return <code>true</code> if the local client is a current driver of this
     *         shared project. <code>false</code> otherwise.
     */
    public boolean isDriver();

    /**
     * The host is the person that initiated this SharedProject and holds all
     * original files.
     * 
     * @return the host.
     */
    public User getHost();

    /**
     * @return <code>true</code> if the local client is the host of this shared
     *         project. <code>false</code> otherwise.
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
     */
    public void removeUser(User user);

    /**
     * Invites a user to the shared project.
     * 
     * @param jid
     *            the JID of the user that is to be invited.
     * @param description
     *            a description that will be shown to the invited user before he
     *            makes the decision to accept or decline the invitation.
     * @param inviteUI
     *            user interface of the invitation for feedback calls.
     * 
     * @return the outgoing invitation process.
     */
    public IOutgoingInvitationProcess invite(JID jid, String description,
        boolean inactive, IInvitationUI inviteUI);

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
     * @return the Eclipse project associtated with this shared project. This
     *         never returns <code>null</code>.
     */
    public IProject getProject();

    /**
     * @return the file list representation of the Eclipse project that is
     *         associated with this shared project.
     * @throws CoreException
     *             if there are problems while reading file tree of the Eclipse
     *             project.
     */
    public FileList getFileList() throws CoreException;

    // TODO remove direct uses of FileList

    /**
     * @return the sequencer that is responsible for sending and receiving
     *         activities.
     */
    public IActivitySequencer getSequencer();

    /**
     * @return the activity manager that is responsible for all activity
     *         providers.
     */
    public IActivityManager getActivityManager();

    /**
     * Starts the invitation wizard to invite users.
     * 
     * @param list
     *            the JIDs of users to invite manual selection
     */
    public void startInvitation(@Nullable List<JID> list);

    /**
     * Activates sending of activities. The reason that this isn't done
     * automatically are unit tests.
     */
    public void start();

    /**
     * Deactivates sending of activities.
     */
    public void stop();

    public User getParticipant(JID jid);

    /**
     * Sets all resources of the project to a readonly state on the local file
     * system.
     * 
     * @param readonly
     *            The readonly state to set the file to.
     */
    public void setProjectReadonly(boolean readonly);

    /**
     * true, if single driver is active, false otherwise.
     * 
     * @return
     */
    public boolean isExclusiveDriver();

    /**
     * the concurrent document manager is responsible for all jupiter controlled
     * documents
     * 
     * @return the concurrent document manager
     */
    public ConcurrentDocumentManager getConcurrentDocumentManager();

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
     * Gets a driver. If there is more than one, there is no guarantee which one
     * of them is returned.
     * 
     * @return a {@link User} with the driver role, or <code>null</code> if
     *         there is no driver at all.
     */
    public User getADriver();
}