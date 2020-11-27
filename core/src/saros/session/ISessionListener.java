package saros.session;

import saros.filesystem.IReferencePoint;
import saros.session.User.Permission;

/**
 * Listens for events that can happen during a {@link ISarosSession session}. For life-cycle events
 * like the start and end of a session, use {@link ISessionLifecycleListener}.
 */
public interface ISessionListener {
  /**
   * The user {@link Permission} of the given participant has been changed. This is called after the
   * {@link Permission} of the user has been updated to represent the new state.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user whose {@link Permission} changed.
   */
  default void permissionChanged(User user) {
    // NOP
  }

  /**
   * Is fired when an user joins the session.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user that has joined.
   */
  default void userJoined(User user) {
    // NOP
  }

  /**
   * Is fired when a user started queuing and is now able to process all activities.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user that has joined.
   */
  default void userStartedQueuing(User user) {
    // NOP
  }

  /**
   * Is fired when a finished the resource negotiation
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user that has joined.
   */
  default void userFinishedResourceNegotiation(User user) {
    // NOP
  }

  /**
   * Is fired when the color assigned to a user in the session changed.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user whose color changed
   */
  default void userColorChanged(User user) {
    // NOP
  }

  /**
   * Is fired when an user leaves the session.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user that has left.
   */
  default void userLeft(User user) {
    // NOP
  }

  /**
   * Is fired then a reference point has been made part of the session, either because the local
   * user began sharing it or because it is being shared by a remote user.
   *
   * <p>This method might <i>not</i> be called on the UI thread.
   *
   * @param referencePoint the reference point that was added
   */
  default void referencePointAdded(IReferencePoint referencePoint) {
    // NOP
  }

  /**
   * Is fired then a reference point has been removed from the session, meaning it is not shared
   * between the session's users anymore.
   *
   * <p>This method might <i>not</i> be called on the UI thread.
   *
   * @param referencePoint the reference point that was removed
   */
  default void referencePointRemoved(IReferencePoint referencePoint) {
    // NOP
  }

  /**
   * Is fired when resources are added to the current session.
   *
   * <p>This method might <i>not</i> be called on the UI thread.
   */
  default void resourcesAdded(IReferencePoint referencePoint) {
    // NOP
  }
}
