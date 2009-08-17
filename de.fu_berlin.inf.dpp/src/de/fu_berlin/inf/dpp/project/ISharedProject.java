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
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;

/**
 * Shared projects are the central concept of this plugin. They are associated
 * with usual Eclipse projects and make them available for synchronous/real-time
 * collaboration.
 * 
 * TODO Is this interface really necessary?
 * 
 * @author rdjemili
 */
public interface ISharedProject extends IActivityListener {

    /**
     * @return a collection of all participants of the shared project. This
     *         collection includes the local user.
     */
    public Collection<User> getParticipants();

    /**
     * @return a snap-shot copy of all remote users.
     */
    public List<User> getRemoteUsers();

    /**
     * Initiates a role change. This method is called when the user wants to
     * change user roles via the UI.
     * 
     * @host This method may only called by the host.
     * @noSWT This method mustn't be called from the SWT UI thread
     * @param user
     *            The user which role has to be changed.
     * @blocking Returning after the role change is complete
     * @cancelable
     * 
     * @Throws CancellationException
     * @Throws InterruptedException
     */
    public void initiateRoleChange(User user, UserRole newRole,
        SubMonitor progress) throws CancellationException, InterruptedException;

    /**
     * Set the role of the given user. This is called on incoming activities
     * from the network.
     * 
     * @swt This method needs to be called from the SWT UI thread
     * @param user
     *            the user which role has to be set.
     * @param role
     *            The new role of the user.
     */
    public void setUserRole(User user, UserRole role);

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
     * Activates sending of activities. The reason that this isn't done
     * automatically are unit tests.
     */
    public void start();

    /**
     * Deactivates sending of activities.
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
     * given JID exists in this SharedProject.
     */
    public JID getResourceQualifiedJID(JID jid);

    public User getLocalUser();

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

    /**
     * Execute activity after jupiter transforming process.
     * 
     * @swt Must be called from the SWT Thread
     */
    public void execTransformedActivity(TextEditActivity activity);

    /**
     * Excutes the given activities locally.
     */
    public void exec(List<IActivity> activities);

    /**
     * All the ActivityProviders will call this method when new events occurred
     * in the UI.
     * 
     * @see IActivityListener
     */
    public void activityCreated(IActivity activity);

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
}