package saros.session.resources.validation;

import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import saros.Saros;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.session.ISarosSession;
import saros.session.User.Permission;

/**
 * This model provider is responsible for warning a session participant when trying to modify the
 * resources of a shared reference point or the reference point resource itself in an unsupported
 * way.
 *
 * <p>Ignoring the warnings will lead to inconsistencies. Note that we can't prevent these actions,
 * just detect them and report them.
 *
 * <p>Currently detected:
 *
 * <ul>
 *   <li>File and folder modifications as a user with {@link Permission#READONLY_ACCESS}.
 *   <li>Deletion of a shared reference point resource.
 *   <li>Renaming and moving of a shared reference point resource.
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

  private static final IStatus DELETE_REFERENCE_POINT_ERROR_STATUS =
      createErrorStatus(Messages.ResourceChangeValidator_DeleteReferencePointErrorMessage);

  private static final IStatus MOVE_OR_RENAME_REFERENCE_POINT_ERROR_STATUS =
      createErrorStatus(Messages.ResourceChangeValidator_MoveOrRenameReferencePointErrorMessage);

  private static volatile ISarosSession session;

  /* The provider is created on the fly when needed so we cannot benefit from the initialize method and so this has to be static. */
  static void setSession(final ISarosSession session) {
    ResourceChangeValidator.session = session;
  }

  @Override
  public IStatus validateChange(final IResourceDelta delta, final IProgressMonitor monitor) {

    final ISarosSession currentSession = session;

    if (currentSession == null) return Status.OK_STATUS;

    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();

    for (IReferencePoint referencePoint : sharedReferencePoints) {
      IContainer referencePointDelegate = ResourceConverter.getDelegate(referencePoint);

      IResourceDelta referencePointDelta = delta.findMember(referencePointDelegate.getFullPath());

      if (referencePointDelta == null) {
        continue;
      }

      final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(referencePoint, currentSession);
      try {
        referencePointDelta.accept(visitor);
      } catch (CoreException e) {
        log.warn(
            "error occurred during delta visitation, some resources might have not been checked", //$NON-NLS-1$
            e);
      }

      if (!currentSession.hasWriteAccess() && visitor.isModifyingResources())
        return MODIFYING_RESOURCES_ERROR_STATUS;

      if (visitor.isDeletingReferencePointResource()) return DELETE_REFERENCE_POINT_ERROR_STATUS;

      if (visitor.isMovingReferencePointResource())
        return MOVE_OR_RENAME_REFERENCE_POINT_ERROR_STATUS;
    }

    return Status.OK_STATUS;
  }

  private static Status createErrorStatus(final String message) {
    return new Status(IStatus.ERROR, Saros.PLUGIN_ID, ERROR_CODE, message, null);
  }
}
