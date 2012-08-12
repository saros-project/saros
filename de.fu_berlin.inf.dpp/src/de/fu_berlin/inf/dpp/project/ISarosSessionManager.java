package de.fu_berlin.inf.dpp.project;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.XMPPException;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * An interface behind which the {@link SarosSessionManager} hides its
 * non-public methods.
 * 
 * The (I)SessionManager is responsible for providing a link between the
 * basically static world managed by PicoContainer where every class has just a
 * singleton instance which never changes and the {@link ISarosSession} which
 * can change many times during the course of the plug-in life-cycle.
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
     * @throws XMPPException
     *             if this method is called with no established XMPP-connection.
     */
    public void startSession(Map<IProject, List<IResource>> projectResources)
        throws XMPPException;

    /**
     * Creates a Saros session. The returned session is NOT started!
     * 
     * @param host
     *            the host of the session.
     * @param myColorID
     *            Color ID of the local user
     * 
     * @return the new Saros session.
     */
    public ISarosSession joinSession(JID host, int myColorID,
        DateTime sessionStart, JID inviter, int inviterColorID);

    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     * 
     * Has no effect if there is no open session.
     */
    public void stopSarosSession();

    /**
     * Add the given session listener. Is ignored if the listener is already
     * listening.
     * 
     * @param listener
     *            the listener that is to be added.
     */
    public void addSarosSessionListener(ISarosSessionListener listener);

    /**
     * Removes the given session listener. Is ignored if the given listener
     * wasn't listening.
     * 
     * @param listener
     *            the listener that is to be removed.
     */
    public void removeSarosSessionListener(ISarosSessionListener listener);

    /**
     * Is fired when an incoming invitation is received.
     * 
     * @param from
     *            the sender of this invitation.
     * @param colorID
     *            the assigned color id for the invited participant.
     * @param comPrefs
     *            multi user chat parameters.
     */
    public void invitationReceived(JID from, String sessionID, int colorID,
        VersionInfo versionInfo, DateTime sessionStart, SarosUI sarosUI,
        String invitationID, MUCSessionPreferences comPrefs,
        String description, JID host, int inviterColorID);

    /**
     * initiate the ({@link OutgoingProjectNegotiation project exchanging}) with
     * user
     */
    public void startSharingProjects(JID user,
        List<ProjectExchangeInfo> projectExchangeInfos);

    /**
     * Invites a user to the shared project.
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
     *            see: {@link ProjectExchangeInfo}
     * @param processID
     *            ID of the exchanging process
     */
    public void incomingProjectReceived(JID from,
        List<ProjectExchangeInfo> projectInfos, String processID);

    /**
     * Call this when a new project was added.
     * 
     * @param projectID
     *            TODO
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
     * Call this on the client after the invitation has been completed.
     */
    void preIncomingInvitationCompleted(IProgressMonitor monitor);

    /**
     * Call this on the host after the invitation was accepted and has been
     * completed.
     */
    void postOutgoingInvitationCompleted(IProgressMonitor monitor, User newUser);
}