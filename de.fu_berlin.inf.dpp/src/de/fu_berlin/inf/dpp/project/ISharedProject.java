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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
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
	public List<User> getParticipants();

	/**
	 * Sets the new driver. If given driver is already driver the call is
	 * ignored.
	 * 
	 * @param driver
	 *            the new driver.
	 * @param replicated
	 *            <code>false</code> if this event was created by this client.
	 *            <code>true</code> if it was created by another client and
	 *            only replicated to this client.
	 */
	public void setDriver(User driver, boolean replicated);
	
	/**
	 * Remove driver role for given User.
	 * 
	 * @param driver
	 *            one current driver.
	 * @param replicated
	 *            <code>false</code> if this event was created by this client.
	 *            <code>true</code> if it was created by another client and
	 *            only replicated to this client.
	 */
	public void removeDriver(User driver, boolean replicated);

	/**
	 * The driver is the person that is currently allowed to edit the resources.
	 * 
	 * @return the driver.
	 */
	public User getDriver();

	/**
	 * @return <code>true</code> if the local client is the current driver of
	 *         this shared project. <code>false</code> otherwise.
	 */
	public boolean isDriver();
	
	/**
	 * @return <code>true</code> if the given user is one of the current driver of
	 *         this shared project. <code>false</code> otherwise.
	 */
	public boolean isDriver(User user);

	/**
	 * The host is the person that initiated this SharedProject and holds all
	 * original files.
	 * 
	 * @return the host.
	 */
	public User getHost();

	/**
	 * @return <code>true</code> if the local client is the host of this
	 *         shared project. <code>false</code> otherwise.
	 */
	public boolean isHost();

	/**
	 * Adds the user.
	 * 
	 * @param user
	 *            the user that is to be added.
	 */
	public void addUser(User user);
	public void addUser(User user, int index);

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
	 * @return the outgoing invitation process.
	 */
	public IOutgoingInvitationProcess invite(JID jid, String description, boolean inactive, IInvitationUI inviteUI);

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
	 * @param jid
	 *            the JID of a user to invite without manual selection
	 */
	public void startInvitation(JID jid);
	
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
	 * Sets all resources of the project to a readonly state 
	 * on the local file system.
	 * @param The readonly state to set the file to. 
	 */
	public void setProjectReadonly(boolean readonly);
}