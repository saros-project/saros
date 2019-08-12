package saros.awareness;

import saros.editor.EditorManager;
import saros.editor.FollowModeManager;
import saros.editor.remote.UserEditorStateManager;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.User;

/**
 * Singleton that provides methods to collect and retrieve awareness information for session
 * participants (who is following who, which file is currently opened, etc.)
 */
public class AwarenessInformationCollector {

  private final EditorManager editorManager;

  private volatile FollowModeManager followModeManager;

  private final ISessionLifecycleListener sessionLifeCyclelistener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {
          followModeManager = session.getComponent(FollowModeManager.class);
        }

        @Override
        public void sessionEnding(final ISarosSession session) {
          followModeManager = null;
        }
      };

  public AwarenessInformationCollector(
      ISarosSessionManager sessionManager, final EditorManager editorManager) {

    sessionManager.addSessionLifecycleListener(sessionLifeCyclelistener);
    this.editorManager = editorManager;
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

    final FollowModeManager currentFollowModeManager = followModeManager;

    if (currentFollowModeManager == null) return null;

    return currentFollowModeManager.getFollowModeStates().getFollowee(user);
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
