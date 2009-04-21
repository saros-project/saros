package de.fu_berlin.inf.dpp.project;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;

/**
 * An interface behind which the {@link SessionManager} hides its non-public
 * methods.
 * 
 * The (I)SessionManager is responsible for providing a link between the
 * basically static world managed by PicoContainer where every class has just a
 * singleton instance which never changes and the {@link SharedProject} which
 * can change many times during the course of the plug-in life-cycle.
 */
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
     * @throws XMPPException
     *             if this method is called with no established XMPP-connection.
     */
    public void startSession(IProject project) throws XMPPException;

    /**
     * Creates a shared project for a session hosted remotely. The returned
     * project is NOT started!
     * 
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
    public ISharedProject joinSession(IProject project, JID host, int myColorID);

    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     * 
     * Has no effect if there is no currently shared project.
     */
    public void stopSharedProject();

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
     * @return the process that represents the invitation and which handles the
     *         further interaction with the invitation.
     */
    public IIncomingInvitationProcess invitationReceived(JID from,
        String sessionID, String projectName, String description, int colorID);

    /*
     * @see IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState);

    public void onReconnect(Map<JID, Integer> expectedSequenceNumbers);

    /**
     * Called by the Invitation Process if the invitation did not work out
     * (joinSession was not called).
     * 
     * Set the SessionID to none, so that new Sessions can be begun.
     */
    public void cancelIncomingInvitation();

}