package de.fu_berlin.inf.dpp.awareness;

import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.remote.UserEditorStateManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that provides methods to collect and retrieve awareness information for session
 * participants (who is following who, which file is currently opened, etc.)
 *
 * <p>All methods provided by the interface are <b>not</b> thread safe.
 *
 * @author waldmann
 */
public class AwarenessInformationCollector {

  private final EditorManager editorManager;
  private final ISarosSessionManager sessionManager;

  /** Who is following who in the session? */
  private final Map<User, User> followModes = new ConcurrentHashMap<User, User>();

  public AwarenessInformationCollector(
      ISarosSessionManager sessionManager, final EditorManager editorManager) {

    this.sessionManager = sessionManager;
    this.editorManager = editorManager;
  }

  /**
   * Make sure to call this, when a session ends, or when a session starts to avoid having outdated
   * information
   */
  public void flushFollowModes() {
    followModes.clear();
  }

  /**
   * Remember that "user" is following "target" in the currently running session.
   *
   * @param user
   * @param target
   */
  public void setUserFollowing(User user, User target) {
    assert user != null;
    assert !(user.equals(target));

    followModes.remove(user);

    if (target != null) // null is not allowed in CHM
    followModes.put(user, target);
  }

  /**
   * Returns the followee of the given user, or <code>null</code> if that user does not follow
   * anyone at the moment, or there is no active session.
   *
   * @param user
   * @return
   */
  public User getFollowedUser(User user) {
    assert user != null;

    final ISarosSession session = sessionManager.getSession();

    // should not be called outside of a running session
    if (session == null) return null;

    final User followee = followModes.get(user);

    if (followee == null) return null;

    /*
     * FIXME this should not be done here, it should be the responsibility
     * of the class that calls setUserFollowing to correctly clear this map
     * entries !
     */
    return session.getUser(followee.getJID());
  }

  /**
   * Checks if the currently active editor of the given user is shared. The user can be the local or
   * remote one.
   *
   * @return <code>true</code>, if the active editor of the given user is shared, <code>false</code>
   *     otherwise
   */
  public boolean isActiveEditorShared(User user) {
    if (user == null) return false;

    if (user.isLocal() && editorManager.isActiveEditorShared()) return true;

    UserEditorStateManager mgr = editorManager.getUserEditorStateManager();

    if (mgr != null && mgr.getState(user).getActiveEditorState() != null) return true;

    return false;
  }
}
