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
package de.fu_berlin.inf.dpp;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;

import de.fu_berlin.inf.dpp.listeners.ISharedProjectListener;
import de.fu_berlin.inf.dpp.xmpp.JID;

public interface ISharedProject {

    /**
     * @return a list of all participants of the shared project. This list
     * includes yourself.
     */
    public List<User> getParticipants();

    /**
     * Sets the new driver. If given driver is already driver the call is
     * ignored.
     * 
     * @param driver the new driver.
     * @param replicated <code>false</code> if this event was created by this
     * client. <code>true</code> if it was created by another client and only
     * replicated to this client.
     */
    public void setDriver(User driver, boolean replicated);

    /**
     * The driver is the person that is currently allowed to edit the resources.
     * 
     * @return the driver.
     */
    public User getDriver();

    /**
     * @return <code>true</code> if the local client is the current driver of
     * this shared project. <code>false</code> otherwise.
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
     * @return <code>true</code> if the local client is the host of this
     * shared project. <code>false</code> otherwise.
     */
    public boolean isHost();

    /**
     * Adds the user.
     * 
     * @param user the user that is to be added.
     */
    public void addUser(User user);
    
    /**
     * Removes the user.
     * 
     * @param user the user that is to be removed.
     */
    public void removeUser(User user);

    /**
     * Invites a user to the shared project.
     * 
     * @param jid the JID of the user that is to be invited.
     * @param description a description that will be shown to the invited user
     * before he makes the decision to accept or decline the invitation.
     * @return the outgoing invitation process.
     */
    public IOutgoingInvitationProcess invite(JID jid, String description);
    
    public IActivitySequencer getSequencer();
    
    public void addListener(ISharedProjectListener listener);

    public void removeListener(ISharedProjectListener listener);

    /**
     * @return the Eclipse project associtated.
     */
    public IProject getProject();

    /**
     * @return the path to the resource that the driver is currently editting.
     */
    public IPath getDriverPath();

    public void setDriverPath(IPath path, boolean replicated);
    
    public ITextSelection getDriverTextSelection();
    
    public void setDriverTextSelection(ITextSelection selection);

    public FileList getFileList() throws CoreException; // TODO remove direct uses of FileList

    public EditorManager getEditorManager();

}