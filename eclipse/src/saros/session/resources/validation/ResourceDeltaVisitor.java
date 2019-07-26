package saros.session.resources.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import saros.filesystem.ResourceAdapterFactory;
import saros.session.ISarosSession;

/**
 * This <code>IResourceDeltaVisitor</code> implementation tracks resource changes which will cause
 * inconsistencies when these changed would actually be applied.
 */
final class ResourceDeltaVisitor implements IResourceDeltaVisitor {

  private boolean isModifyingResources = false;

  private boolean isDeletingProject = false;

  private boolean isMovingProject = false;

  private final ISarosSession session;

  ResourceDeltaVisitor(final ISarosSession session) {
    this.session = session;
  }

  @Override
  public boolean visit(final IResourceDelta delta) throws CoreException {

    final IResource resource = delta.getResource();

    if (resource.getType() == IResource.ROOT) return true;

    if (!session.isShared(ResourceAdapterFactory.create(resource))) return true;

    if (resource.getType() != IResource.PROJECT) {
      isModifyingResources = true;
      return false;
    }

    if (delta.getKind() == IResourceDelta.REMOVED) {

      if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) isMovingProject = true;
      else isDeletingProject = true;

      return false;
    }

    return true;
  }

  boolean isModifyingResources() {
    return isModifyingResources;
  }

  boolean isDeletingProject() {
    return isDeletingProject;
  }

  boolean isMovingProject() {
    return isMovingProject;
  }
}
