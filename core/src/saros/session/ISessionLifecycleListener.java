package saros.session;

/** A listener for {@link ISarosSession} life-cycle related events. */
public interface ISessionLifecycleListener {
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
