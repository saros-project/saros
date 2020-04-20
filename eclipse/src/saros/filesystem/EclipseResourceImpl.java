package saros.filesystem;

import static saros.filesystem.IResource.Type.FILE;
import static saros.filesystem.IResource.Type.FOLDER;
import static saros.filesystem.IResource.Type.PROJECT;
import static saros.filesystem.IResource.Type.ROOT;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

public class EclipseResourceImpl implements IResource {

  private static final Map<
          Class<? extends saros.filesystem.IResource>,
          Class<? extends org.eclipse.core.resources.IResource>>
      classMapping;

  static {
    classMapping = new HashMap<>();
    classMapping.put(IResource.class, org.eclipse.core.resources.IResource.class);
    classMapping.put(IWorkspaceRoot.class, org.eclipse.core.resources.IWorkspaceRoot.class);
    classMapping.put(IContainer.class, org.eclipse.core.resources.IContainer.class);
    classMapping.put(IProject.class, org.eclipse.core.resources.IProject.class);
    classMapping.put(IFolder.class, org.eclipse.core.resources.IFolder.class);
    classMapping.put(IFile.class, org.eclipse.core.resources.IFile.class);
  }

  protected final org.eclipse.core.resources.IResource delegate;

  EclipseResourceImpl(org.eclipse.core.resources.IResource delegate) {
    if (delegate == null) throw new NullPointerException("delegate is null");

    this.delegate = delegate;
  }

  @Override
  public boolean exists() {
    return delegate.exists();
  }

  @Override
  public IPath getFullPath() {
    return new EclipsePathImpl(delegate.getFullPath());
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public IContainer getParent() {
    org.eclipse.core.resources.IContainer container = delegate.getParent();

    if (container == null) return null;

    switch (container.getType()) {
      case org.eclipse.core.resources.IResource.FOLDER:
        return new EclipseFolderImpl((org.eclipse.core.resources.IFolder) container);
      case org.eclipse.core.resources.IResource.PROJECT:
        return new EclipseProjectImpl((org.eclipse.core.resources.IProject) container);
      case org.eclipse.core.resources.IResource.ROOT:
        return new EclipseWorkspaceRootImpl((org.eclipse.core.resources.IWorkspaceRoot) container);
      default:
        return null;
    }
  }

  @Override
  public IProject getProject() {
    org.eclipse.core.resources.IProject project = delegate.getProject();

    if (project == null) return null;

    return new EclipseProjectImpl(project);
  }

  @Override
  public IPath getProjectRelativePath() {
    return new EclipsePathImpl(delegate.getProjectRelativePath());
  }

  @Override
  public Type getType() {
    int delegateType = delegate.getType();

    switch (delegateType) {
      case org.eclipse.core.resources.IResource.FILE:
        return FILE;

      case org.eclipse.core.resources.IResource.FOLDER:
        return FOLDER;

      case org.eclipse.core.resources.IResource.PROJECT:
        return PROJECT;

      case org.eclipse.core.resources.IResource.ROOT:
        return ROOT;

      default:
        throw new IllegalStateException("Encountered unknown resource type " + delegateType);
    }
  }

  @Override
  public boolean isIgnored() {
    return isGitConfig() || isDerived();
  }

  /**
   * Returns whether this resource is seen as derived by the local Eclipse instance.
   *
   * @return whether this resource is seen as derived by the local Eclipse instance
   * @see org.eclipse.core.resources.IResource#isDerived(int)
   */
  boolean isDerived() {
    return delegate.isDerived(org.eclipse.core.resources.IResource.CHECK_ANCESTORS);
  }

  /**
   * Returns whether this resource is part of the git configuration directory.
   *
   * @return whether this resource is part of the git configuration directory
   */
  private boolean isGitConfig() {
    String path = getProjectRelativePath().toPortableString();

    return (path.startsWith(".git/")
        || path.contains("/.git/")
        || getType() == FOLDER && (path.endsWith("/.git") || path.equals(".git")));
  }

  @Override
  public void delete() throws IOException {
    try {
      delegate.delete(org.eclipse.core.resources.IResource.KEEP_HISTORY, null);
    } catch (CoreException | OperationCanceledException e) {
      throw new IOException(e);
    }
  }

  @Override
  public IPath getLocation() {
    org.eclipse.core.runtime.IPath location = delegate.getLocation();
    return (location != null) ? new EclipsePathImpl(location) : null;
  }

  @Override
  public <T extends IResource> T adaptTo(Class<T> clazz) {

    /*
     * As we do not know what Eclipse is doing in the background play it safe and let Eclipse always
     * convert the object.
     */

    Class<? extends org.eclipse.core.resources.IResource> classToMap = classMapping.get(clazz);

    if (classToMap == null)
      throw new IllegalArgumentException("class: " + clazz + " is not available as adapter");

    return clazz.cast(ResourceAdapterFactory.create(delegate.getAdapter(classToMap)));
  }

  /**
   * Returns the original {@link org.eclipse.core.resources.IResource IResource} object.
   *
   * @return
   */
  public org.eclipse.core.resources.IResource getDelegate() {
    return delegate;
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (!(obj instanceof EclipseResourceImpl)) return false;

    return delegate.equals(((EclipseResourceImpl) obj).delegate);
  }

  @Override
  public String toString() {
    return delegate.toString() + " (" + getClass().getSimpleName() + ")";
  }
}
