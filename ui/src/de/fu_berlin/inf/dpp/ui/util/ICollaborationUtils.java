package de.fu_berlin.inf.dpp.ui.util;

import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import java.util.List;

/** Offers convenient methods for collaboration actions like sharing a project resources. */
public interface ICollaborationUtils {

  /**
   * Starts a new session and shares the given resources with given contacts.<br>
   * Does nothing if a {@link ISarosSession session} is already running.
   *
   * @param resources to share
   * @param contacts which should be invited
   * @nonBlocking
   */
  public void startSession(List<IResource> resources, List<JID> contacts);

  /**
   * Leaves the currently running {@link ISarosSession}<br>
   * Does nothing if no {@link ISarosSession} is running.
   */
  public void leaveSession();

  /**
   * Adds the given project resources to the session.<br>
   * Does nothing if no {@link ISarosSession session} is running.
   *
   * @param resources
   * @nonBlocking
   */
  public void addResourcesToSession(List<IResource> resources);

  /**
   * Adds the given contacts to the session.<br>
   * Does nothing if no {@link ISarosSession session} is running.
   *
   * @param contacts which should be added to the session.
   * @nonBlocking
   */
  public void addContactsToSession(final List<JID> contacts);
}
