package saros.session;

import saros.monitoring.IProgressMonitor;

/**
 * A listener for {@link ISarosSession} life-cycle related events.
 *
 * @author rdjemili
 */
public interface ISessionLifecycleListener {
  /*
   * TODO: remove this method as soon as external components like the
   * whiteboard are maintained in another way (i.e. a component interface)
   */

  /**
   * Is fired after invitation complete but for every peer the host invited. At this state, the
   * session is fully established and confirmed but the outgoing session negotiation job is still
   * running.
   *
   * <p>Can be used by session components to plug their synchronization process in the session
   * negotiation.
   *
   * <p>Implementations must not block for too long, because this blocks the whole invitation
   * process.
   *
   * <p>TODO: remove this method as soon as external components like the whiteboard are maintained
   * in another way (i.e. a component interface)
   *
   * @param session The corresponding session
   * @param monitor the invitation process's monitor to track process and cancellation
   */
  public default void postOutgoingInvitationCompleted(
      ISarosSession session, User user, IProgressMonitor monitor) {
    // NOP
  }

  /**
   * Is fired when a new session is about to start.
   *
   * @param session the session that is about to start
   */
  public default void sessionStarting(ISarosSession session) {
    // NOP
  }

  /**
   * Is fired when a new session started.
   *
   * @param session the session that has been started
   */
  public default void sessionStarted(ISarosSession session) {
    // NOP
  }

  /**
   * Is fired when a session is about to be ended.
   *
   * @param session the session that is about to end <code>null</code>.
   */
  public default void sessionEnding(ISarosSession session) {
    // NOP
  }

  /**
   * Is fired when a session ended.
   *
   * @param session the session that has been ended
   * @param reason the reason why the session ended
   */
  public default void sessionEnded(ISarosSession session, SessionEndReason reason) {
    // NOP
  }
}
