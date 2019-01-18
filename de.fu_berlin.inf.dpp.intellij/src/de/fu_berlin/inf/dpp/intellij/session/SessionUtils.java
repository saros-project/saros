package de.fu_berlin.inf.dpp.intellij.session;

import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJReferencePointManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.annotations.Inject;

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
  private static volatile ISarosSession currentSarosSession;

  @Inject private static ISarosSessionManager sarosSessionManager;

  @Inject private static IntelliJReferencePointManager intelliJReferencePointManager;

  static {
    SarosPluginContext.initComponent(new SessionUtils());

    sarosSessionManager.addSessionLifecycleListener(
        new ISessionLifecycleListener() {
          @Override
          public void sessionStarted(ISarosSession session) {

            currentSarosSession = session;
          }

          @Override
          public void sessionEnded(ISarosSession session, SessionEndReason reason) {

            currentSarosSession = null;
          }
        });
  }

  private SessionUtils() {
    // NOP
  }

  /**
   * Returns whether the resource represented by the given SPath is shared.
   *
   * @param resource the resource to check
   * @return <code>true</code> if a session is currently running and the given resource exists and
   *     is shared, <code>false</code> otherwise
   */
  public static boolean isShared(
      @NotNull IReferencePoint referencePoint, @NotNull IResource resource) {

    ISarosSession session = currentSarosSession;

    return session != null && session.isShared(referencePoint, resource);
  }

  /**
   * Returns whether the resource represented by the given SPath is shared.
   *
   * @param path the SPath representing the resource to check
   * @return <code>true</code> if a session is currently running and the resource represented by the
   *     given resource exists and is shared, <code>false</code> otherwise
   */
  public static boolean isShared(@NotNull SPath path) {

    ISarosSession session = currentSarosSession;

    if (session == null) return false;

    IReferencePoint referencePoint = path.getReferencePoint();

    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    VirtualFile virtualFile =
        intelliJReferencePointManager.getResource(referencePoint, referencePointRelativePath);

    if (virtualFile == null) return false;

    IResource resource = VirtualFileConverter.convertToResource(virtualFile);

    return resource != null && session.isShared(referencePoint, resource);
  }
}
