package de.fu_berlin.inf.dpp.project;

import org.eclipse.core.resources.IProject;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;

public interface ISessionManager {

    public final static String NOT_IN_SESSION = "NOT_IN_SESSION";

    /**
     * Starts a new shared project with the local user as only participant.
     *
     * @param project
     *            the local Eclipse project which should become shared.
     * @throws XMPPException
     *             if this method is called with no established XMPP-connection.
     */
    public abstract void startSession(IProject project) throws XMPPException;

    /**
     * Every Session is identified by an int as identifier.
     *
     * @return the session id of this session
     */
    public String getSessionID();

    /**
     * Joins an remotely already running shared project.
     *
     * @param project
     *            the local Eclipse project which should be used to replicate
     *            the remote shared project.
     * @param host
     *            the host of the remotely shared project.
     * @param driver
     *            the driver of the shared project.
     * @param myColorID
     *            Color ID of the user.
     *
     * @return the shared project.
     */
    public abstract ISharedProject joinSession(IProject project, JID host,
            int myColorID);

    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     *
     * Has no effect if there is no currently shared project.
     */
    public abstract void stopSharedProject();

    /**
     * @return the active SharedProject object or <code>null</code> if there is
     *         no active project.
     */
    public abstract ISharedProject getSharedProject();

    /**
     * Add the given session listener. Is ignored if the listener is already
     * listening.
     *
     * @param listener
     *            the listener that is to be added.
     */
    public abstract void addSessionListener(ISessionListener listener);

    /**
     * Removes the given session listener. Is ignored if the given listener
     * wasn't listening.
     *
     * @param listener
     *            the listener that is to be removed.
     */
    public abstract void removeSessionListener(ISessionListener listener);

    /**
     * Is fired when an incoming invitation is received.
     *
     * @param from
     *            the sender of this invitation.
     * @param description
     *            the informal description text that can be given with
     *            invitations.
     * @param sessionID
     *            the id of the session
     * @param colorID
     *            the assigned color id for the invited participant.
     * @return the process that represents the invitation and which handles the
     *         further interaction with the invitation.
     */
    public abstract IIncomingInvitationProcess invitationReceived(JID from,
            String sessionID, String projectName, String description,
            int colorID);

    /*
     * (non-Javadoc)
     *
     * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
     */
    public abstract void connectionStateChanged(XMPPConnection connection,
            ConnectionState newState);

    public abstract void OnReconnect(int oldtimestamp);

    /**
     * Get the transmitter of the session.
     *
     * @return the transmitter of the session
     */
    public abstract ITransmitter getTransmitter();

}