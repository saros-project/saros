package de.fu_berlin.inf.dpp.project;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.net.ConnectionState;
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
     * @param projects
     *            the local Eclipse projects which should become shared.
     * @throws XMPPException
     *             if this method is called with no established XMPP-connection.
     */
    public void startSession(List<IProject> projects, List<IResource> resources)
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
        DateTime sessionStart);

    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     * 
     * Has no effect if there is no open session.
     */
    public void stopSarosSession();

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
        String invitationID, MUCSessionPreferences comPrefs, String description);

    /*
     * @see IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState);

    public void onReconnect(Map<JID, Integer> expectedSequenceNumbers);

    /**
     * initiate the ({@link OutgoingProjectNegotiation project exchanging}) with
     * user
     */
    public void startSharingProjects(JID user);

}