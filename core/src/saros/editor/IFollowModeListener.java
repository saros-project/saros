package saros.editor;

import saros.session.User;

/** Listener interface for monitoring local and remote following states. */
public interface IFollowModeListener {
  /** Reasons why the FollowMode stopped */
  enum Reason {
    /** The user being followed left the session */
    FOLLOWEE_LEFT_SESSION,
    /**
     * The local user switched to another editor or closed the editor (not easy to distinguish).
     * Display something like: <i>Follow Mode stopped: You switched to another editor that is not
     * shared or closed the followed editor.</i>
     */
    FOLLOWER_CLOSED_OR_SWITCHED_EDITOR,
    /**
     * The local user closed the editor. Display something like: <i>Follow Mode stopped: You closed
     * the followed editor.</i>
     */
    FOLLOWER_CLOSED_EDITOR,
    /** The local user actively stopped the Follow Mode */
    FOLLOWER_STOPPED,
    /**
     * The local user is about to follow another user. A {@link
     * IFollowModeListener#startedFollowing(User) startedFollowing()} call is imminent.
     */
    FOLLOWER_SWITCHES_FOLLOWEE
  }

  /** Gets called when the <b>local</b> user starts following another session user. */
  public default void startedFollowing(User target) {
    // NOP
  }

  /** Gets called when the <b>local</b> user stops following another session user. */
  public default void stoppedFollowing(Reason reason) {
    // NOP;
  }

  /** Gets called when a <b>remote</b> user starts following another session user. */
  public default void startedFollowing(final User follower, final User followee) {
    // NOP;
  }

  /** Gets called when a <b>remote</b> user stops following another session user. */
  public default void stoppedFollowing(final User follower) {
    // NOP
  }
}
