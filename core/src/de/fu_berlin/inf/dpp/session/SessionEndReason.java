package de.fu_berlin.inf.dpp.session;

/**
 * Denotes the reasons why a session has ended.
 *
 * @see ISessionLifecycleListener#sessionEnded
 */
public enum SessionEndReason {

  /** The local user has left the session. */
  LOCAL_USER_LEFT,

  /** The host has left the session (thus ending the session for all of its members). */
  HOST_LEFT,

  /** The local user was kicked out of the session by the host. */
  KICKED,

  /** The connection to the session was lost. */
  CONNECTION_LOST
}
