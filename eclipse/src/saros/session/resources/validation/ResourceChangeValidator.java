package saros.session.resources.validation;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import saros.Saros;
import saros.session.ISarosSession;
import saros.session.User.Permission;

/**
 * This model provider is responsible for warning a session participant when trying to modify the
 * resources of a shared project or the project itself in an unsupported way.
 *
 * <p>Ignoring the warnings will lead to inconsistencies. Note that we can't prevent these actions,
 * just detect them and report them.
 *
 * <p>Currently detected:
 *
 * <ul>
 *   <li>File and folder modifications as a user with {@link Permission#READONLY_ACCESS}.
 *   <li>Deletion of a shared project.
 *   <li>Renaming and moving of a shared project.
 * </ul>
 */
/*
 * FIXME there are several deltas that report changes which do not affect the
 * file system. As this class did not work until Eclipse 4.x ??? nobody ever
 * realized this
 */
public class ResourceChangeValidator extends ModelProvider {
  private static final Logger log = Logger.getLogger(ResourceChangeValidator.class);

  /** Error code for internal use, but we don't need it. */
  private static final int ERROR_CODE = 0;

  private static final IStatus MODIFYING_RESOURCES_ERROR_STATUS =
      createErrorStatus(Messages.ResourceChangeValidator_ModifyingResourcesErrorMessage);

  private static final IStatus DELETE_PROJECT_ERROR_STATUS =
      createErrorStatus(Messages.ResourceChangeValidator_DeleteProjectErrorMessage);

  private static final IStatus MOVE_OR_RENAME_PROJECT_ERROR_STATUS =
      createErrorStatus(Messages.ResourceChangeValidator_MoveOrRenameProjectErrorMessage);

  private static volatile ISarosSession session;

  /* The provider is created on the fly when needed so we cannot benefit from the initialize method and so this has to be static. */
  static void setSession(final ISarosSession session) {
    ResourceChangeValidator.session = session;
  }

  @Override
  public IStatus validateChange(final IResourceDelta delta, final IProgressMonitor monitor) {

    final ISarosSession currentSession = session;

    if (currentSession == null) return Status.OK_STATUS;

    final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(currentSession);

    try {
      delta.accept(visitor);
    } catch (CoreException e) {
      log.warn(
          "error occured during delta visiting, some resources might have not been checked", //$NON-NLS-1$
          e);
    }

    if (!currentSession.hasWriteAccess() && visitor.isModifyingResources())
      return MODIFYING_RESOURCES_ERROR_STATUS;

    if (visitor.isDeletingProject()) return DELETE_PROJECT_ERROR_STATUS;

    if (visitor.isMovingProject()) return MOVE_OR_RENAME_PROJECT_ERROR_STATUS;

    return Status.OK_STATUS;
  }

  private static Status createErrorStatus(final String message) {
    return new Status(IStatus.ERROR, Saros.PLUGIN_ID, ERROR_CODE, message, null);
  }
}
