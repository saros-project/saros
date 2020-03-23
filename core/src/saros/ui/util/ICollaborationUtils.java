package saros.ui.util;

import java.util.List;
import java.util.Set;
import saros.filesystem.IProject;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;

/** Offers convenient methods for collaboration actions like sharing a project resources. */
public interface ICollaborationUtils {

  /**
   * Starts a new session and shares the given projects with given contacts.<br>
   * Does nothing if a {@link ISarosSession session} is already running.
   *
   * @param projects projects to share
   * @param contacts which should be invited
   * @nonBlocking
   */
  public void startSession(Set<IProject> projects, List<JID> contacts);

  /**
   * Leaves the currently running {@link ISarosSession}<br>
   * Does nothing if no {@link ISarosSession} is running.
   */
  public void leaveSession();

  /**
   * Adds the given projects to the session.<br>
   * Does nothing if no {@link ISarosSession session} is running.
   *
   * @param projects the projects to add to the session
   * @nonBlocking
   */
  public void addProjectsToSession(Set<IProject> projects);

  /**
   * Adds the given contacts to the session.<br>
   * Does nothing if no {@link ISarosSession session} is running.
   *
   * @param contacts which should be added to the session.
   * @nonBlocking
   */
  public void addContactsToSession(final List<JID> contacts);
}
