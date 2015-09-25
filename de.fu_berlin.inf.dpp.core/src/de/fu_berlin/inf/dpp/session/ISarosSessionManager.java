package de.fu_berlin.inf.dpp.session;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * Interface for starting and stopping a DPP session. It also offers support for
 * monitoring the life-cycle of a session.
 */
@Component(module = "net")
public interface ISarosSessionManager {

    /**
     * @return the active SarosSession object or <code>null</code> if there is
     *         no active session.
     */
    public ISarosSession getSarosSession();

    /**
     * Starts a new Saros session with the local user as only participant.
     * 
     * @param projectResources
     *            the local Eclipse project resources which should become
     *            shared.
     */
    public void startSession(Map<IProject, List<IResource>> projectResources);

    /**
     * Creates a Saros session. The returned session is NOT started!
     * 
     * @param host
     *            the host of the session.
     * 
     * @return the new Saros session.
     */
    public ISarosSession joinSession(final String id, JID host,
        int clientColor, int hostColor);

    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     * <p>
     * Has no effect if there is no open session.
     * </p>
     * 
     * @param reason
     *            the reason why the session ended
     */
    public void stopSarosSession(SessionEndReason reason);

    /**
     * Add the given session life-cycle listener.
     * 
     * @param listener
     *            the listener that is to be added.
     */
    public void addSessionLifecycleListener(ISessionLifecycleListener listener);

    /**
     * Removes the given session life-cycle listener.
     * 
     * @param listener
     *            the listener that is to be removed.
     */
    public void removeSessionLifecycleListener(
        ISessionLifecycleListener listener);

    /**
     * Handles the negotiation for a received invitation.
     * 
     * @param from
     *            the sender of this invitation
     * @param sessionID
     *            the unique session ID of the inviter side
     * @param invitationID
     *            a unique identifier for the negotiation
     * @param version
     *            remote Saros version of the inviter side
     * @param description
     *            what this session invitation is about
     * @deprecated will be removed from the interface - do not use in new code
     */
    @Deprecated
    public void invitationReceived(JID from, String sessionID,
        String invitationID, String version, String description);

    /**
     * Will start sharing all projects of the current session with a
     * participant. This should be called after a the invitation to a session
     * was completed successfully.
     * 
     * @param user
     *            JID of session participant to share projects with
     */
    public void startSharingProjects(JID user);

    /**
     * Invites a user to a running session. Does nothing if no session is
     * running, the user is already part of the session or is currently in the
     * invitation process.
     * 
     * @param toInvite
     *            the JID of the user that is to be invited.
     */
    public void invite(JID toInvite, String description);

    /**
     * Invites users to the shared project.
     * 
     * @param jidsToInvite
     *            the JIDs of the users that should be invited.
     */
    public void invite(Collection<JID> jidsToInvite, String description);

    /**
     * Adds project resources to an existing session.
     * 
     * @param projectResourcesMapping
     * 
     */
    public void addResourcesToSession(
        Map<IProject, List<IResource>> projectResourcesMapping);

    /**
     * This method is called when a new project was added to the session
     * 
     * @param from
     *            The one who added the project.
     * @param projectInfos
     *            what projects where added ({@link FileList}, projectName etc.)
     *            see: {@link ProjectNegotiationData}
     * @param negotiationID
     *            ID of the negotiation
     */
    public void incomingProjectReceived(JID from,
        List<ProjectNegotiationData> projectInfos, String negotiationID);

    /**
     * Call this as soon as all shared resources of a project in the session are
     * available.
     * 
     * @param projectID
     *            ID of the project whose shared resources are now available
     */
    void projectResourcesAvailable(String projectID);

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
    void postOutgoingInvitationCompleted(IProgressMonitor monitor, User newUser);

    // FIXME add back
    // /**
    // * Sets the {@link INegotiationHandler negotiation handler} that will
    // handle
    // * incoming and outgoing session and project negotiations requests.
    // *
    // * @param handler
    // * the handler to handle the request or <code>null</code> if the
    // * requests should not be handled
    // *
    // */
    // public void setNegotiationHandler(INegotiationHandler handler);
}