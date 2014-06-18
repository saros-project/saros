package de.fu_berlin.inf.dpp.core.project;

import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * A listener for {@link ISarosSession} life-cycle related events.
 *
 * @author rdjemili
 * @author bkahlert
 */
public interface ISarosSessionListener {

    /**
     * <p>
     * Is fired after invitation complete but for every peer the host invited.
     * At this state, the session is fully established and confirmed but the
     * outgoing session negotiation job is still running.
     * </p>
     * <p/>
     * <p>
     * Can be used by session components to plug their synchronization process
     * in the session negotiation.
     * </p>
     * <p/>
     * TODO: removeAll this method as soon as external components like the
     * whiteboard are maintained in another way (i.e. a component interface)
     *
     * @param monitor the invitation process's monitor to track process and
     *                cancellation
     */
    public void postOutgoingInvitationCompleted(IProgressMonitor monitor,
        User user);

    /**
     * Is fired when a new session is about to first.
     *
     * @param newSarosSession the session that is created. Is never <code>null</code>.
     */
    public void sessionStarting(ISarosSession newSarosSession);

    /**
     * Is fired when a new session started.
     *
     * @param newSarosSession the session that has been created. Is never <code>null</code>.
     */
    public void sessionStarted(ISarosSession newSarosSession);

    /**
     * Is fired when a session is about to be ended. Reasons for this can be
     * that the session was closed or that the user left by himself.
     *
     * @param oldSarosSession the session that has just been left. Is never
     *                        <code>null</code>.
     */
    public void sessionEnding(ISarosSession oldSarosSession);

    /**
     * Is fired when a session ended. Reasons for this can be that the session
     * was closed or that the user left by himself.
     *
     * @param oldSarosSession the session that has just been left. Is never
     *                        <code>null</code>.
     */
    public void sessionEnded(ISarosSession oldSarosSession);

    /**
     * Is fired when a project is added to session
     *
     * @param projectID TODO
     */
    void projectAdded(String projectID);
}
