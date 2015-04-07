/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.project;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.core.invitation.INegotiationHandler;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An interface behind which the {@link SarosSessionManager} hides its
 * non-public methods.
 * <p/>
 * The (I)SessionManager is responsible for providing a link between the
 * basically static world managed by PicoContainer where every class has just a
 * singleton instance which never changes and the {@link ISarosSession} which
 * can change many times during the course of the plug-in life-cycle.
 */
@Component(module = "net")
public interface ISarosSessionManager {

    /**
     * @return the active SarosSession object or <code>null</code> if there is
     * no active session.
     */
    public ISarosSession getSarosSession();

    /**
     * Starts a new Saros session with the local user as only participant.
     *
     * @param projectResources the local Eclipse project resources which should become
     *                         shared.
     */
    public void startSession(Map<IProject, List<IResource>> projectResources);

    /**
     * Creates a Saros session. The returned session is NOT started!
     *
     * @param host the host of the session.
     * @return the new Saros session.
     */
    public ISarosSession joinSession(JID host, int clientColor, int hostColor);

    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     * <p/>
     * Has no effect if there is no open session.
     */
    public void stopSarosSession();

    /**
     * Add the given session listener.
     *
     * @param listener the listener that is to be added.
     */
    public void addSarosSessionListener(ISarosSessionListener listener);

    /**
     * Removes the given session listener.
     *
     * @param listener the listener that is to be removed.
     */
    public void removeSarosSessionListener(ISarosSessionListener listener);

    /**
     * Handles the negotiation process for a received invitation.
     *
     * @param from         the sender of this invitation
     * @param sessionID    the unique session ID of the inviter side
     * @param invitationID a unique identifier for the negotiation process
     * @param version      remote Saros version of the inviter side
     * @param description  what this session invitation is about
     */
    public void invitationReceived(JID from, String sessionID,
        String invitationID, String version, String description);

    /**
     * Will start sharing all projects of the current session with a
     * participant. This should be called after a the invitation to a session
     * was completed successfully.
     *
     * @param user JID of session participant to share projects with
     */
    public void startSharingProjects(JID user);

    /**
     * Invites a user to a running session. Does nothing if no session is
     * running, the user is already part of the session or is currently in the
     * invitation process.
     *
     * @param toInvite the JID of the user that is to be invited.
     */
    public void invite(JID toInvite, String description);

    /**
     * Invites users to the shared project.
     *
     * @param jidsToInvite the JIDs of the users that should be invited.
     */
    public void invite(Collection<JID> jidsToInvite, String description);

    /**
     * Adds project resources to an existing session.
     *
     * @param projectResourcesMapping
     */
    public void addResourcesToSession(
        Map<IProject, List<IResource>> projectResourcesMapping);

    /**
     * This method is called when a new project was added to the session
     *
     * @param from         The one who added the project.
     * @param projectInfos what projects where added ({@link de.fu_berlin.inf.dpp.invitation.FileList}, projectName etc.)
     *                     see: {@link de.fu_berlin.inf.dpp.invitation.ProjectNegotiationData}
     * @param processID    ID of the exchanging process
     */
    public void incomingProjectReceived(JID from,
        List<ProjectNegotiationData> projectInfos, String processID);

    /**
     * Call this when a new project was added.
     *
     * @param projectID TODO
     */
    void projectAdded(String projectID);

    /**
     * Call this before a ISarosSession is started.
     */
    void sessionStarting(ISarosSession sarosSession);

    /**
     * Call this after a ISarosSession has been started.
     */
    void sessionStarted(ISarosSession sarosSession);

    /**
     * Call this on the host after the invitation was accepted and has been
     * completed.
     */
    void postOutgoingInvitationCompleted(IProgressMonitor monitor,
        User newUser);

    /**
     * Sets the {@link de.fu_berlin.inf.dpp.core.invitation.INegotiationHandler negotiation handler} that will handle
     * incoming and outgoing session and project negotiations requests.
     *
     * @param handler the handler to handle the request or <code>null</code> if the
     *                requests should not be handled
     */
    public void setNegotiationHandler(INegotiationHandler handler);
}
