/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.User.Permission;

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
  public default void permissionChanged(User user) {
    // NOP
  }

  /**
   * Is fired when an user joins the shared project.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user that has joined.
   */
  public default void userJoined(User user) {
    // NOP
  }

  /**
   * Is fired when a user started queuing and is now able to process all activities.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user that has joined.
   */
  public default void userStartedQueuing(User user) {
    // NOP
  }

  /**
   * Is fired when a finished the Project Negotiation
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user that has joined.
   */
  public default void userFinishedProjectNegotiation(User user) {
    // NOP
  }

  /**
   * Is fired when the color assigned to a user in the session changed.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user whose color changed
   */
  public default void userColorChanged(User user) {
    // NOP
  }

  /**
   * Is fired when an user leaves the shared project.
   *
   * <p>This method is called on the UI thread.
   *
   * @param user the user that has left.
   */
  public default void userLeft(User user) {
    // NOP
  }

  /**
   * Is fired then a project has been made part of the session, either because the local user began
   * sharing it or because it is being shared by a remote user.
   *
   * <p>Note that this event is also fired if a project is re-shared with a different set of shared
   * resources (e.g. by sharing a previously unshared folder of a partially shared project).
   *
   * <p>This method might <i>not</i> be called on the UI thread.
   *
   * @param project the project that was added
   */
  public default void projectAdded(IProject project) {
    // NOP
  }

  /**
   * Is fired then a project has been removed from the session, meaning it is not shared between the
   * session's users anymore.
   *
   * <p>This method might <i>not</i> be called on the UI thread.
   *
   * @param project the project that was removed
   */
  public default void projectRemoved(IProject project) {
    // NOP
  }

  /**
   * Is fired when resources are added to the current session.
   *
   * <p>This method might <i>not</i> be called on the UI thread.
   */
  public default void resourcesAdded(IProject project) {
    // NOP
  }
}
