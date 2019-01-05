package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface for starting and stopping a DPP session. It also offers support for monitoring the
 * life-cycle of a session.
 */
public interface ISarosSessionManager {

  /** @return the active session or <code>null</code> if there is no active session. */
  public ISarosSession getSession();

  /**
   * Starts a new DPP session with the local user as only participant.
   *
   * @param referencePointResources the local reference point resources which should be shared.
   * @param referencePointManager
   */
  public void startSession(
      Map<IReferencePoint, List<IResource>> referencePointResources,
      IReferencePointManager referencePointManager);

  // FIXME this method is error prone and only used by the IPN, find a better
  // abstraction
  /**
   * Creates a DPP session. The session is NOT started!
   *
   * @param host the host of the session.
   * @return a new session.
   */
  public ISarosSession joinSession(
      final String id,
      JID host,
      IPreferenceStore hostProperties,
      IPreferenceStore clientProperties);

  /**
   * Stops the currently active session. If the local user is the host, this will close the session
   * for everybody.
   *
   * @param reason the reason why the session ended.
   */
  public void stopSession(SessionEndReason reason);

  /**
   * Add the given session life-cycle listener.
   *
   * @param listener the listener that is to be added.
   */
  public void addSessionLifecycleListener(ISessionLifecycleListener listener);

  /**
   * Removes the given session life-cycle listener.
   *
   * @param listener the listener that is to be removed.
   */
  public void removeSessionLifecycleListener(ISessionLifecycleListener listener);

  /**
   * Starts sharing all projects of the current session with the given session user. This should be
   * called after the user joined the current session.
   *
   * @param user JID of the user to share projects with
   */
  public void startSharingProjects(JID user);

  /**
   * Invites a user to a running session. Does nothing if no session is running, the user is already
   * part of the session, or is currently joining the session.
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
  public void addResourcesToSession(Map<IProject, List<IResource>> projectResourcesMapping);

  /**
   * Call this before a ISarosSession is started.
   *
   * @deprecated the manager should notify its listeners not any other component
   */
  @Deprecated
  void sessionStarting(ISarosSession sarosSession);

  /**
   * Call this after a ISarosSession has been started.
   *
   * @deprecated the manager should notify its listeners not any other component
   */
  @Deprecated
  void sessionStarted(ISarosSession sarosSession);

  /** Call this on the host after the invitation was accepted and has been completed. */
  void postOutgoingInvitationCompleted(IProgressMonitor monitor, User newUser);

  /**
   * Sets the {@link INegotiationHandler negotiation handler} that will handle incoming and outgoing
   * session and project negotiations requests.
   *
   * @param handler a handler to handle negotiation request or <code>null</code> if requests should
   *     not be handled at all.
   */
  public void setNegotiationHandler(INegotiationHandler handler);
}
