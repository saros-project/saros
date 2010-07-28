package de.fu_berlin.inf.dpp.project;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager.CommunicationPreferences;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * An interface behind which the {@link SessionManager} hides its non-public
 * methods.
 * 
 * The (I)SessionManager is responsible for providing a link between the
 * basically static world managed by PicoContainer where every class has just a
 * singleton instance which never changes and the {@link SharedProject} which
 * can change many times during the course of the plug-in life-cycle.
 */
@Component(module = "net")
public interface ISessionManager {

    /**
     * @return the active SharedProject object or <code>null</code> if there is
     *         no active project.
     */
    public ISharedProject getSharedProject();

    /**
     * Starts a new shared project with the local user as only participant.
     * 
     * @param project
     *            the local Eclipse project which should become shared.
     * @param useVersionControl
     *            true iff this session uses Version Control, see
     *            {@link ISharedProject#useVersionControl()}.
     * @throws XMPPException
     *             if this method is called with no established XMPP-connection.
     */
    public void startSession(IProject project, List<IResource> resources,
        boolean useVersionControl) throws XMPPException;

    /**
     * Creates a shared project for a session hosted remotely. The returned
     * project is NOT started!
     * 
     * @param projectID
     *            the ID to use when sending activities. This ID is set by the
     *            host for all session participants
     * @param project
     *            the local Eclipse project which should be used to replicate
     *            the remote shared project.
     * @param host
     *            the host of the remotely shared project.
     * @param myColorID
     *            Color ID of the local user
     * 
     * @return the shared project.
     */
    public ISharedProject joinSession(String projectID, IProject project,
        JID host, int myColorID, DateTime sessionStart);

    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     * 
     * Has no effect if there is no currently shared project.
     */
    public void stopSharedProject();

    /**
     * Sets the sessionID to <code>NOT_IN_SESSION</code>
     */
    public void clearSessionID();

    /**
     * Add the given session listener. Is ignored if the listener is already
     * listening.
     * 
     * @param listener
     *            the listener that is to be added.
     */
    public void addSessionListener(ISessionListener listener);

    /**
     * Removes the given session listener. Is ignored if the given listener
     * wasn't listening.
     * 
     * @param listener
     *            the listener that is to be removed.
     */
    public void removeSessionListener(ISessionListener listener);

    /**
     * Is fired when an incoming invitation is received.
     * 
     * @param from
     *            the sender of this invitation.
     * @param description
     *            the informal description text that can be given with
     *            invitations.
     * @param colorID
     *            the assigned color id for the invited participant.
     * @param comPrefs
     *            TODO
     */
    public void invitationReceived(JID from, String sessionID,
        String projectName, String description, int colorID,
        VersionInfo versionInfo, DateTime sessionStart, SarosUI sarosUI,
        String invitationID, boolean doStream, CommunicationPreferences comPrefs);

    /*
     * @see IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState);

    public void onReconnect(Map<JID, Integer> expectedSequenceNumbers);

}