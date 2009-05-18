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

import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;

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
     * @param fileList
     *            a list of all files currently present in the project
     * 
     * @return the outgoing invitation process.
     */
    public IOutgoingInvitationProcess invite(JID jid, String description,
        boolean inactive, IInvitationUI inviteUI, FileList fileList);

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
     * @return the sequencer that is responsible for sending and receiving
     *         activities.
     */
    public ActivitySequencer getSequencer();

    /**
     * @return the activity manager that is responsible for all activity
     *         providers.
     */
    public IActivityManager getActivityManager();

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

    public User getLocalUser();

    /**
     * Sets all resources of the project to a readonly state on the local file
     * system.
     * 
     * @param readonly
     *            The readonly state to set the file to.
     */
    public void setProjectReadonly(boolean readonly);

    /**
     * @return true, if there is exactly one driver, false otherwise.
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
     * The ITransmitter is the network component and responsible for
     * transmitting changes to the other participants
     */
    public ITransmitter getTransmitter();

    /**
     * The Saros Plugin this SharedProject is running in.
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

}