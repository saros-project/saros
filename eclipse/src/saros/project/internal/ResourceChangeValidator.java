package saros.project.internal;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.filesystem.ResourceAdapterFactory;
import saros.project.Messages;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User.Permission;

/**
 * This model provider is responsible for warning a session participant when trying to modify the
 * file tree of a shared project in an unsupported way.<br>
 * <br>
 * Ignoring the warnings will lead to inconsistencies. Note that we can't prevent these actions,
 * just detect them. Currently detected: File/folder activities as a user with {@link
 * Permission#READONLY_ACCESS}, and deletion of a shared project.
 */
/*
 * FIXME there are several deltas that report changes which do not affect the
 * file system. As this class did not work until Eclipse 4.x ??? nobody ever
 * realized this
 */
public class ResourceChangeValidator extends ModelProvider {
  private static final Logger log = Logger.getLogger(ResourceChangeValidator.class.getName());

  private static final String ERROR_TEXT = Messages.ResourceChangeValidator_error_no_write_access;
  private static final String DELETE_PROJECT_ERROR_TEXT =
      Messages.ResourceChangeValidator_error_leave_session_before_delete_project;

  /** Error code for internal use, but we don't need it. */
  private static final int ERROR_CODE = 0;

  private static final IStatus ERROR_STATUS =
      new Status(IStatus.ERROR, "saros", ERROR_CODE, ERROR_TEXT, null);

  private static final IStatus DELETE_PROJECT_ERROR_STATUS =
      new Status(IStatus.ERROR, "saros", ERROR_CODE, DELETE_PROJECT_ERROR_TEXT, null);

  @Inject private ISarosSessionManager sessionManager;

  /** the currently running shared project */
  private volatile ISarosSession session;

  /**
   * Check each resource delta whether it is in a shared project. If we are not the exclusive user
   * with {@link Permission#WRITE_ACCESS} set the appropriate flag.
   */
  private static class ResourceDeltaVisitor implements IResourceDeltaVisitor {

    private boolean isAffectingSharedProjectFiles = false;

    private boolean isDeletingSharedProject = false;

    private final ISarosSession session;

    private ResourceDeltaVisitor(final ISarosSession session) {
      assert session != null;
      this.session = session;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.resources.IResourceDeltaVisitor
     */
    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {

      IResource resource = delta.getResource();

      if (resource.getType() == IResource.ROOT) return true;

      if (resource.getType() == IResource.PROJECT) {
        if (!session.isShared(ResourceAdapterFactory.create(resource))) return false;

        if (delta.getKind() == IResourceDelta.REMOVED) {
          isDeletingSharedProject = true;
          return false;
        }

        return true;
      }

      isAffectingSharedProjectFiles = true;
      return false;
    }
  }

  @Override
  protected void initialize() {

    SarosPluginContext.initComponent(this);

    sessionManager.addSessionLifecycleListener(
        new ISessionLifecycleListener() {
          @Override
          public void sessionStarted(ISarosSession newSarosSession) {
            session = newSarosSession;
          }

          @Override
          public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
            assert session == oldSarosSession;
            session = null;
          }
        });
    session = sessionManager.getSession();
  }

  @Override
  public IStatus validateChange(IResourceDelta delta, IProgressMonitor pm) {

    final ISarosSession currentSession = session;
    // If we are currently not sharing a project, we don't have to prevent
    // any file operations
    if (currentSession == null) return Status.OK_STATUS;

    ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(currentSession);

    try {
      delta.accept(visitor);
    } catch (CoreException e) {
      log.error("Could not run visitor: ", e);
    }

    if (!currentSession.hasWriteAccess() && visitor.isAffectingSharedProjectFiles) {
      return ERROR_STATUS;
    }

    if (visitor.isDeletingSharedProject) {
      return DELETE_PROJECT_ERROR_STATUS;
    }

    return Status.OK_STATUS;
  }
}
