package saros.intellij.session;

import org.jetbrains.annotations.NotNull;
import saros.activities.SPath;
import saros.filesystem.IResource;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

/**
 * A utility class to provide easy access to functionality needing a session object.
 *
 * <p><b>NOTE:</b> It should be carefully considered when to use this class as it generally is an
 * indicator that the calling class should belong to the session context (which would make the usage
 * of this class unnecessary).
 *
 * @deprecated Consider whether your class actually belongs inside the session context and use the
 *     session container instead in such cases.
 */
// TODO consider to remove this class after the session & plugin context have been correctly
// separated
@Deprecated
public class SessionUtils {

  @SuppressWarnings("FieldCanBeLocal")
  private ISessionLifecycleListener lifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession session) {

          currentSarosSession = session;
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {

          currentSarosSession = null;
        }
      };

  private static volatile ISarosSession currentSarosSession;

  /**
   * Initializes the SessionUtils. This is only used as part of the plugin context initialization by
   * the PicoContainer and should not be called otherwise.
   *
   * @param sarosSessionManager the current SarosSessionManager instance
   */
  public SessionUtils(ISarosSessionManager sarosSessionManager) {
    sarosSessionManager.addSessionLifecycleListener(lifecycleListener);
  }

  /**
   * Returns whether the resource represented by the given SPath is shared.
   *
   * @param resource the resource to check
   * @return <code>true</code> if a session is currently running and the given resource exists and
   *     is shared, <code>false</code> otherwise
   */
  public static boolean isShared(@NotNull IResource resource) {

    ISarosSession session = currentSarosSession;

    return session != null && session.isShared(resource);
  }

  /**
   * Returns whether the resource represented by the given SPath is shared.
   *
   * @param path the SPath representing the resource to check
   * @return <code>true</code> if a session is currently running and the resource represented by the
   *     given resource exists and is shared, <code>false</code> otherwise
   */
  public static boolean isShared(@NotNull SPath path) {

    IResource resource = path.getResource();

    ISarosSession session = currentSarosSession;

    return resource != null && session != null && session.isShared(resource);
  }
}
